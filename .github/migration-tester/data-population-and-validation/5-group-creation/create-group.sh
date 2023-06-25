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

# Create the group
group_response=$(curl -k -i --location --request POST "$SCIM2_GROUP_EP" \
  --header 'Authorization: Basic YWRtaW46YWRtaW4=' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "displayName": "'$GROUP_NAME'",
    "schemas": [
        "urn:ietf:params:scim:schemas:core:2.0:Group"
    ]
}')

# Check if the group creation was successful
if echo "$group_response" | grep -q '"displayName":'; then
  echo "$group_response"
  # Print success message
  echo -e "${PURPLE}${BOLD}A group has been created successfully.${NC}"
  # Print group name
  echo -e "Group Name: ${PURPLE}$GROUP_NAME${NC}"
else
  # Print failure message
  echo -e "${RED}${BOLD}Failed to create the group.${NC}"
  # Print error details
  echo -e "${RED}${BOLD}Error Response:${NC}"
  echo "$group_response"
fi
echo
