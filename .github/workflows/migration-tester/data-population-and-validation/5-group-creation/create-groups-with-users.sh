#!/bin/bash

CYAN='\033[0;36m\033[1m'   # cyan color
GREEN='\033[0;32m\033[1m'  # green color
BLUE='\033[0;34m\033[1m'   # blue color
YELLOW='\033[0;33m\033[1m' # yellow color
ORANGE='\033[0;91m\033[1m' # orange color
RED='\033[0;31m\033[1m'    # red color
PURPLE='\033[1;35m'        # purple color
RESET='\033[0m'   

# Create the user and retrieve the user ID
user_response=$(curl -v -k --user admin:admin --data '{"schemas":[],"name":{"familyName":"gunasinghe","givenName":"hasinitg"},"userName":"hasinitg","password":"hasinitg","emails":[{"primary":true,"value":"hasini_home.com","type":"home"},{"value":"hasini_work.com","type":"work"}]}' --header "Content-Type:application/json" https://localhost:9443/wso2/scim/Users)
user_id=$(echo "$user_response" | jq -r '.id')

if [ -n "$user_id" ]; then
  echo -e "${PURPLE}${BOLD}User has been created successfully.${NC}"
  echo -e "${PURPLE}${BOLD}User ID:${NC} $user_id"

  # Create the 'Interns' group and add the user to it
  response=$(curl -k --location --request POST "$SCIM2_GROUP_EP" \
    --header 'Authorization: Basic YWRtaW46YWRtaW4=' \
    --header 'Content-Type: application/json' \
    --data-raw '{
      "displayName": "Interns",
      "members": [
        {
          "value": "'"$user_id"'"
        }
      ],
      "schemas": [
        "urn:ietf:params:scim:schemas:core:2.0:Group"
      ]
    }')

  group_id=$(echo "$response" | jq -r '.id')

  if [ -n "$group_id" ]; then
    echo -e "${PURPLE}${BOLD}Success Message${NC}: $response"
    echo -e "${PURPLE}${BOLD}Group 'Interns' has been created and the user has been added successfully.${NC}"
  else
    echo -e "${RED}${BOLD}Failed to create the 'Interns' group.${NC}"
    echo -e "${RED}${BOLD}Error Message:${NC} $response"
  fi
else
  echo -e "${RED}${BOLD}Failed to create the user.${NC}"
  echo -e "Error Message: $user_response"
  exit 1  # Exit with an error code to indicate failure
fi

# Create the user and retrieve the user ID
user_response=$(curl -v -k --user admin:admin --data '{"schemas":[],"name":{"familyName":"Doe","givenName":"John"},"userName":"johndoe","password":"johndoe","emails":[{"primary":true,"value":"johndoe_home.com","type":"home"},{"value":"johndoe_work.com","type":"work"}]}' --header "Content-Type:application/json" https://localhost:9443/wso2/scim/Users)
user_id=$(echo "$user_response" | jq -r '.id')

if [ -n "$user_id" ]; then
  echo -e "${PURPLE}${BOLD}User has been created successfully.${NC}"
  echo -e "${PURPLE}${BOLD}User ID:${NC} $user_id"

  # Create the 'Mentor Group' and add the user to it
  response=$(curl -k --location --request POST "$SCIM2_GROUP_EP" \
    --header 'Authorization: Basic YWRtaW46YWRtaW4=' \
    --header 'Content-Type: application/json' \
    --data-raw '{
      "displayName": "Mentors",
      "members": [
        {
          "value": "'"$user_id"'"
        }
      ],
      "schemas": [
        "urn:ietf:params:scim:schemas:core:2.0:Group"
      ]
    }')

  group_id=$(echo "$response" | jq -r '.id')

  if [ -n "$group_id" ]; then
    echo -e "${PURPLE}${BOLD}Success Message${NC}: $response"
    echo -e "${PURPLE}${BOLD}Group 'Mentor Group' has been created and the user has been added successfully.${NC}"
  else
    echo -e "${RED}${BOLD}Failed to create the 'Mentor Group'.${NC}"
    echo -e "${RED}${BOLD}Error Message:${NC} $response"
  fi
else
  echo -e "${RED}${BOLD}Failed to create the user.${NC}"
  echo -e "Error Message: $user_response"
  exit 1  # Exit with an error code to indicate failure
fi

# Create users using bulk request and extract user IDs
bulk_response=$(curl -v -k --user admin:admin -H "Accept: application/json" -H "Content-type: application/json" -d "{\"failOnErrors\":2,\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"Operations\":[{\"data\":{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"path\":\"/Users\",\"userName\":\"geeth\",\"method\":\"POST\",\"emails\":[{\"value\":\"geeth@gmail.com\"},{\"value\":\"geethg@yahoo.com\"}],\"phoneNumbers\":[{\"value\":\"0772508354\"}],\"displayName\":\"Geeth\",\"externalId\":\"geeth@wso2.com\",\"password\":\"dummyPW1\",\"preferredLanguage\":\"Sinhala\",\"bulkId\":\"bulkIDUser1\"},\"path\":\"/Users\",\"method\":\"POST\",\"bulkId\":\"bulkIDUser1\"},{\"data\":{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"path\":\"/Users\",\"userName\":\"dinuka\",\"method\":\"POST\",\"emails\":[{\"value\":\"dinuka.malalanayake@gmail.com\"},{\"value\":\"dinuka_malalanayake@yahoo.com\"}],\"phoneNumbers\":[{\"value\":\"0772508354\"}],\"displayName\":\"Dinuka\",\"externalId\":\"dinukam@wso2.com\",\"password\":\"myPassword\",\"preferredLanguage\":\"Sinhala\",\"bulkId\":\"bulkIDUser2\"},\"path\":\"/Users\",\"method\":\"POST\",\"bulkId\":\"bulkIDUser2\"}]}" https://localhost:9443/wso2/scim/Bulk)

# Extract user IDs from bulk response
geeth_user_id=$(echo "$bulk_response" | jq -r '.Operations[0].response.body.id')
dinuka_user_id=$(echo "$bulk_response" | jq -r '.Operations[1].response.body.id')

if [ -n "$geeth_user_id" ] && [ -n "$dinuka_user_id" ]; then
  echo -e "${PURPLE}${BOLD}Users have been created successfully.${NC}"
  echo -e "${PURPLE}${BOLD}User IDs:${NC} $geeth_user_id, $dinuka_user_id"

  # Create groups and add users to them
  group_response=$(curl -v -k --user admin:admin -H "Accept: application/json" -H "Content-type: application/json" -d "{\"failOnErrors\":2,\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"Operations\":[{\"data\":{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"path\":\"/Groups\",\"method\":\"POST\",\"displayName\":\"security\",\"externalId\":\"security\",\"members\":[{\"value\":\"$geeth_user_id\"}],\"bulkId\":\"bulkGroup1\"},\"path\":\"/Groups\",\"bulkId\":\"bulkGroup1\"},{\"data\":{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"path\":\"/Groups\",\"method\":\"POST\",\"displayName\":\"legal\",\"externalId\":\"legal\",\"members\":[{\"value\":\"$dinuka_user_id\"}],\"bulkId\":\"bulkGroup2\"},\"path\":\"/Groups\",\"bulkId\":\"bulkGroup2\"}]}" https://localhost:9443/wso2/scim/B)

  # Check if there are any errors in the group creation response
  if echo "$group_response" | grep -q '"error":'; then
    # If there is an error, print the failure message with the error description
    error_description=$(echo "$group_response" | jq -r '.error_description')
    echo -e "${RED}${BOLD}Failure in creating groups: $error_description${NC}"
  else
    echo -e "${PURPLE}${BOLD}Groups have been created successfully.${NC}"
    echo "$group_response"
  fi
else
  echo -e "${RED}${BOLD}Failure in creating users.${NC}"
fi
