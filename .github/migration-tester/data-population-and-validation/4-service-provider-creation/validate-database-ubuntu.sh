#!/bin/bash

# Define colors
RED='\033[0;31m'
GREEN='\033[1;38;5;206m'
YELLOW='\033[0;33m'
PURPLE='\033[1;35m'
BOLD='\033[1m'
NC='\033[0m' # No Color

os=$1

# Set deployment file and path based on OS
if [ "$os" = "ubuntu-latest" ]; then

  chmod +x env.sh
  . "/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation/env.sh"
  echo "${GREEN}==> Env file for Ubuntu sourced successfully"
fi
if [ "$os" = "macos-latest" ]; then

  chmod +x env.sh
  source "/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation/env.sh"
  echo "${GREEN}==> Env file for Mac sourced successfully${RESET}"

fi

# Get the directory of the script
script_dir="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/data-population-and-validation/4-service-provider-creation"

# Load client_id and client_secret from file
if [ -f "$script_dir/client_credentials" ]; then
  . "$script_dir/client_credentials"
else
  echo -e "${RED}${BOLD}Error: client_credentials file not found.${NC}"
  exit 1
fi

# Echo client ID and client secret
echo -e "${YELLOW}Client ID: $client_id${NC}"
echo -e "${YELLOW}Client Secret: $client_secret${NC}"

# Encode client_id:client_secret as base64
base64_encoded=$(echo -n "$client_id:$client_secret" | base64)

# Get access token
echo -e "${PURPLE}${BOLD}Getting access token...${NC}"
token_response=$(curl -ks -X POST https://localhost:9443/oauth2/token \
  -H "Authorization: Basic $base64_encoded" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&username=admin&password=admin&scope=somescope_password')

# Print token response
echo "Token Response: $token_response"

# Check if the token response contains any error message
if echo "$token_response" | grep -q '"error":'; then
  # If there is an error, print the failure message with the error description
  error_description=$(echo "$token_response" | jq -r '.error_description')
  echo -e "${RED}${BOLD}Database validation failed: $error_description${NC}"
  exit 1
fi

# Extract access token and refresh token from response
access_token=$(echo "$token_response" | jq -r '.access_token')
refresh_token=$(echo "$token_response" | jq -r '.refresh_token')

if [ "$access_token" != "null" ]; then
  # Print success message
  echo -e "${PURPLE}${BOLD}An access token generated successfully.${NC}"
  # Print access token
  echo -e "Access Token: ${PURPLE}$access_token${NC}"

  # Store access token in the file
  if grep -q "access_token" "$script_dir/client_credentials"; then
    sed -i "s/access_token=.*/access_token=$access_token/" "$script_dir/client_credentials"
  else
    echo "access_token=$access_token" >>"$script_dir/client_credentials"
  fi
else
  echo -e "Access Token: null"
fi

if [ "$refresh_token" != "null" ]; then
  # Print success message
  echo -e "${PURPLE}${BOLD}A refresh token generated successfully.${NC}"
  # Print refresh token
  echo -e "Refresh Token: ${PURPLE}$refresh_token${NC}"

  # Store refresh token in the file
  if grep -q "refresh_token" "$script_dir/client_credentials"; then
    sed -i "s/refresh_token=.*/refresh_token=$refresh_token/" "$script_dir/client_credentials"
  else
    echo "refresh_token=$refresh_token" >>"$script_dir/client_credentials"
  fi
else
  echo -e "Refresh Token: null"
fi

# test database connection
#set -e
#RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
#  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
#  "https://${HOSTNAME}:${PORT}/api/server/v1/datasources") || exit 1

# check if database connection is successful
#if [ "${RESPONSE_CODE}" -eq 200 ]; then
#  echo "Database validation successful."
#  exit 0
#else
#  echo "Database validation failed with response code: ${RESPONSE_CODE}"
#  exit 1
#fi

# Validate the database
#curl -k -H "Authorization: Bearer $access_token" -H 'Content-Type: application/json' -X GET https://localhost:9443/api/identity/#oauth2/dcr/v1.1/register/$client_id
