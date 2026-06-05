param(
    [string]$BaseUrl = "http://127.0.0.1:8080",
    [int]$RequestCount = 20,
    [int]$Concurrency = 5,
    [long]$ActivityId = 1001,
    [long]$TicketId = 501,
    [int]$Quantity = 1,
    [long]$StartUserId = 0,
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

function Invoke-SeckillRequest {
    param(
        [int]$Index,
        [string]$BaseUrl,
        [long]$ActivityId,
        [long]$TicketId,
        [int]$Quantity,
        [long]$StartUserId,
        [string]$RunId
    )

    $requestId = "phase6-$RunId-req-$Index"
    $idempotencyKey = "phase6-$RunId-idem-$Index"
    $userId = $StartUserId + $Index
    $uri = "$BaseUrl/api/v1/seckill/reservations"
    $body = @{
        requestId = $requestId
        idempotencyKey = $idempotencyKey
        userId = $userId
        activityId = $ActivityId
        ticketId = $TicketId
        quantity = $Quantity
    } | ConvertTo-Json

    $watch = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $response = Invoke-RestMethod -Method Post -Uri $uri -ContentType "application/json" -Body $body
        $watch.Stop()
        return [pscustomobject]@{
            index = $Index
            userId = $userId
            requestId = $requestId
            success = ($response.code -eq 0)
            responseCode = $response.code
            reservationId = $response.data.reservationId
            status = $response.data.status
            elapsedMs = [math]::Round($watch.Elapsed.TotalMilliseconds, 2)
            errorMessage = ""
        }
    } catch {
        $watch.Stop()
        return [pscustomobject]@{
            index = $Index
            userId = $userId
            requestId = $requestId
            success = $false
            responseCode = -1
            reservationId = ""
            status = "FAILED"
            elapsedMs = [math]::Round($watch.Elapsed.TotalMilliseconds, 2)
            errorMessage = $_.Exception.Message
        }
    }
}

$runId = Get-Date -Format "yyyyMMddHHmmss"
if ($StartUserId -le 0) {
    $StartUserId = [DateTimeOffset]::Now.ToUnixTimeSeconds() * 1000
}
$allResults = New-Object System.Collections.Generic.List[object]
$startTime = Get-Date

for ($offset = 0; $offset -lt $RequestCount; $offset += $Concurrency) {
    $batchEnd = [math]::Min($offset + $Concurrency - 1, $RequestCount - 1)
    $jobs = @()
    for ($index = $offset; $index -le $batchEnd; $index++) {
        $jobs += Start-Job -ScriptBlock {
            param($Index, $BaseUrl, $ActivityId, $TicketId, $Quantity, $StartUserId, $RunId)

            $requestId = "phase6-$RunId-req-$Index"
            $idempotencyKey = "phase6-$RunId-idem-$Index"
            $userId = $StartUserId + $Index
            $uri = "$BaseUrl/api/v1/seckill/reservations"
            $body = @{
                requestId = $requestId
                idempotencyKey = $idempotencyKey
                userId = $userId
                activityId = $ActivityId
                ticketId = $TicketId
                quantity = $Quantity
            } | ConvertTo-Json

            $watch = [System.Diagnostics.Stopwatch]::StartNew()
            try {
                $response = Invoke-RestMethod -Method Post -Uri $uri -ContentType "application/json" -Body $body
                $watch.Stop()
                [pscustomobject]@{
                    index = $Index
                    userId = $userId
                    requestId = $requestId
                    success = ($response.code -eq 0)
                    responseCode = $response.code
                    reservationId = $response.data.reservationId
                    status = $response.data.status
                    elapsedMs = [math]::Round($watch.Elapsed.TotalMilliseconds, 2)
                    errorMessage = ""
                }
            } catch {
                $watch.Stop()
                [pscustomobject]@{
                    index = $Index
                    userId = $userId
                    requestId = $requestId
                    success = $false
                    responseCode = -1
                    reservationId = ""
                    status = "FAILED"
                    elapsedMs = [math]::Round($watch.Elapsed.TotalMilliseconds, 2)
                    errorMessage = $_.Exception.Message
                }
            }
        } -ArgumentList $index, $BaseUrl, $ActivityId, $TicketId, $Quantity, $StartUserId, $runId
    }

    Wait-Job -Job $jobs | Out-Null
    foreach ($job in $jobs) {
        $result = Receive-Job -Job $job
        [void]$allResults.Add($result)
        Remove-Job -Job $job | Out-Null
    }
}

$endTime = Get-Date
$successResults = @($allResults | Where-Object { $_.success })
$failureResults = @($allResults | Where-Object { -not $_.success })
$latencies = @($allResults | ForEach-Object { [double]$_.elapsedMs })
$durationSeconds = [math]::Max([math]::Round((New-TimeSpan -Start $startTime -End $endTime).TotalSeconds, 2), 0.01)
$successRate = if ($RequestCount -eq 0) { 0 } else { [math]::Round(($successResults.Count / $RequestCount) * 100, 2) }
$qps = [math]::Round($RequestCount / $durationSeconds, 2)

$summary = [pscustomobject]@{
    runId = $runId
    startedAt = $startTime.ToString("yyyy-MM-dd HH:mm:ss")
    finishedAt = $endTime.ToString("yyyy-MM-dd HH:mm:ss")
    requestCount = $RequestCount
    concurrency = $Concurrency
    successCount = $successResults.Count
    failureCount = $failureResults.Count
    successRate = $successRate
    qps = $qps
    p50Ms = Get-PercentileValue -Values $latencies -Percentile 0.50
    p95Ms = Get-PercentileValue -Values $latencies -Percentile 0.95
    p99Ms = Get-PercentileValue -Values $latencies -Percentile 0.99
    minMs = if ($latencies.Count -eq 0) { 0 } else { [math]::Round((($latencies | Measure-Object -Minimum).Minimum), 2) }
    maxMs = if ($latencies.Count -eq 0) { 0 } else { [math]::Round((($latencies | Measure-Object -Maximum).Maximum), 2) }
}

$summary | Format-List

if ($failureResults.Count -gt 0) {
    Write-Output "失败样本:"
    $failureResults | Select-Object -First 10 | Format-Table index, userId, status, elapsedMs, errorMessage -AutoSize
}

if (-not [string]::IsNullOrWhiteSpace($OutputJsonPath)) {
    $outputDirectory = Split-Path -Path $OutputJsonPath -Parent
    if (-not [string]::IsNullOrWhiteSpace($outputDirectory) -and -not (Test-Path -Path $outputDirectory)) {
        New-Item -ItemType Directory -Path $outputDirectory | Out-Null
    }
    [pscustomobject]@{
        summary = $summary
        results = $allResults
    } | ConvertTo-Json -Depth 6 | Set-Content -Path $OutputJsonPath -Encoding UTF8
}
