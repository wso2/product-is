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

# Create the userstore in Identity Server
response=$(curl -k --location --request POST "https://localhost:9443/api/server/v1/userstores" \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Basic YWRtaW46YWRtaW4=' \
  --data-raw '{
    "typeId": "VW5pcXVlSURKREJDVXNlclN0b3JlTWFuYWdlcg==",
    "description": "Sample JDBC user store to add.",
    "name": "'"$USERSTORE_NAME"'",
    "properties": [
      {
        "name": "userName",
        "value": "testdb"
      },
      {
        "name": "password",
        "value": "testdb"
      },
      {
        "name": "driverName",
        "value": "com.mysql.jdbc.Driver"
      },
      {
        "name": "url",
        "value": "jdbc:mysql://localhost:3306/testdb?useSSL=false&amp;allowPublicKeyRetrieval=true"
      },
      {
        "name": "disabled",
        "value": "false"
      }
    ]
  }')

# Print the response
echo -e "Response:\n$response"

# Check if the response contains any error message
if echo "$response" | grep -q '"typeId":'; then
  # If there is no error, print the success message
  echo -e "${PURPLE}${BOLD}A userstore has been created.Userstore name is ${USERSTORE_NAME}${NC}"
  # Print the additional details
  echo -e "Userstore Name: ${PURPLE}$USERSTORE_NAME${NC}"
  echo -e "Userstore Description: ${PURPLE}Sample JDBC user store to add.${NC}"
  echo -e "Userstore Properties:"
  echo -e "  - Property: userName, Value: ${PURPLE}testdb${NC}"
  echo -e "  - Property: password, Value: ${PURPLE}testdb${NC}"
  echo -e "  - Property: driverName, Value: ${PURPLE}com.mysql.jdbc.Driver${NC}"
  echo -e "  - Property: url, Value: ${PURPLE}jdbc:mysql://localhost:3306/testdb?useSSL=false&amp;allowPublicKeyRetrieval=true${NC}"
  echo -e "  - Property: disabled, Value: ${PURPLE}false${NC}"
else
  # If there is an error, print the failure message with the error description
  error_description=$(echo "$response" | jq -r '.error[0].description')
  echo -e "${RED}${BOLD}Failure: $error_description${NC}"
fi
