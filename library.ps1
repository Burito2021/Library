#Function sections ----------START--------------
function Remove-DockerImages {
    param (
        [string[]]$ImageNames,
        [switch]$Force,
        [switch]$Quiet
    )

    if (-not $ImageNames -or $ImageNames.Count -eq 0) {
        Write-Host "No image names provided" -ForegroundColor Red
        return
    }

    foreach ($imageName in $ImageNames) {
        try {
            Write-Host "Removing image: $imageName" -ForegroundColor Yellow

            if ($Force -and $Quiet) {
                docker rmi -f -q $imageName 2>$null
            } elseif ($Force) {
                docker rmi -f $imageName
            } elseif ($Quiet) {
                docker rmi -q $imageName 2>$null
            } else {
                docker rmi $imageName
            }

            if ($LASTEXITCODE -eq 0) {
                Write-Host "Successfully removed: $imageName" -ForegroundColor Green
            } else {
                Write-Host "Failed to remove: $imageName" -ForegroundColor Gray
            }
        }
        catch {
            Write-Host "Error removing $imageName`: $_" -ForegroundColor Gray
        }
    }
}

function Find-Content
{
    param(
        [string]$content,
        [string]$expression
    )
    $match = [regex]::Matches($content, $expression)
    return $match
}

function Get-InfoMenu
{
    param(
        [string]$composePath,
        [string]$homeLocation
    )
    Clear-Host
    Write-Host ""
    Write-Host "Info" -ForegroundColor Cyan
    Write-Host "===========================" -ForegroundColor Cyan
    $appHost = "http://localhost"
#--------------------------------------------------------------------
    Write-Host "Below are details(reached externally)on the containers and credentials:"
    $сontent = Get-FileContent -filePath $composePath
    $fitleredPostgresDbContent = Find-Content -content $сontent -expression "postgres-script:[\s\S]*?ports:([\s\S]*?)(?=\s{2}\w|\z)"
    $postgresPortSection = $fitleredPostgresDbContent.Groups[1].Value
    $postgresDbPortMatches = [regex]::Matches($postgresPortSection, '"(\d+):\d+"')
    $postresDbPorts = $postgresDbPortMatches | ForEach-Object { $_.Groups[1].Value }
    Write-Host "1. PostgresDb: $appHost`:$postresDbPorts"
    #--------------------------------------------------------------------
    $dbMatches = [regex]::Matches($сontent, 'POSTGRES_DB:\s*([^\s\n]+)')
    $postgresDb = $dbMatches[0].Groups[1].Value
    Write-Host "PostgresDb schema: $postgresDb"
    $userMatches = [regex]::Matches($сontent, 'POSTGRES_USER:\s*([^\s\n]+)')
    $postgresUser = $userMatches[0].Groups[1].Value
    Write-Host "PostgresDb user: $postgresUser"
    $passwordMatches = [regex]::Matches($сontent, 'POSTGRES_PASSWORD:\s*([^\s\n]+)')
    $postgresPassword = $passwordMatches[0].Groups[1].Value
    Write-Host "PostgresDb password: $postgresPassword"
    Write-Host "-------------------------------------------------------------"
    $appContent = Find-Content -content $сontent -expression "library-script:[\s\S]*?ports:([\s\S]*?)(?=\s{2}\w|\z)"
    $appPortSection = $appContent.Groups[1].Value
    $appPortMatches = [regex]::Matches($appPortSection, '"(\d+):\d+"')
    $appPorts = $appPortMatches | ForEach-Object { $_.Groups[1].Value }
    Write-Host "2. Library: $appHost`:$appPorts"
    Write-Host "-------------------------------------------------------------"
    $promContent = Find-Content -content $сontent -expression "prometheus-script:[\s\S]*?ports:([\s\S]*?)(?=\s{2}\w|\z)"
    $promPortSection = $promContent.Groups[1].Value
    $promPortMatches = [regex]::Matches($promPortSection, '"(\d+):\d+"')
    $promPorts = $promPortMatches | ForEach-Object { $_.Groups[1].Value }
    Write-Host "3. Prometheus: $appHost`:$promPorts"
    Write-Host "-------------------------------------------------------------"
    $grafContent = Find-Content -content $сontent -expression "grafana-script:[\s\S]*?ports:([\s\S]*?)(?=\s{2}\w|\z)"
    $grafPortSection = $grafContent.Groups[1].Value
    $grafPortMatches = [regex]::Matches($grafPortSection, '"(\d+):\d+"')
    $grafPorts = $grafPortMatches | ForEach-Object { $_.Groups[1].Value }
    Write-Host "4. Grafana: $appHost`:$grafPorts"
    Write-Host "-------------------------------------------------------------"

    Write-Host "b. Back" -ForegroundColor Gray

    $selection = Read-Host "Select an option"

    if ($selection.ToLower() -eq "b")
    {
        return "back"
    }
}

function Get-UserRetryDecision
{
    param(
        [string]$tempComposePath,
        [string]$profiles
    )
    Write-Host ""
    $tryAgain = Read-Host "Would you like to try again with a different module? (y/n) [y]"

    if (-not ([string]::IsNullOrEmpty($tryAgain)) -and $tryAgain.ToLower() -eq "n")
    {
        return $false
    }
    elseif (-not ([string]::IsNullOrEmpty($tryAgain)) -and $tryAgain.ToLower() -eq "y")
    {
        Stop-Container -tempComposePath $tempComposePath -profiles $profiles
        return $true
    }

    return $false
}

function Stop-Container
{
    param(
        [string]$tempComposePath,
        [string]$profiles
    )

    if ( [string]::IsNullOrEmpty($tempComposePath))
    {
        #        Write-Host "No compose file path provided, skipping container stop" -ForegroundColor Yellow
        return
    }

    #    Write-Host "Stopping containers with: docker-compose -f `"$tempComposePath`" $profiles down" -ForegroundColor Yellow
    Invoke-Expression "docker-compose -f `"$tempComposePath`" $profiles down -v"

    if ($LASTEXITCODE -eq 0)
    {
        Write-Host "Containers stopped successfully!" -ForegroundColor Green
    }
    else
    {
        Write-Host "Failed to stop containers. See error messages above." -ForegroundColor Red
    }
}

function Test-MariaDbHealth {
    param (
        [string]$containerName = "mariadb-script",
        [int]$maxRetries = 20,
        [int]$waitSeconds = 5
    )

    #    Write-Host "Checking MariaDB health in container $containerName..." -ForegroundColor Yellow

    for ($i = 1; $i -le $maxRetries; $i++) {
        try {
            $containerExists = docker ps -q -f "name=$containerName"
            if (-not $containerExists) {
                Write-Host "Container $containerName does not exist or is not running. Retry $i/$maxRetries" -ForegroundColor Red
                Start-Sleep -Seconds $waitSeconds
                continue
            }
            # docker exec mariadb-script mysqladmin ping -h localhost
            #check directly
            $status = docker exec $containerName mysqladmin ping -h localhost 2>&1
            if ($status -match "mysqld is alive") {
                Write-Host "MariaDB is healthy and responding to pings!" -ForegroundColor Green
                return $true
            } else {
                Write-Host "MariaDB is not ready yet. Retry $i/$maxRetries" -ForegroundColor Yellow
            }
        } catch {
            Write-Host "Error checking MariaDB health: $_. Retry $i/$maxRetries" -ForegroundColor Red
        }

        Start-Sleep -Seconds $waitSeconds
    }

    Write-Host "MariaDB health check failed after $maxRetries retries." -ForegroundColor Red
    return $false
}
# Function to check Oracle DB health
function Test-OracleDbHealth {
    param (
        [string]$containerName = "oracledb-script",
        [int]$maxRetries = 60,  # Oracle needs more time to start
        [int]$waitSeconds = 10
    )

    Write-Host "Checking Oracle DB health in container $containerName..." -ForegroundColor Gray

    for ($i = 1; $i -le $maxRetries; $i++) {
        try {
            $containerExists = docker ps -q -f "name=$containerName"
            if (-not $containerExists) {
                Write-Host "Container $containerName does not exist or is not running. Retry $i/$maxRetries" -ForegroundColor Red
                Start-Sleep -Seconds $waitSeconds
                continue
            }

            # Check the health status reported by Docker
            $healthStatus = docker inspect --format='{{.State.Health.Status}}' $containerName 2>&1

            if ($healthStatus -eq "healthy") {
                Write-Host "Oracle DB is healthy according to Docker health check!" -ForegroundColor Green
                Write-Host ""
                return $true
            } else {
                Write-Host "Oracle DB is not ready yet (status: $healthStatus). Retry $i/$maxRetries" -ForegroundColor Yellow
                if ($i % 5 -eq 0) {  # Only show logs every 5 retries to avoid flooding the console
                    $healthLog = docker inspect --format='{{json .State.Health.Log}}' $containerName | ConvertFrom-Json
                    if ($healthLog.Length -gt 0) {
                        $lastCheck = $healthLog[-1]
                        if(-not [string]::IsNullOrWhiteSpace($lastCheck.Output))
                        {
                            Write-Host "Last health check output: $( $lastCheck.Output )" -ForegroundColor Gray
                        }
                        else
                        {
                            Write-Host "Oracle DB is initializing..." -ForegroundColor Yellow
                        }
                    }
                }
            }
        } catch {
            Write-Host "Error checking Oracle DB health: $_. Retry $i/$maxRetries" -ForegroundColor Red
        }

        Start-Sleep -Seconds $waitSeconds
    }

    Write-Host "Oracle DB health check failed after $maxRetries retries." -ForegroundColor Red
    return $false
}

function Start-Containers {
    param (
        [string]$composeFile,
        [string]$profileArg,
        [string[]]$serviceOrder
    )

    foreach ($service in $serviceOrder) {
        Write-Host ""
        Write-Host "Starting $service..." -ForegroundColor Gray
        #        Write-Host "docker-compose -f `"$composeFile`" $profileArg up -d $service" -ForegroundColor Yellow

        Invoke-Expression "docker-compose -f `"$composeFile`" $profileArg up -d $service"

        if ($LASTEXITCODE -ne 0) {
            Write-Host "Failed to start $service" -ForegroundColor Red
            return $false
        }

        $containerName = $service
        $isReady = $false
        Write-Host "Waiting for $containerName to be ready..." -ForegroundColor Yellow

        if ($containerName -match "mariadb-script") {
            $isReady = Test-MariaDbHealth -containerName $containerName
        }
        elseif ($containerName -match "oracledb-script") {
            $isReady = Test-OracleDbHealth -containerName $containerName
        }
        elseif ($containerName -match "node-red-script") {

            $containerStatus = docker inspect --format='{{.State.Status}}' $containerName 2>&1
            if ($containerStatus -eq "running") {
                Write-Host "Node-RED container is running" -ForegroundColor Green
                $isReady = $true
            }
        }
        else {
            $maxRetries = 10
            $waitSeconds = 5

            for ($i = 1; $i -le $maxRetries; $i++) {
                $containerStatus = docker inspect --format='{{.State.Status}}' $containerName 2>&1
                $containerHealth = docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' $containerName 2>&1

                if ($containerStatus -eq "running") {
                    if ($containerHealth -eq "healthy" -or $containerHealth -eq "running" -or $i -ge 10) {
                        $isReady = $true
                        Write-Host "$containerName is ready (Status: $containerStatus, Health: $containerHealth)" -ForegroundColor Green
                        break
                    }
                    Write-Host "Waiting for $containerName to be healthy... (Status: $containerStatus, Health: $containerHealth) Attempt $i/$maxRetries" -ForegroundColor Yellow
                } else {
                    Write-Host "Waiting for $containerName to start running... (Status: $containerStatus) Attempt $i/$maxRetries" -ForegroundColor Yellow
                }
                Start-Sleep -Seconds $waitSeconds
            }
        }

        if (-not $isReady) {
            Write-Host "Container $containerName did not become ready in time" -ForegroundColor Red
            return $false
        }

    }
    return $true
}
#------------------------------------------------------------------------------
function Stop-Containers
{
    param (
        [string]$composeFile,
        [string]$profileArg,
        [string[]]$serviceOrder
    )

    # Stop containers in reverse order
    $reverseOrder = $serviceOrder | Sort-Object { $serviceOrder.IndexOf($_) } -Descending

    foreach ($service in $reverseOrder)
    {
        Write-Host "Stopping $service..." -ForegroundColor Yellow
        #        Write-Host "docker-compose -f `"$composeFile`" $profileArg stop $service" -ForegroundColor Yellow

        Invoke-Expression "docker-compose -f `"$composeFile`" $profileArg stop $service"

        if ($LASTEXITCODE -ne 0)
        {
            Write-Host "Failed to stop $service" -ForegroundColor Red
            return $false
        }

        $containerName = $service
        $containerStatus = docker inspect --format='{{.State.Status}}' $containerName 2>&1

        if ($containerStatus -eq "exited")
        {
            Write-Host "$containerName stopped successfully" -ForegroundColor Green
        }
        else
        {
            Write-Host "Warning: $containerName may still be running (Status: $containerStatus)" -ForegroundColor Yellow
        }
    }

    return $true
}

function Get-FileContent {
    param([string]$filePath)
    if (Test-Path $filePath)
    {
        return Get-Content $FilePath -Raw
    }
    return $null
}
#Get the name of git branch function
function Get-GitBranch {
    param([string]$path = ".")

    try {
        #        Write-Host "Getting git branch for path: $Path" -ForegroundColor Yellow
        $resolvedPath = if (Test-Path $path) {
            (Get-Item $path).FullName
        } else {
            return $null
        }

        $gitDir = Join-Path $resolvedPath ".git"

        if (Test-Path $gitDir -PathType Leaf) {
            # .git is a file → worktree
            $content = Get-Content $gitDir -Raw -ErrorAction Stop
            if ($content -match 'gitdir:\s*(.+)') {
                $gitDirPath = $Matches[1].Trim()
                # Handle relative paths in worktree
                if (-not [System.IO.Path]::IsPathRooted($gitDirPath)) {
                    $gitDir = Join-Path $resolvedPath $gitDirPath
                } else {
                    $gitDir = $gitDirPath
                }
            }
        }

        $headFile = Join-Path $gitDir "HEAD"
        if (Test-Path $headFile) {
            $head = (Get-Content $headFile -Raw -ErrorAction Stop).Trim()
            if ($head -match '^ref:\s+refs/heads/(.+)$') {
                return $Matches[1]
            }
            # Return short hash for detached HEAD
            return "$($head.Substring(0, [Math]::Min(7, $head.Length))) (detached)"
        }

        return $null
    }
    catch {
        Write-Warning "Failed to get git branch: $($_.Exception.Message)"
        return $null
    }
}
# Function to get project path
function Get-ProjectPath {
    param([string]$pathFileName = "last_nodered_path.txt")
    $pathFile = Join-Path -Path $PSScriptRoot -ChildPath $pathFileName
    $savedPath = $null

    if (Test-Path $pathFile) {
        $savedPath = Get-Content -Path $pathFile -Raw
        $savedPath = $savedPath.Trim()
    }
    Clear-Host
    Write-Host ""
    Write-Host "Project Configuration" -ForegroundColor Cyan
    Write-Host "============================" -ForegroundColor Cyan

    if ($savedPath) {
        Write-Host "1. Use saved path: $savedPath"
        Write-Host "2. Enter a new path"
        Write-Host "b. Back"
        Write-Host ""
        $pathChoice = Read-Host "Select an option (1-2)"

        if ($pathChoice -eq "1") {

            return $savedPath
        }
        if ($pathChoice -eq "b")
        {
            return "back"
        }
    } else {
        Write-Host "No saved path found. Please enter the path to your project."
    }

    $newPath = Read-Host "Please enter the path to your application project"

    $newPath | Out-File -FilePath $pathFile -Force

    Write-Host "Path saved for future use: $newPath" -ForegroundColor Green
    return $newPath
}
# Function to monitor the status of integration tests in the container
function Watch-IntegrationTests
{
    param (
        [string]$containerName = "integration-tests",
        [int]$checkIntervalSeconds = 1
    )

    Write-Host "Monitoring integration tests in container $containerName..." -ForegroundColor Gray
    Write-Host ""
    $testCompleted = $false

    $startTime = Get-Date

    $testLogColor = "Gray"

    Write-Host "Live test output:" -ForegroundColor Gray
    Write-Host "----------------" -ForegroundColor Gray

    $lastLogTime = Get-Date -Format "yyyy-MM-ddTHH:mm:ss"

    while (-not $testCompleted) {

        $containerStatus = docker inspect --format='{{.State.Status}}' $containerName 2>$null
        $containerExitCode = docker inspect --format='{{.State.ExitCode}}' $containerName 2>$null

        if ($null -ne $containerExitCode -and $containerStatus -ne "running")
        {
            $testCompleted = $true
            if ($containerExitCode -eq 0) {
                Write-Host ""
                Write-Host "Integration tests completed successfully!" -ForegroundColor Green
                Write-Host ""
            } else {
                Write-Host ""
                Write-Host "Integration tests failed with exit code: $containerExitCode" -ForegroundColor Red
                Write-Host ""
            }
            break
        }

        $currentTime = Get-Date -Format "yyyy-MM-ddTHH:mm:ss"
        $logCommand = "docker logs --since `"$lastLogTime`" $containerName 2>&1"
        $logs = Invoke-Expression $logCommand
        $lastLogTime = $currentTime

        if ($logs) {
            foreach ($line in $logs) {
                if ($line -match "test" -or $line -match "gradle" -or
                        $line -match "BUILD" -or $line -match "Task" -or
                        $line -match "allure" -or $line -match "PASSED" -or
                        $line -match "FAILED" ) {

                    if ($line -match "BUILD SUCCESSFUL") {
                        Write-Host $line -ForegroundColor Green
                    } elseif ($line -match "BUILD FAILED") {
                        Write-Host $line -ForegroundColor Red
                        #                    } elseif ($line -match "allure") {
                        #                        Write-Host $line -ForegroundColor Cyan
                    } elseif ($line -match "Starting") {
                        Write-Host $line -ForegroundColor Gray
                    } else {
                        Write-Host $line -ForegroundColor $testLogColor
                    }
                }
            }
        }

        $runningTime = (Get-Date) - $startTime
        if ($runningTime.TotalMinutes -gt 60)
        {
            Write-Host "Warning: Tests have been running for over 60 minutes." -ForegroundColor Yellow
            $continueWaiting = Read-Host "Continue waiting? (y/n) [y]"
            if ($continueWaiting.ToLower() -eq "n") {
                break
            }
            $startTime = Get-Date
        }

        Start-Sleep -Seconds $checkIntervalSeconds
    }

    if ($testCompleted) {
        Write-Host "Final test results:" -ForegroundColor Gray
        $finalLogs = docker logs --tail 50 $containerName 2>&1 | Select-String -Pattern "BUILD|PASSED|FAILED|Gradle Test"
        foreach ($line in $finalLogs) {
            if ($line -match "FAILED") {
                Write-Host $line -ForegroundColor Red
            } elseif ($line -match "PASSED" -or $line -match "BUILD SUCCESSFUL") {
                Write-Host $line -ForegroundColor Green
            } elseif ($line -match "allure") {
                Write-Host $line -ForegroundColor Cyan
            } else {
                Write-Host $line -ForegroundColor Yellow
            }
        }
    }

    return $containerExitCode
}
# Function to check if allureServerGenerate task failed and display appropriate message
function Invoke-AllureReport
{
    param (
        [string]$buildDir = (Join-Path -Path (Get-Location).Path -ChildPath "integration-tests\build")
    )

    Write-Host "Checking for Allure report..." -ForegroundColor Gray

    $reportLocations = @{
        "ServerUrlFile" = (Join-Path -Path $buildDir -ChildPath "generated-report-url.txt");
        "UuidFile" = (Join-Path -Path $buildDir -ChildPath "uploaded-result-uuid.txt");
        "Index" = (Join-Path -Path $buildDir -ChildPath "allure-report/index.html");
        "HtmlReport" = (Join-Path -Path $buildDir -ChildPath "reports\allure-report\allureReport");
        "ResultsDir" = (Join-Path -Path $buildDir -ChildPath "allure-results");
    }
    #
    #    if (!(Test-Path $reportLocations.Index)) {
    #            Write-Host "ALLURE REPORT GENERATION FAILED " -ForegroundColor Yellow
    #            Write-Host "The Allure plugin or server could not be reached." -ForegroundColor Yellow
    #            Write-Host "Please ensure you are connected to the VPN to access the repository server and download dependencies." -ForegroundColor Yellow
    #            return $false
    #    }

    if ((Test-Path $reportLocations.ServerUrlFile) -and (Test-Path $reportLocations.UuidFile))
    {
        $reportUrl = Get-Content -Path $reportLocations.ServerUrlFile -Raw
        $reportUrl = $reportUrl.Trim()

        if ($reportUrl) {
            Write-Host "Found Allure server report URL: $reportUrl" -ForegroundColor Green

            $openReport = Read-Host "Would you like to open the test report in your browser? (y/n) [y]"
            if ([string]::IsNullOrEmpty($openReport) -or $openReport.ToLower() -eq "y") {
                Write-Host "Opening report in default browser..." -ForegroundColor Green
                Start-Process $reportUrl | Out-Null
                return $true
            }
            return $false
        }
    }

    Write-Host ""
    Write-Host "No Allure test reports found." -ForegroundColor Yellow
    Write-Host "If you expected a report, please check your VPN connection and try running the tests again." -ForegroundColor Yellow
    Write-Host ""
    return $false
}
# Function to check if Docker is actually working
function Test-DockerRunning {
    try {
        docker info 2>&1 | Out-Null
        return ($LASTEXITCODE -eq 0)
    } catch {
        return $false
    }
}
# Function to select a module from available options
function Select-Module {
    param (
        [array]$availableModules
    )

    Clear-Host
    Write-Host ""
    Write-Host "Module Selection" -ForegroundColor Cyan
    Write-Host "=========================" -ForegroundColor Cyan

    for ($i = 0; $i -lt $availableModules.Count; $i++) {
        Write-Host "$($i+1). $($availableModules[$i])"
    }

    Write-Host ""
    $moduleChoice = Read-Host "Select a module (1-$($availableModules.Count))"

    if ([int]::TryParse($moduleChoice, [ref]$null) -and [int]$moduleChoice -ge 1 -and [int]$moduleChoice -le $availableModules.Count) {
        $selectedModule = $availableModules[[int]$moduleChoice-1]
    } else {
        #        $selectedModule = $availableModules[0]
        Write-Host ""
        #        Write-Host "Invalid selection. Using default: $($availableModules[0])" -ForegroundColor Yellow
        continue
    }

    #    Write-Host "Selected module: $selectedModule" -ForegroundColor Gray
    return $selectedModule
}
# Function to handle Node-Red API submenu
function Show-NodeRedMenu {
    Clear-Host
    Write-Host ""
    Write-Host "Node-Red type selection" -ForegroundColor Cyan
    Write-Host "===========================" -ForegroundColor Cyan
    Write-Host "1. Actions"
    Write-Host "2. Flows"
    Write-Host "b. Back"

    Write-Host ""
    $typeChoice = Read-Host "Select Node-Red API type (1-2)"

    if ($typeChoice -eq "1")
    {
        $script:MODULES = "nodered-action-api"
        return "actions"
    }
    elseif ($typeChoice -eq "2")
    {
        $script:MODULES = "nodered-flow-api"
        return "flows"
    }
    elseif($typeChoice -eq "b")
    {
        return "back"
    }
    else
    {
        $script:MODULES = "nodered-action-api"
        return "actions"
    }
}

function Show-SubMenu
{
    param(
        [string]$module

    )
    Clear-Host
    Write-Host ""
    Write-Host "Selection" -ForegroundColor Cyan
    Write-Host "===========================" -ForegroundColor Cyan
    Write-Host "1. Deploy containers"
    Write-Host "b. Back"
    Write-Host ""
    $typeChoice = Read-Host "Select action (1-2)"

#    if ($typeChoice -eq "1")
#    {
#        return $false
#    }
#    else
    if ($typeChoice -eq "1")
    {
        return $true
    }
    elseif($typeChoice -eq "b")
    {
        return "back"
    }
    else
    {
        return $false
    }
}
# Function to handle container deployment
function Show-ContainerSelection
{
    param (
        [hashtable[]]$availableContainers = @()
    )
    #    # Display menu
    Clear-Host
    Write-Host ""
    Write-Host "Container Selection" -ForegroundColor Cyan
    Write-Host "==========================" -ForegroundColor Cyan
    #    Write-Host "0. Select All" -ForegroundColor Green

    for ($i = 0; $i -lt $availableContainers.Count; $i++) {
        Write-Host "$( $i + 1 ). $( $availableContainers[$i].Name )" -ForegroundColor Gray
    }
    Write-Host "b. Back" -ForegroundColor Gray

    Write-Host ""
    Write-Host "Enter selections (e.g., 13 or 0 for all): " -NoNewline
    $selection = Read-Host

    if ($selection.ToLower() -eq "b")
    {
        return "back"
    }

    $selectedContainers = @()
    $requiredProfiles = @()
    $serviceOrder = @()

    if ($selection -eq "0")
    {
        $selectedContainers = $availableContainers
    }
    else
    {
        $indices = $selection.ToCharArray() | ForEach-Object { [int]$_.ToString() - 1 }
        foreach ($index in $indices)
        {
            if ($index -ge 0 -and $index -lt $availableContainers.Count)
            {
                $selectedContainers += $availableContainers[$index]
            }
        }
    }

    foreach ($container in $selectedContainers)
    {
        $requiredProfiles += $container.Profile
        $serviceOrder += $container.Service
    }

    return @{
        RequiredProfiles = $requiredProfiles
        ServiceOrder = $serviceOrder
        SelectedContainers = $selectedContainers
    }
}
#Build image function for java
function New-DockerImage
{
    param(
        [Parameter(Mandatory = $true)]
        [string]$homeLocation,

        [Parameter(Mandatory = $true)]
        [string]$projectPath,

        [Parameter(Mandatory = $true)]
        [string]$jarPath,

        [Parameter(Mandatory = $true)]
        [string]$dockerfileName,

        [Parameter(Mandatory = $true)]
        [string]$ImageName,

        [string]$appPort = "1280"
    )

    try
    {
        Set-Location $projectPath

        if ($imageName -eq "library-custom" )
        {
            Clear-Host
            Write-Host "Removing old image..." -ForegroundColor Gray
            docker rmi library-custom:latest 2> $null
        }

        docker run --rm -v "${PWD}:/workspace" -w /workspace gradle:9.0.0-jdk24 gradle clean bootJar --no-daemon


        if ($LASTEXITCODE -ne 0)
        {
            throw "Gradle build failed"
        }

        $jarFile = Get-ChildItem -Path $jarPath -Filter "*.jar" | Select-Object -First 1
        if (-not $jarFile)
        {
            throw "No JAR file found in $jarPath"
        }
        Write-Host ""
        Write-Host "Creating Dockerfile..." -ForegroundColor Gray
        Write-Host ""
        @"
            FROM openjdk:24-jdk-slim
            WORKDIR /app
            COPY $jarPath/*.jar app.jar

            EXPOSE $appPort

            ENTRYPOINT ["java", "-jar", "app.jar"]
"@ | Out-File -FilePath $dockerfileName -Encoding UTF8

        docker build -f $dockerfileName -t $ImageName .
        if ($LASTEXITCODE -ne 0)
        {
            throw "Docker build failed"
        }
        Write-Host ""
        Write-Host "Image created: $ImageName" -ForegroundColor Green
    }
    finally
    {
        Set-Location $homeLocation
    }
}
#------------------------------------------------------
function Get-Containers
{
    param (
        [string]$module,
        [string]$nodeRedType = "",
        [string]$tempComposePath
    )

    $availableContainers = @()

    if ($module -eq "library")
    {
        $availableContainers = @(
            @{ Name = "postgres"; Profile = "postgres"; Service = "postgres-script" },
            @{ Name = "library"; Profile = "library"; Service = "library-script" },
            @{ Name = "prometheus"; Profile = "prometheus"; Service = "prometheus-script" },
            @{ Name = "grafana"; Profile = "grafana"; Service = "grafana-script" }
        )
    }

    $selectedContainers = Show-ContainerSelection -availableContainers $availableContainers

    if ($selectedContainers -eq "back")
    {
        return "back"
    }

    # Auto-add dependencies based on selected profiles
    $selectedProfiles = $selectedContainers.RequiredProfiles
    $servicesToAdd = @()
    $profilesToAdd = @()

    if ($selectedProfiles -contains "library" -and $selectedProfiles -notcontains "postgres")
    {
        $profilesToAdd += "postgres"
        $servicesToAdd += "postgres-script"
#        $profilesToAdd += "flyway"
#        $servicesToAdd += "flyway-script"
    }

    if ($selectedProfiles -contains "library" -and $selectedProfiles -notcontains "prometheus")
    {
        $profilesToAdd += "prometheus"
        $servicesToAdd += "prometheus-script"
    }

    if ($selectedProfiles -contains "library" -and $selectedProfiles -notcontains "grafana")
    {
        $profilesToAdd += "grafana"
        $servicesToAdd += "grafana-script"
    }

    if ($selectedProfiles -contains "grafana" -and $selectedProfiles -notcontains "prometheus")
    {
        $profilesToAdd += "prometheus"
        $servicesToAdd += "prometheus-script"
        $profilesToAdd += "postgres"
        $servicesToAdd += "postgres-script"
#        $profilesToAdd += "flyway"
#        $servicesToAdd += "flyway-script"
        $profilesToAdd += "library"
        $servicesToAdd += "library-script"
    }

    if ($selectedProfiles -contains "prometheus" -and $selectedProfiles -notcontains "grafana")
    {
        $profilesToAdd += "grafana"
        $servicesToAdd += "grafana-script"
        $profilesToAdd += "postgres"
        $servicesToAdd += "postgres-script"
#        $profilesToAdd += "flyway"
#        $servicesToAdd += "flyway-script"
        $profilesToAdd += "library"
        $servicesToAdd += "library-script"
    }

#    if ($selectedProfiles -contains "postgres" -and $selectedProfiles -notcontains "flyway")
#    {
#        $profilesToAdd += "flyway"
#        $servicesToAdd += "flyway-script"
#    }

    if ($profilesToAdd.Count -gt 0)
    {
        $selectedContainers.RequiredProfiles = $profilesToAdd + $selectedContainers.RequiredProfiles
        $selectedContainers.ServiceOrder = $servicesToAdd + $selectedContainers.ServiceOrder
    }

#    if ($selectedProfiles -contains "postgres" -and $selectedProfiles -notcontains "flyway")
#    {
#        Remove-DockerImages -ImageNames @("ghcr.io/burito2021/library_db:latest", "ghcr.io/burito2021/library_db-postgres:15")
#    }

    $requiredProfiles = $selectedContainers.RequiredProfiles
    $profilesArg = ($requiredProfiles | ForEach-Object { "--profile $_" }) -join " "
    $serviceOrder = $selectedContainers.ServiceOrder

    return @{
        ProfilesArg = $profilesArg
        RequiredProfiles = $requiredProfiles
        SelectedProfiles = $selectedProfiles
        ServiceOrder = $serviceOrder
    }
}

function Test-ProfileExists
{
    param (
        [string]$profileName,
        [string[]]$requiredProfiles
    )

    return $requiredProfiles -contains $profileName
}
#-------------docker related services functions (startup)
function Find-DockerDesktop
{
    $possiblePaths = @(
        "C:\Program Files\Docker\Docker\Docker Desktop.exe",
        "${env:ProgramFiles}\Docker\Docker\Docker Desktop.exe",
        "${env:LOCALAPPDATA}\Programs\Docker\Docker\Docker Desktop.exe",
        "${env:ProgramFiles(x86)}\Docker\Docker\Docker Desktop.exe"
    )

    foreach ($path in $possiblePaths)
    {
        if (Test-Path $path)
        {
            return $path
        }
    }
    return $null
}

function Start-Docker
{
    Write-Host "Docker is not running or not responding." -ForegroundColor Yellow

    $dockerDesktopPath = Find-DockerDesktop
    $timeout = 60
    $interval = 2

    if (Test-Path $dockerDesktopPath)
    {
        Write-Host "Docker Desktop found. Attempting to start Docker Desktop..." -ForegroundColor Yellow
        Start-Process -FilePath $dockerDesktopPath

        Write-Host "Waiting for Docker Desktop to start..." -ForegroundColor Yellow
        if (Wait-ForDocker -timeout $timeout -interval $interval)
        {
            Write-Host ""
            Write-Host "Docker Desktop started successfully!" -ForegroundColor Green
            return $true | Out-Null
        }

        Write-Host "Failed to start Docker Desktop within timeout period." -ForegroundColor Yellow
        Write-Host "Trying to start Docker service instead..." -ForegroundColor Yellow
    }
    else
    {
        Write-Host "Docker Desktop not found. Trying to start Docker service..." -ForegroundColor Yellow
    }

    return Start-DockerService -timeout $timeout -interval $interval
}

function Start-DockerService
{
    param([int]$timeout, [int]$interval)

    $dockerService = Get-Service -Name docker -ErrorAction SilentlyContinue
    if ($null -eq $dockerService)
    {
        Write-Host "Docker service not found. Please make sure Docker is installed properly." -ForegroundColor Red
        Start-Sleep -Seconds 5
        exit 1
    }

    Start-Service -Name docker

    if (Wait-ForDocker -timeout $timeout -interval $interval)
    {
        Write-Host "Docker service started successfully!" -ForegroundColor Green
        return $true
    }
    else
    {
        Write-Host "Failed to start Docker service within timeout period. Please start Docker manually." -ForegroundColor Red
        Start-Sleep -Seconds 5
        exit 1
    }
}

function Wait-ForDocker
{
    param([int]$timeout, [int]$interval)

    $timer = 0
    while (($timer -lt $timeout) -and (-not (Test-DockerRunning)))
    {
        Write-Host "Waiting for Docker to start... ($timer/$timeout seconds)" -ForegroundColor Yellow
        Start-Sleep -Seconds $interval
        $timer += $interval
    }

    return (Test-DockerRunning)
}
#Function sections ----------END--------------
# ------------------------------RUN THE SCRIPT-------------------------------------
Write-Host ""
Write-Host "Checking if Docker is running..." -ForegroundColor Gray
$dockerRunning = Test-DockerRunning

if ($dockerRunning)
{
    Write-Host ""
    Write-Host "Docker is already running and functional." -ForegroundColor Green
}
else
{
    Start-Docker
}

$script:composePath = ".\docker-compose-script.yml"
Write-Host ""
Write-Host "Using default file: $composePath" -ForegroundColor Gray

#--------------------------------------------------------------------------------------------
# Main script loop to allow reselecting modules
$runAgain = $true
while ($runAgain)
{
    if (-not (Test-Path $composePath))
    {
        Write-Host "Docker compose file not found at $composePath" -ForegroundColor Red
        Start-Sleep -Seconds 3
        exit 1
    }

    $AVAILABLE_MODULES = @("library", "info")

    if (-not $skipMenus)
    {
        $MODULE = Select-Module -availableModules $AVAILABLE_MODULES
    }

    $CONTAINER_DEPLOYMENT = $false
    $SELECTED_CONTAINERS = @()
    $PROFILES = @()
    $REQUIRED_PROFILES = @()
    $SERVICE_ORDER = @()
    $HOME_LOCATION = Get-Location
    $PROJECT_PATH = ($HOME_LOCATION).Path

    if ($MODULE -eq "info")
    {
        $result = Get-InfoMenu -composePath $composePath -homeLocation $HOME_LOCATION
        if ($result -eq "back")
        {
            continue
        }
    }

    if ($MODULE -eq "library")
    {
        $PROJECT_NAME = "library"
        $MODULES = "library"
        $CONTAINER_DEPLOYMENT = Show-SubMenu
        if ([string]$CONTAINER_DEPLOYMENT -eq "back")
        {
            $skipMenus = $false
            continue
        }

        if ($CONTAINER_DEPLOYMENT)
        {
            $SELECTED_CONTAINERS = Get-Containers -module $MODULE -nodeRedType $NODERED_TYPE -tempComposePath $tempComposePath
            if ($SELECTED_CONTAINERS -eq "back")
            {
                $skipMenus = $true
                continue
            }

            if ($SELECTED_CONTAINERS.SelectedProfiles -contains "postgres" -and $selectedProfiles -notcontains "flyway")
            {
                Remove-DockerImages -ImageNames @("ghcr.io/burito2021/library_db-postgres:15")
            }

            $PROFILES = $SELECTED_CONTAINERS.ProfilesArg
            $REQUIRED_PROFILES = $SELECTED_CONTAINERS.RequiredProfiles
            $SERVICE_ORDER = $SELECTED_CONTAINERS.ServiceOrder
        }

        if($REQUIRED_PROFILES -contains "library")
        {
            if (-not (Test-Path $PROJECT_PATH))
            {
                Write-Host "Error: Path '$PROJECT_PATH' does not exist!" -ForegroundColor Red
            }
            $CI_COMMIT_REF_NAME = Get-GitBranch -Path $PROJECT_PATH  #Get the branch name
            Clear-Host
            Write-Host ""
            Write-Host "Selected options" -ForegroundColor Cyan
            Write-Host "===========================" -ForegroundColor Cyan
            Write-Host "Selected module: $MODULE" -ForegroundColor Cyan
            Write-Host "Project name: $PROJECT_NAME" -ForegroundColor Cyan
            Write-Host "Branch: $CI_COMMIT_REF_NAME" -ForegroundColor Cyan
            Write-Host "===========================" -ForegroundColor Cyan
            Write-Host ""
            Start-Sleep -Seconds 5

            New-DockerImage -HomeLocation $HOME_LOCATION -ProjectPath $PROJECT_PATH -JarPath "build/libs" -DockerfileName "DockerFile-Library" -ImageName "library-custom"
        }
}

    #------------------ General flow-----------------------------------------------------
    $currentDir = ($HOME_LOCATION).Path

    $currentDirForDocker = $currentDir -replace '\\', '/'

    $tempComposePath = [System.IO.Path]::GetTempFileName() + ".yml"

    $composeContent = Get-Content -Path $composePath -Raw
    $composeContent = $composeContent -replace '\$\{CI_PROJECT_DIR\}', $currentDirForDocker

    $composeContent | Set-Content -Path $tempComposePath
    Write-Host ""
    Write-Host "Temporary compose file created at: $tempComposePath" -ForegroundColor Green
    Write-Host ""
    Write-Host "Starting containers for $MODULE..." -ForegroundColor Gray
    $success = Start-Containers -composeFile $tempComposePath -profileArg $PROFILES -serviceOrder $SERVICE_ORDER

    if ($success)
    {
        Write-Host ""
            Write-Host "Containers started successfully for module: $MODULE!" -ForegroundColor Green
    }

    if ($CONTAINER_DEPLOYMENT)
        {
            $skipMenus = $false
            $runAgain = $false
        }
}

if (Test-Path $tempComposePath) {
    Write-Host ""
    $keepRunning = Read-Host "Do you want to keep containers running? (y/n) [y]"
    Write-Host ""
    if (-not ([string]::IsNullOrEmpty($keepRunning)) -and $keepRunning.ToLower() -eq "n") {

        Stop-Container -tempComposePath $tempComposePath -profiles $PROFILES

    } else {
        Write-Host "Containers will continue running." -ForegroundColor Green
    }
    Write-Host ""
    Write-Host "Cleaning up temporary compose file..." -ForegroundColor Gray
    Remove-Item -Path $tempComposePath -Force
}
Write-Host ""
Write-Host "Script will close in 1 seconds..." -ForegroundColor Gray
Start-Sleep -Seconds 1

# White logs info
# Yellow warn
# Green success
# Red fail
# Cyan Config info  and setup