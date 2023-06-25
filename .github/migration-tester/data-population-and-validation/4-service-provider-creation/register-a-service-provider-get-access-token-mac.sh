#!/bin/bash

# Define colors
RED='\033[0;31m'
GREEN='\033[1;38;5;206m'
PURPLE='\033[1;35m'
BOLD='\033[1m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

os=$1

# Set deployment file and path based on OS
if [ "$os" = "ubuntu-latest" ]; then

  chmod +x env.sh
  . "/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/env.sh"
  echo "${GREEN}==> Env file for Ubuntu sourced successfully"
fi
if [ "$os" = "macos-latest" ]; then

  chmod +x env.sh
  source "/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/env.sh"
  echo "${GREEN}==> Env file for Mac sourced successfully${RESET}"

fi

# Register service provider
response=$(curl -k --location --request POST 'https://localhost:9443/api/identity/oauth2/dcr/v1.1/register' \
  --header 'Authorization: Basic YWRtaW46YWRtaW4=' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "client_name": "Migration Application",
    "grant_types": ["authorization_code","implicit","password","client_credentials","refresh_token"],
    "redirect_uris":["http://localhost:8080/playground2"]
  }')

# Check if the response contains any error message
if echo "$response" | grep -q '"error":'; then
  # If there is an error, print the failure message with the error description
  error_description=$(echo "$response" | jq -r '.error_description')
  echo -e "${RED}${BOLD}Failure: $error_description${NC}"
  exit 1
else
  # If successful, print additional details in purple
  client_name=$(echo "$response" | jq -r '.client_name')
  echo "Response: $response${NC}"
  echo -e "${PURPLE}Service provider '$client_name' registered successfully${NC}"
  echo
fi

# Extract client_id and client_secret
client_id=$(echo "$response" | jq -r '.client_id')
client_secret=$(echo "$response" | jq -r '.client_secret')

# Store client_id and client_secret in a file
client_credentials_file="/Users/runner/work/product-is/product-is/.github/migration-tester/data-population-and-validation/4-service-provider-creation/client_credentials"

if [ -f "$client_credentials_file" ]; then
  echo "client_id=$client_id" >>"$client_credentials_file"
  echo "client_secret=$client_secret" >> "$client_credentials_file"
else
  echo "client_id=$client_id" >"$client_credentials_file"
  echo "client_secret=$client_secret" >> "$client_credentials_file"
fi

# Print client_id and client_secret
echo -e "Client ID: ${PURPLE}$client_id${NC}"
echo -e "Client Secret: ${PURPLE}$client_secret${NC}"

# Encode client_id:client_secret as base64
base64_encoded=$(echo -n "$client_id:$client_secret" | base64)

# Get access token
access_token_response=$(curl -k -X POST https://localhost:9443/oauth2/token \
  -H "Authorization: Basic $base64_encoded" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&username=admin&password=admin&scope=somescope_password')

# Check if the access token response contains any error message
if echo "$access_token_response" | grep -q '"error":'; then
  # If there is an error, print the failure message with the error description
  error_description=$(echo "$access_token_response" | jq -r '.error_description')
  echo -e "${RED}${BOLD}Failure: $error_description${NC}"
  exit 1
else
  # If successful, print the success message and additional details in purple
  access_token=$(echo "$access_token_response" | jq -r '.access_token')

  # Store access token in a file
  echo "access_token=$access_token" >> "$client_credentials_file"

  # Print success message
  echo -e "${PURPLE}${BOLD}Access token obtained successfully from the registered service provider.${NC}"

  # Print additional details
  echo -e "${PURPLE}${BOLD}Additional Details:${NC}"
  echo -e "Access Token: ${PURPLE}$access_token${NC}"
  echo
fi
