Write-Host "Starting order-service..."
# Start the service in the background
Start-Process -FilePath "cmd.exe" -ArgumentList "/c","..\gradlew.bat :order-service:bootRun" -NoNewWindow
# Wait for health endpoint
$max=60; $elapsed=0
while ($elapsed -lt $max) {
  try {
    $h = Invoke-RestMethod http://localhost:8084/actuator/health -UseBasicParsing
    if ($h.status -eq 'UP') { break }
  } catch { }
  Start-Sleep -Seconds 2
  $elapsed += 2
}
if ($h.status -ne 'UP') { Write-Host "Service not healthy"; exit 1 }
Write-Host "Service is UP. Running API tests..."
function Test-Api { param([string]$Method,[string]$Url,$Body=$null) try { $resp = if($Body){ Invoke-RestMethod -Method $Method -Uri $Url -Body ($Body|ConvertTo-Json) -ContentType "application/json" } else { Invoke-RestMethod -Method $Method -Uri $Url } Write-Host "`n=== $Method ${Url} ==="; $resp | ConvertTo-Json -Depth 5; return $resp } catch { Write-Host "Error $Method ${Url}: $_"; return $null } }
# Create order
$create = @{ userId='test-user'; courseIds=@('course-1','course-2'); couponCode='WELCOME10' }
$created = Test-Api POST "http://localhost:8084/orders/create" $create
$orderId = $created?.orderId
if (-not $orderId) { Write-Host "Create failed"; exit 1 }
# Get order
Test-Api GET "http://localhost:8084/orders/$orderId"
# List by user
Test-Api GET "http://localhost:8084/orders/user/test-user"
# Update status
Test-Api PUT "http://localhost:8084/orders/$orderId/status?status=COMPLETED"
# Refund order
Test-Api POST "http://localhost:8084/orders/$orderId/refund"
# Cancel a new order
$cancel = @{ userId='test-user2'; courseIds=@('course-3') }
$new = Test-Api POST "http://localhost:8084/orders/create" $cancel
$newId = $new?.orderId
if ($newId) { Test-Api DELETE "http://localhost:8084/orders/$newId/cancel" }
Write-Host "API test completed."
