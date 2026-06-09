param(
    [string]$ContainerName = "ticket-mysql",
    [string]$Database = "ticket_system",
    [string]$Username = "root",
    [string]$Password = "root123456"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Resolve-Path (Join-Path $scriptRoot "..\..")
$migrationDir = Join-Path $projectRoot "docker\mysql\init"
$sqlFiles = Get-ChildItem -Path $migrationDir -Filter *.sql | Sort-Object Name

if (-not $sqlFiles) {
    throw "No SQL migration files were found under $migrationDir."
}

function New-MysqlDockerExecArguments {
    param(
        [switch]$NoColumnNames,
        [switch]$BatchMode
    )

    $arguments = @(
        "exec",
        "-i",
        "-e", "MYSQL_PWD=$Password",
        $ContainerName,
        "mysql",
        "--default-character-set=utf8mb4",
        "-u$Username",
        "-D$Database"
    )

    if ($NoColumnNames) {
        $arguments += "-N"
    }
    if ($BatchMode) {
        $arguments += "-B"
    }

    return $arguments
}

function Invoke-DockerMysqlSql {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Sql
    )

    $arguments = New-MysqlDockerExecArguments -NoColumnNames -BatchMode
    $output = $Sql | & docker @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to execute MySQL SQL. container=$ContainerName, database=$Database, exitCode=$LASTEXITCODE."
    }
    return ($output | Out-String).Trim()
}

function Invoke-DockerMysqlFile {
    param(
        [Parameter(Mandatory = $true)]
        [string]$FilePath
    )

    $arguments = New-MysqlDockerExecArguments
    Get-Content -Path $FilePath -Raw -Encoding UTF8 | & docker @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to execute migration script. file=$(Split-Path -Leaf $FilePath), container=$ContainerName, database=$Database, exitCode=$LASTEXITCODE."
    }
}

function Escape-SqlLiteral {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Value
    )

    return $Value.Replace("'", "''")
}

Write-Host "Checking local database migrations. container=$ContainerName, database=$Database"

$containerStatus = docker ps --filter "name=^$ContainerName$" --format "{{.Names}}"
if (-not $containerStatus) {
    throw "Container $ContainerName is not running. Start docker compose first."
}

$bootstrapSql = @"
CREATE TABLE IF NOT EXISTS schema_migration_log (
    migration_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    script_name VARCHAR(128) NOT NULL,
    script_checksum VARCHAR(128) NOT NULL,
    applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_schema_migration_log_script_name (script_name)
);
"@
Invoke-DockerMysqlSql -Sql $bootstrapSql | Out-Null

foreach ($sqlFile in $sqlFiles) {
    $checksum = (Get-FileHash -Path $sqlFile.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    $scriptName = Escape-SqlLiteral -Value $sqlFile.Name
    $lookupSql = "SELECT script_checksum FROM schema_migration_log WHERE script_name = '$scriptName' LIMIT 1;"
    $existingChecksum = Invoke-DockerMysqlSql -Sql $lookupSql

    if ($existingChecksum) {
        if ($existingChecksum -ne $checksum) {
            throw "Migration script $($sqlFile.Name) was already applied, but its checksum has changed. Confirm database and script versions manually."
        }

        Write-Host "Skipping applied script: $($sqlFile.Name)"
        continue
    }

    Write-Host "Applying migration script: $($sqlFile.Name)"
    Invoke-DockerMysqlFile -FilePath $sqlFile.FullName

    $insertSql = @"
INSERT INTO schema_migration_log (script_name, script_checksum)
VALUES ('$scriptName', '$checksum');
"@
    Invoke-DockerMysqlSql -Sql $insertSql | Out-Null
}

Write-Host "Local database migrations completed."
