#!/bin/bash

# Function to clean up a file by removing excessive blank lines
cleanup_file() {
    local file="$1"
    if [ -f "$file" ]; then
        # Use sed to:
        # 1. Remove leading blank lines
        # 2. Remove trailing blank lines
        # 3. Replace 2+ consecutive blank lines with a single blank line
        sed -i ':a;N;$!ba;s/\n\s*\n\s*\n/\n\n/g; /^$/d; $d' "$file"
        echo "Cleaned: $file"
    fi
}

# Clean YAML files
cleanup_file "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\API_DOCUMENTATION.yml"
cleanup_file "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\AUTH_ADMIN_APIs.yml"
cleanup_file "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\api-gateway\compose.yml"
cleanup_file "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\api-gateway\src\main\resources\application-local.yml"
cleanup_file "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\api-gateway\src\main\resources\application-production.yml"
cleanup_file "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\api-gateway\src\main\resources\application.yml"
cleanup_file "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\api-gateway\src\test\resources\application-test.yml"
cleanup_file "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\docker-compose.kafka.yml"
cleanup_file "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\docker\compose.yml"
cleanup_file "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\instructor-service\docker\compose.yml"
cleanup_file "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\review-service\src\main\resources\application.yml"
cleanup_file "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\wishlist-service\src\main\resources\application.yaml"

echo "YAML files cleaned up!"
