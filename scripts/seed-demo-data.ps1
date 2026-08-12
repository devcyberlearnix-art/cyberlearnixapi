param(
    [string]$Container = "cyberlearnix-postgres",
    [string]$DatabaseUser = "cyberlearnix",
    [switch]$SkipImageCheck
)

$ErrorActionPreference = "Stop"
$scriptRoot = Join-Path $PSScriptRoot "demo-data"

if (-not (docker ps --format "{{.Names}}" | Select-String -SimpleMatch $Container)) {
    throw "PostgreSQL container '$Container' is not running."
}

$imageUrls = @(
    "https://images.unsplash.com/photo-1526379095098-d400fd0bf935?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1555949963-ff9fe0c870eb?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1461749280684-dccba630e2f6?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1547658719-da2b51169166?auto=format&fit=crop&w=1200&q=80"
)

if (-not $SkipImageCheck) {
    Write-Host "Checking remote demo images..."
    foreach ($url in $imageUrls) {
        $response = Invoke-WebRequest -UseBasicParsing -Method Head -Uri $url -TimeoutSec 20
        if ($response.StatusCode -ne 200) {
            throw "Image is unavailable: $url"
        }
    }
}

$seeds = @(
    @{ Database = "lms_user_db"; File = "seed-users.sql" },
    @{ Database = "lms_instructor_db"; File = "seed-instructors.sql" },
    @{ Database = "lms_course_db"; File = "seed-courses.sql" }
)

foreach ($seed in $seeds) {
    $path = Join-Path $scriptRoot $seed.File
    Write-Host "Loading $($seed.File) into $($seed.Database)..."
    Get-Content -Raw $path | docker exec -i $Container psql -v ON_ERROR_STOP=1 -U $DatabaseUser -d $seed.Database
    if ($LASTEXITCODE -ne 0) {
        throw "Failed loading $($seed.File)."
    }
}

Write-Host "Demo data loaded successfully."
Write-Host "Students: maya.student@merqora.com, arjun.student@merqora.com, sofia.student@merqora.com, liam.student@merqora.com"
Write-Host "Instructors: priya.instructor@merqora.com, daniel.instructor@merqora.com"
Write-Host "Password for all demo accounts: Demo@12345"