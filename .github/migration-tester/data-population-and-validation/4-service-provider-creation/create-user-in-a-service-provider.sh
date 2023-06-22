#!/bin/bash

# Define colors
GREEN='\033[1;38;5;206m'
RED='\033[0;31m'
BOLD='\033[1m'
PURPLE='\033[1;35m'
NC='\033[0m' # No Color

os=$1

# Set deployment file and path based on OS
if [ "$os" = "ubuntu-latest" ]; then
  chmod +x env.sh
  . "/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation/env.sh"
  echo -e "${GREEN}==> Env file for Ubuntu sourced successfully"
fi

if [ "$os" = "macos-latest" ]; then
  chmod +x env.sh
  source "/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation/env.sh"
  echo -e "${GREEN}==> Env file for Mac sourced successfully${RESET}"
fi

# Create a user in the service provider
response=$(curl -k --location --request POST "https://localhost:9443/scim2/Users" \
    --header 'Authorization: Basic YWRtaW46YWRtaW4=' \
    --header 'Content-Type: application/json' \
    --data-raw '{
        "schemas": [],
        "name": {
            "givenName": "'"$SP_USER_NAME"'",
            "familyName": "'"$SP_USER_FAMILY_NAME"'"
        },
        "userName": "lanka",
        "password": "'"$SP_USER_PASSWORD"'",
        "emails": [
            {
                "type": "home",
                "value": "'"$SP_USER_HOME_EMAIL"'",
                "primary": true
            },
            {
                "type": "work",
                "value": "'"$SP_USER_WORK_EMAIL"'"
            }
        ],
        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User": {
            "employeeNumber": "1234A",
            "manager": {
                "value": "Taylor"
            }
        }
    }')

echo -e "${PURPLE}${BOLD}Response${NC}: $response " 

