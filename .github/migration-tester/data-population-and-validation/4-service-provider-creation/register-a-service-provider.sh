#!/bin/bash

# Define colors
RED='\033[0;31m'
GREEN='\033[1;38;5;206m'
BOLD='\033[1m'
PURPLE='\033[1;35m'
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

# Register the service provider
response=$(curl -k --location --request POST 'https://localhost:9443/api/identity/oauth2/dcr/v1.1/register' \
    --header 'Authorization: Basic YWRtaW46YWRtaW4=' \
    --header 'Content-Type: application/json' \
    --data-raw '{  "client_name": "testMigrationApp", "grant_types": ["authorization_code","implicit","password","client_credentials","refresh_token"], "redirect_uris":["http://localhost:8080/playground2"] }')

# Check if the registration was successful
if echo "$response" | grep -q '"client_name":'; then
    # Print service provider details
    echo -e "${PURPLE}${BOLD}A new service provider has been registered. Service provider name is test migration app${NC}"
else
    # Print failure message
    echo "${RED}${BOLD}Failed to register the service provider.${NC}"
    # Print error details
    echo "Error Details:"
    echo "$response"
fi
echo

