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
  . "/home/runner/work/product-is/product-is/.github/migration-tester/migration-automation/env.sh"
  echo "${GREEN}==> Env file for Ubuntu sourced successfully"
fi
if [ "$os" = "macos-latest" ]; then

  chmod +x env.sh
  source "/Users/runner/work/product-is/product-is/.github/migration-tester/migration-automation/env.sh"
  echo "${GREEN}==> Env file for Mac sourced successfully${RESET}"

fi

response=$(curl -k --location --request POST "$SCIM_BULK_EP" \
    --header 'Content-Type: application/scim+json' \
    --header 'Authorization: Basic YWRtaW46YWRtaW4=' \
    --data-raw '{
  "failOnErrors": 1,
  "schemas": ["urn:ietf:params:scim:api:messages:2.0:BulkRequest"],
  "Operations": [
    {
      "method": "POST",
      "path": "/Users",
      "bulkId": "qwerty",
      "data": {
        "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
        "userName": "Jayana",
        "password": "jayanapass"
      }
    },
    {
      "method": "POST",
      "path": "/Users",
      "bulkId": "qwerty",
      "data": {
        "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
        "userName": "Randul",
        "password": "Randulpass"
      }
    },
    {
      "method": "POST",
      "path": "/Users",
      "bulkId": "qwerty",
      "data": {
        "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
        "userName": "Rukshan",
        "password": "rukshanpass"
      }
    },
    {
      "method": "POST",
      "path": "/Users",
      "bulkId": "qwerty",
      "data": {
        "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
        "userName": "Chithara",
        "password": "chitharapass"
      }
    },
    {
      "method": "POST",
      "path": "/Users",
      "bulkId": "ytrewq",
      "data": {
        "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User", "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User"],
        "userName": "Kalana",
        "password": "kalanapass",
        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User": {
          "employeeNumber": "11250",
          "mentor": {
            "value": "bulkId:qwerty"
          }
        }
      }
    },
    {
      "method": "POST",
      "path": "/Users",
      "bulkId": "ytrewq",
      "data": {
        "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User", "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User"],
        "userName": "Shwetha",
        "password": "shwethapass",
        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User": {
          "employeeNumber": "11251",
          "": {
            "value": "bulkId:qwerty"
          }
        }
      }
    }
  ]
}')

# Check if the response contains any error message
if echo "$response" | grep -q '"Errors":'; then
  # If there is an error, print the failure message with the error description
  error_description=$(echo "$response" | jq -r '.Errors[0].description')
  echo -e "${RED}${BOLD}Failure: $error_description${NC}"
else
  # If there is no error, print the success message with the output
  echo -e "Success: $response"
  # Print the additional information with all the details
  echo -e "${PURPLE}${BOLD}Bulk user creation request sent to Identity Server. Some roles have also been assigned to the created users.${NC}"
  echo -e "${PURPLE}${BOLD}Bulk user details:${NC}"
  echo -e "${PURPLE}${BOLD}  User 1:${NC}"
  echo -e "User Name: ${PURPLE}${BOLD}${GIVEN_USER_NAME}${NC}"
  echo -e "Password: ${PURPLE}${BOLD}${GIVEN_PASSWORD}${NC}"
  echo -e "${PURPLE}${BOLD}  User 2:${NC}"
  echo -e "User Name: ${PURPLE}${BOLD}Randul${NC}"
  echo -e "Password: ${PURPLE}${BOLD}Randulpass${NC}"
  echo -e "${PURPLE}${BOLD}  User 3:${NC}"
  echo -e "User Name: ${PURPLE}${BOLD}Rukshan${NC}"
  echo -e "Password: ${PURPLE}${BOLD}rukshanpass${NC}"
  echo -e "${PURPLE}${BOLD}  User 4:${NC}"
  echo -e "User Name: ${PURPLE}${BOLD}Chithara${NC}"
  echo -e "Password: ${PURPLE}${BOLD}chitharapass${NC}"
  echo -e "${PURPLE}${BOLD}  User 5:${NC}"
  echo -e "User Name: ${PURPLE}${BOLD}Chamath${NC}"
  echo -e "Password: ${PURPLE}${BOLD}chamathpass${NC}"
  echo -e "Employee Number: ${PURPLE}${BOLD}11250${NC}"
  echo -e "Mentor: ${PURPLE}${BOLD}bulkId:qwerty${NC}"
  echo -e "${PURPLE}${BOLD}  User 6:${NC}"
  echo -e "User Name: ${PURPLE}${BOLD}Ashen${NC}"
  echo -e "Password: ${PURPLE}${BOLD}ashenpass${NC}"
  echo -e "Employee Number: ${PURPLE}${BOLD}11251${NC}"
  echo -e "${PURPLE}${BOLD}    : bulkId:qwerty${NC}"
  echo
fi
