param(
    [string]$Container = "cyberlearnix-postgres",
    [string]$DatabaseUser = "cyberlearnix"
)

$ErrorActionPreference = "Stop"
$scriptRoot = $PSScriptRoot

if (-not (docker ps --format "{{.Names}}" | Select-String -SimpleMatch $Container)) {
    throw "PostgreSQL container '$Container' is not running."
}

Write-Host "Generating test data SQL files using Python..." -ForegroundColor Yellow
python (Join-Path $scriptRoot "generate_test_data.py")
if ($LASTEXITCODE -ne 0) {
    throw "Failed generating test data SQL."
}

$seeds = @(
    @{ Database = "lms_user_db"; File = "seed_test_users.sql" },
    @{ Database = "lms_instructor_db"; File = "seed_test_instructors.sql" },
    @{ Database = "lms_course_db"; File = "seed_test_courses.sql" },
    @{ Database = "lms_course_db"; File = "seed_test_enrollments.sql" },
    @{ Database = "lms_wishlist_db"; File = "seed_test_wishlists.sql" },
    @{ Database = "lms_cart_db"; File = "seed_test_carts.sql" },
    @{ Database = "lms_review_db"; File = "seed_test_reviews.sql" },
    @{ Database = "lms_coupon_db"; File = "seed_test_coupons.sql" },
    @{ Database = "lms_order_db"; File = "seed_test_orders.sql" },
    @{ Database = "lms_payment_db"; File = "seed_test_payments.sql" },
    @{ Database = "lms_notification_db"; File = "seed_test_notifications.sql" }
)

foreach ($seed in $seeds) {
    $path = Join-Path (Get-Location) $seed.File
    if (Test-Path $path) {
        Write-Host "Loading $($seed.File) into $($seed.Database)..." -ForegroundColor Cyan
        Get-Content -Raw $path | docker exec -i $Container psql -v ON_ERROR_STOP=1 -U $DatabaseUser -d $seed.Database
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Failed loading $($seed.File)." -ForegroundColor Red
            continue
        }
    } else {
        Write-Host "Warning: $($seed.File) not found." -ForegroundColor Yellow
    }
}

Write-Host "Test data loaded successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Admin: dev.cyberlearnix@gmail.com (MainAdmin@123)"
Write-Host "Students: student01@cyberlearnix.test to student20@cyberlearnix.test (Password13!)"
Write-Host "Instructors: instructor01@cyberlearnix.test to instructor05@cyberlearnix.test (Password13!)"
