param(
    [string]$BaseUrl = "http://127.0.0.1:8080",
    [int]$RequestCount = 20,
    [int]$Concurrency = 5,
    [long]$ActivityId = 1001,
    [long]$TicketId = 501,
    [int]$Quantity = 1,
    [string]$UsernamePrefix = "",
    [string]$Password = "password123",
    [string]$OutputJsonPath = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Get-PercentileValue {
    param(
        [double[]]$Values,
        [double]$Percentile
    )

    if ($Values.Count -eq 0) {
        return 0
    }

    $sorted = $Values | Sort-Object
    $position = [math]::Ceiling($sorted.Count * $Percentile) - 1
    if ($position -lt 0) {
        $position = 0
    }
    if ($position -ge $sorted.Count) {
        $position = $sorted.Count - 1
    }
    return [math]::Round([double]$sorted[$position], 2)
}

function Invoke-TicketApi {
    param(
        [string]$Method,
        [string]$Uri,
        [object]$Body = $null,
        [string]$AccessToken = ""
    )

    $headers = @{
        Accept = "application/json"
    }
    if (-not [string]::IsNullOrWhiteSpace($AccessToken)) {
        $headers.Authorization = "Bearer $AccessToken"
    }

    $options = @{
        Method = $Method
        Uri = $Uri
        Headers = $headers
    }
    if ($null -ne $Body) {
        $options.ContentType = "application/json"
        $options.Body = ($Body | ConvertTo-Json -Depth 8)
    }

    return Invoke-RestMethod @options
}

function New-LoadTestUser {
    param(
        [string]$BaseUrl,
        [string]$Username,
        [string]$Password
    )

    $registerBody = @{
        username = $Username
        password = $Password
        displayName = "压测用户-$Username"
    }

    try {
        [void](Invoke-TicketApi -Method Post -Uri "$BaseUrl/api/v1/users/register" -Body $registerBody)
    } catch {
        Write-Verbose "注册用户可能已存在，username=$Username, error=$($_.Exception.Message)"
    }

    $loginResponse = Invoke-TicketApi -Method Post -Uri "$BaseUrl/api/v1/users/login" -Body @{
        username = $Username
        password = $Password
    }

    if ($loginResponse.code -ne 0 -or [string]::IsNullOrWhiteSpace($loginResponse.data.accessToken)) {
        throw "登录失败，username=$Username, code=$($loginResponse.code), message=$($loginResponse.message)"
    }

    return [pscustomobject]@{
        username = $Username
        accessToken = $loginResponse.data.accessToken
    }
}

$runId = Get-Date -Format "yyyyMMddHHmmss"
if ([string]::IsNullOrWhiteSpace($UsernamePrefix)) {
    $UsernamePrefix = "lt_user_$runId"
}

$startTime = Get-Date
$users = New-Object System.Collections.Generic.List[object]
Write-Output "准备临时用户并登录，count=$RequestCount"
for ($index = 0; $index -lt $RequestCount; $index++) {
    $username = "$UsernamePrefix`_$index"
    [void]$users.Add((New-LoadTestUser -BaseUrl $BaseUrl -Username $username -Password $Password))
}

$submitResults = New-Object System.Collections.Generic.List[object]
Write-Output "开始并发提交抢票请求，requestCount=$RequestCount, concurrency=$Concurrency"
for ($offset = 0; $offset -lt $RequestCount; $offset += $Concurrency) {
    $batchEnd = [math]::Min($offset + $Concurrency - 1, $RequestCount - 1)
    $jobs = @()
    for ($index = $offset; $index -le $batchEnd; $index++) {
        $user = $users[$index]
        $jobs += Start-Job -ScriptBlock {
            param($Index, $BaseUrl, $ActivityId, $TicketId, $Quantity, $RunId, $Username, $AccessToken)

            $requestId = "userflow-$RunId-req-$Index"
            $idempotencyKey = "userflow-$RunId-idem-$Index"
            $uri = "$BaseUrl/api/v1/seckill/reservations"
            $body = @{
                requestId = $requestId
                idempotencyKey = $idempotencyKey
                activityId = $ActivityId
                ticketId = $TicketId
                quantity = $Quantity
            } | ConvertTo-Json -Depth 8
            $headers = @{
                Accept = "application/json"
                Authorization = "Bearer $AccessToken"
            }

            $watch = [System.Diagnostics.Stopwatch]::StartNew()
            try {
                $response = Invoke-RestMethod -Method Post -Uri $uri -Headers $headers -ContentType "application/json" -Body $body
                $watch.Stop()
                [pscustomobject]@{
                    index = $Index
                    username = $Username
                    requestId = $requestId
                    success = ($response.code -eq 0)
                    responseCode = $response.code
                    reservationId = if ($null -ne $response.data) { $response.data.reservationId } else { "" }
                    submitStatus = if ($null -ne $response.data) { $response.data.status } else { "FAILED" }
                    elapsedMs = [math]::Round($watch.Elapsed.TotalMilliseconds, 2)
                    errorMessage = ""
                }
            } catch {
                $watch.Stop()
                [pscustomobject]@{
                    index = $Index
                    username = $Username
                    requestId = $requestId
                    success = $false
                    responseCode = -1
                    reservationId = ""
                    submitStatus = "FAILED"
                    elapsedMs = [math]::Round($watch.Elapsed.TotalMilliseconds, 2)
                    errorMessage = $_.Exception.Message
                }
            }
        } -ArgumentList $index, $BaseUrl, $ActivityId, $TicketId, $Quantity, $runId, $user.username, $user.accessToken
    }

    Wait-Job -Job $jobs | Out-Null
    foreach ($job in $jobs) {
        $result = Receive-Job -Job $job
        [void]$submitResults.Add($result)
        Remove-Job -Job $job | Out-Null
    }
}

Write-Output "查询用户侧结果感知与我的订单接口"
$resultQueries = New-Object System.Collections.Generic.List[object]
$orderQueries = New-Object System.Collections.Generic.List[object]
foreach ($submitResult in $submitResults) {
    $user = $users[$submitResult.index]

    if (-not [string]::IsNullOrWhiteSpace($submitResult.reservationId)) {
        $watch = [System.Diagnostics.Stopwatch]::StartNew()
        try {
            $resultResponse = Invoke-TicketApi -Method Get -Uri "$BaseUrl/api/v1/orders/reservations/$($submitResult.reservationId)" -AccessToken $user.accessToken
            $watch.Stop()
            [void]$resultQueries.Add([pscustomobject]@{
                index = $submitResult.index
                username = $submitResult.username
                reservationId = $submitResult.reservationId
                success = ($resultResponse.code -eq 0)
                resultStatus = if ($null -ne $resultResponse.data) { $resultResponse.data.resultStatus } else { "EMPTY" }
                orderId = if ($null -ne $resultResponse.data) { $resultResponse.data.orderId } else { $null }
                elapsedMs = [math]::Round($watch.Elapsed.TotalMilliseconds, 2)
                errorMessage = ""
            })
        } catch {
            $watch.Stop()
            [void]$resultQueries.Add([pscustomobject]@{
                index = $submitResult.index
                username = $submitResult.username
                reservationId = $submitResult.reservationId
                success = $false
                resultStatus = "FAILED"
                orderId = $null
                elapsedMs = [math]::Round($watch.Elapsed.TotalMilliseconds, 2)
                errorMessage = $_.Exception.Message
            })
        }
    }

    $watch = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $ordersResponse = Invoke-TicketApi -Method Get -Uri "$BaseUrl/api/v1/orders?current=1&pageSize=10" -AccessToken $user.accessToken
        $watch.Stop()
        [void]$orderQueries.Add([pscustomobject]@{
            index = $submitResult.index
            username = $submitResult.username
            success = ($ordersResponse.code -eq 0)
            total = if ($null -ne $ordersResponse.data) { $ordersResponse.data.total } else { 0 }
            recordCount = if ($null -ne $ordersResponse.data -and $null -ne $ordersResponse.data.records) { $ordersResponse.data.records.Count } else { 0 }
            elapsedMs = [math]::Round($watch.Elapsed.TotalMilliseconds, 2)
            errorMessage = ""
        })
    } catch {
        $watch.Stop()
        [void]$orderQueries.Add([pscustomobject]@{
            index = $submitResult.index
            username = $submitResult.username
            success = $false
            total = 0
            recordCount = 0
            elapsedMs = [math]::Round($watch.Elapsed.TotalMilliseconds, 2)
            errorMessage = $_.Exception.Message
        })
    }
}

$endTime = Get-Date
$successSubmitResults = @($submitResults | Where-Object { $_.success })
$failureSubmitResults = @($submitResults | Where-Object { -not $_.success })
$latencies = @($submitResults | ForEach-Object { [double]$_.elapsedMs })
$durationSeconds = [math]::Max([math]::Round((New-TimeSpan -Start $startTime -End $endTime).TotalSeconds, 2), 0.01)
$reservationIds = @($successSubmitResults | Where-Object { -not [string]::IsNullOrWhiteSpace($_.reservationId) } | Select-Object -ExpandProperty reservationId)
$resultStatusGroups = $resultQueries | Group-Object resultStatus | ForEach-Object {
    [pscustomobject]@{
        status = $_.Name
        count = $_.Count
    }
}

$summary = [pscustomobject]@{
    runId = $runId
    startedAt = $startTime.ToString("yyyy-MM-dd HH:mm:ss")
    finishedAt = $endTime.ToString("yyyy-MM-dd HH:mm:ss")
    requestCount = $RequestCount
    concurrency = $Concurrency
    submitSuccessCount = $successSubmitResults.Count
    submitFailureCount = $failureSubmitResults.Count
    reservationIdCount = $reservationIds.Count
    uniqueReservationIdCount = @($reservationIds | Sort-Object -Unique).Count
    resultQuerySuccessCount = @($resultQueries | Where-Object { $_.success }).Count
    orderQuerySuccessCount = @($orderQueries | Where-Object { $_.success }).Count
    orderListRecordHitCount = @($orderQueries | Where-Object { $_.recordCount -gt 0 }).Count
    qps = [math]::Round($RequestCount / $durationSeconds, 2)
    p50SubmitMs = Get-PercentileValue -Values $latencies -Percentile 0.50
    p95SubmitMs = Get-PercentileValue -Values $latencies -Percentile 0.95
    p99SubmitMs = Get-PercentileValue -Values $latencies -Percentile 0.99
    resultStatuses = $resultStatusGroups
}

$summary | Format-List

if ($failureSubmitResults.Count -gt 0) {
    Write-Output "提交失败样本:"
    $failureSubmitResults | Select-Object -First 10 | Format-Table index, username, responseCode, submitStatus, elapsedMs, errorMessage -AutoSize
}

if (-not [string]::IsNullOrWhiteSpace($OutputJsonPath)) {
    $outputDirectory = Split-Path -Path $OutputJsonPath -Parent
    if (-not [string]::IsNullOrWhiteSpace($outputDirectory) -and -not (Test-Path -Path $outputDirectory)) {
        New-Item -ItemType Directory -Path $outputDirectory | Out-Null
    }
    [pscustomobject]@{
        summary = $summary
        submitResults = $submitResults
        resultQueries = $resultQueries
        orderQueries = $orderQueries
    } | ConvertTo-Json -Depth 8 | Set-Content -Path $OutputJsonPath -Encoding UTF8
}
