#!/bin/bash

# Define colors
RED='\033[0;31m'
GREEN='\033[1;38;5;206m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Load client_id and client_secret from file
if [ -f "client_credentials" ]; then
  . /home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/data-population-and-validation/4-service-provider-creation/client_credentials
  
else
  echo "${RED}Error: client_credentials file not found.${NC}"
  exit 1
fi

# Encode client_id:client_secret as base64
base64_encoded=$(echo -n "$client_id:$client_secret" | base64)

# Get access token
echo "${YELLOW}Getting access token...${NC}"
token_response=$(curl -ks -X POST https://localhost:9443/oauth2/token \
  -H "Authorization: Basic $base64_encoded" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&username=admin&password=admin&scope=somescope_password')

# Extract access token and refresh token from response
access_token=$(echo $token_response | jq -r '.access_token')
refresh_token=$(echo $token_response | jq -r '.refresh_token')

if [ -n "$access_token" ] && [ -n "$refresh_token" ]; then
  # Print access token and refresh token
  if [ "$access_token" != "null" ]; then
    echo "Access token: \033[31m$access_token\033[0m"
  fi
  if [ "$refresh_token" != "null" ]; then
    echo "Refresh token: \033[31m$refresh_token\033[0m"
  fi

  # Store access token and refresh token in a file
  if grep -q "access_token" client_credentials; then
    sed -i "s/access_token=.*/access_token=$access_token/" client_credentials
  else
    echo "access_token=$access_token" >> client_credentials
  fi

  if grep -q "refresh_token" client_credentials; then
    sed -i "s/refresh_token=.*/refresh_token=$refresh_token/" client_credentials
  else
    echo "refresh_token=$refresh_token" >> client_credentials
  fi

  # Print success message
  echo "${GREEN}An access token and a refresh token generated successfully.${NC}"
else
  echo "${GREEN}Database validation failed.${NC}"
fi
