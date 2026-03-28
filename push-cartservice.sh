#!/bin/bash

# Define variables
ACR_NAME="swachvegaregistry"
IMAGE_NAME="cartservice"
TAG="latest"
FULL_IMAGE_NAME="${ACR_NAME}.azurecr.io/${IMAGE_NAME}:${TAG}"

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SEARCH_DIR="$SCRIPT_DIR"
GRADLEW_PATH=""
GRADLEW_DIR=""

while [ "$SEARCH_DIR" != "/" ]; do
  if [ -f "$SEARCH_DIR/gradlew" ]; then
    GRADLEW_PATH="$SEARCH_DIR/gradlew"
    GRADLEW_DIR="$SEARCH_DIR"
    break
  fi
  SEARCH_DIR="$(dirname "$SEARCH_DIR")"
done

if [ -n "$GRADLEW_PATH" ]; then
  echo "🧱 Building JAR using Gradle wrapper at: $GRADLEW_PATH"
  "$GRADLEW_PATH" -p "$GRADLEW_DIR" :cartservice:clean :cartservice:bootJar
else
  echo "❌ Gradle wrapper not found. Run from repo root (where gradlew exists) or install Gradle and build manually."
  exit 1
fi

# Step 1: Build the image
echo "🔧 Building Docker image..."
docker build --platform=linux/amd64 -t ${IMAGE_NAME}:${TAG} .

# Step 2: Tag the image with the ACR name
echo "🏷️ Tagging image as ${FULL_IMAGE_NAME}..."
docker tag ${IMAGE_NAME}:${TAG} ${FULL_IMAGE_NAME}

# Step 3: Log in to Azure Container Registry
echo "🔐 Logging into Azure Container Registry..."
az acr login --name ${ACR_NAME} --username ${ACR_NAME}

# Step 4: Push the image to ACR
echo "🚀 Pushing image to ACR..."
docker push ${FULL_IMAGE_NAME}

echo "✅ Successfully pushed ${FULL_IMAGE_NAME} to Azure Container Registry!"
