param(
    [string]$Container = "cyberlearnix-postgres",
    [string]$DatabaseUser = "cyberlearnix"
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")

if (-not (docker ps --format "{{.Names}}" | Select-String -SimpleMatch $Container)) {
    throw "PostgreSQL container '$Container' is not running. Please start docker services first."
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Seeding Permanent LMS Data into PostgreSQL Microservices " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$seeds = @(
    @{ Database = "lms_user_db";         File = "seed_permanent_users.sql" },
    @{ Database = "lms_instructor_db";   File = "seed_permanent_instructors.sql" },
    @{ Database = "lms_course_db";       File = "seed_permanent_courses.sql" },
    @{ Database = "lms_course_db";       File = "seed_permanent_enrollments.sql" },
    @{ Database = "lms_course_db";       File = "seed_permanent_progress.sql" },
    @{ Database = "lms_order_db";        File = "seed_permanent_orders.sql" },
    @{ Database = "lms_payment_db";      File = "seed_permanent_payments.sql" },
    @{ Database = "lms_review_db";       File = "seed_permanent_reviews.sql" },
    @{ Database = "lms_coupon_db";       File = "seed_permanent_coupons.sql" },
    @{ Database = "lms_cart_db";         File = "seed_permanent_carts.sql" },
    @{ Database = "lms_wishlist_db";     File = "seed_permanent_wishlists.sql" },
    @{ Database = "lms_notification_db"; File = "seed_permanent_notifications.sql" }
)

foreach ($seed in $seeds) {
    $path = Join-Path $root $seed.File
    if (Test-Path $path) {
        Write-Host "Ingesting $($seed.File) -> $($seed.Database)..." -ForegroundColor Yellow
        Get-Content -Raw $path | docker exec -i $Container psql -U $DatabaseUser -d $seed.Database 2>$null
        Write-Host "  -> Success: $($seed.File)" -ForegroundColor Green
    } else {
        Write-Host "  -> Warning: File not found: $path" -ForegroundColor DarkYellow
    }
}

Write-Host "`nVerifying Database Record Counts:" -ForegroundColor Cyan
$checks = @(
    @{ Database = "lms_order_db"; Table = "orders" },
    @{ Database = "lms_order_db"; Table = "order_item" },
    @{ Database = "lms_user_db"; Table = "users" },
    @{ Database = "lms_course_db"; Table = "courses" },
    @{ Database = "lms_payment_db"; Table = "payments" }
)

foreach ($chk in $checks) {
    $cnt = docker exec -i $Container psql -U $DatabaseUser -d $chk.Database -t -c "SELECT COUNT(*) FROM $($chk.Table);" 2>$null
    $trimmed = if ($cnt) { $cnt.Trim() } else { "0" }
    Write-Host "  $($chk.Database).$($chk.Table): $trimmed rows" -ForegroundColor White
}

Write-Host "Permanent LMS data successfully loaded and verified!" -ForegroundColor Green

