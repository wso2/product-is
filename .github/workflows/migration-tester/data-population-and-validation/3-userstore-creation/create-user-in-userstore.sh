#!/bin/bash

# Define colors
RED='\033[0;31m'
GREEN='\033[1;38;5;206m'
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

# Create the user store
user_store_response=$(curl -k --location --request POST "https://localhost:9443/api/server/v1/userstores" \
    --header 'Content-Type: application/json' \
    --header 'Authorization: Basic YWRtaW46YWRtaW4=' \
    --data-raw '{
    "typeId": "VW5pcXVlSURKREJDVXNlclN0b3JlTWFuYWdlcg==",
    
    "description": "Sample JDBC user store to add.",
    "name": "Testuserstore",
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

if [ -n "$user_store_response" ]; then
    echo -e "${PURPLE}${BOLD}User store 'Testuserstore' has been created successfully.${NC}"
    echo -e "${PURPLE}${BOLD}User Store Response:${NC}"
    echo "$user_store_response"
fi

user_store_response=$(curl -k -X 'POST' \
  'https://localhost:9443/t/carbon.super/scim2/Users' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "typeId": "VW5pcXVlSURKREJDVXNlclN0b3JlTWFuYWdlcg",
  "description": "Some description about the user store.",
  "name": "UniqueIDJDBCUserStoreManager",
  "properties": [
    {
      "name": "some property name",
      "value": "some property value"
    }
  ]
}')

echo "$user_store_response"
 echo -e "${PURPLE}${BOLD} A UniqueIDJDBCUserStore has been created successfully${NC}"

# Create a user in the userstore
user_store_response=$(curl -k --location --request POST "https://localhost:9443/scim2/Users" \
    --header 'Content-Type: application/json' \
    --header 'Authorization: Basic YWRtaW46YWRtaW4=' \
    --data-raw '{
        "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
        "userName": "JayanaG",
        "password": "jayanapassword",
        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User": {
            "employeeNumber": "000111",
            "costCenter": "111111",
            "organization": "WSO2Org",
            "division": "Engineering",
            "department": "Integration",
            "manager": {
                "managerId": "111000",
                "displayName": "Jayana"
            }
        }
    }')

echo -e "${PURPLE}${BOLD}A user has been created in the userstore successfully.${NC}"
echo -e "${PURPLE}${BOLD}User Creation Response${NC}: ""$user_store_response"
echo "user added"
