#!/bin/bash

# Define colours
RED='\033[0;31m'
GREEN='\033[0;32m\033[1m'
PURPLE='\033[1;35m'
BOLD='\033[1m'
NC='\033[0m' # No Color

os=$1

# Set deployment file and path based on OS
if [ "$os" = "ubuntu-latest" ]; then
  chmod +x env.sh
  . "/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation/env.sh"
  echo "${GREEN}==> Env file for Ubuntu sourced successfully${NC}"
fi
if [ "$os" = "macos-latest" ]; then
  chmod +x env.sh
  source "/Users/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/migration-tester/migration-automation/env.sh"
  echo "${GREEN}==> Env file for Mac sourced successfully${RESET}"
fi

# Create a sample tenant
response=$(curl -k --location --request POST 'https://localhost:9443/api/server/v1/tenants' \
  --header 'accept: */*' \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Basic YWRtaW46YWRtaW4=' \
  --data-raw '{"domain":"iit.com","owners":[{"username":"admin","password":"admin","email":"jayana@iit.com","firstname":"Jayana","lastname":"Gunaweera","provisioningMethod":"inline-password","additionalClaims":[{"claim":"http://wso2.org/claims/telephone","value":"+94 562 8723"}]}]}')

# Check if the response contains any error message
if echo "$response" | grep -q '"error":'; then
  # If there is an error, print the failure message with the error description
  error_description=$(echo "$response" | jq -r '.error_description')
  echo -e "${RED}${BOLD}Failure: $error_description${NC}"

else
  # If there is no error, print the success message
  echo -e "${PURPLE}${BOLD}Success: A tenant has been created successfully.${NC}"
fi

# Create a sample tenant, register a service provider inside in it and generate an access token from it.
# Define variables
TENANT_EP="https://localhost:9443/api/server/v1/tenants"
USERNAME="dummyuser"
PASSWORD="dummypassword"
EMAIL="dummyuser@wso2.com"
FIRSTNAME="Dummy"
LASTNAME="User"
TELEPHONE="+94 123 4567"

echo -e "${PURPLE}${BOLD}Creating a tenant using a tenanted url....${NC}"

# Create tenant
response=$(curl -k --location --request POST "$TENANT_EP" \
  --header 'accept: */*' \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Basic YWRtaW46YWRtaW4=' \
  --data-raw '{
    "domain": "wso2.com",
    "owners": [
      {
        "username": "'"$USERNAME"'",
        "password": "'"$PASSWORD"'",
        "email": "'"$EMAIL"'",
        "firstname": "'"$FIRSTNAME"'",
        "lastname": "'"$LASTNAME"'",
        "provisioningMethod": "inline-password",
        "additionalClaims": [
          {
            "claim": "http://wso2.org/claims/telephone",
            "value": "'"$TELEPHONE"'"
          }
        ]
      }
    ]
  }')

echo "Curl response: $response"

# Check if the response contains any error message
if echo "$response" | grep -q '"error":'; then
  # If there is an error, print the failure message with the error description
  error_description=$(echo "$response" | jq -r '.error[0].description')
  echo -e "${RED}${BOLD}Failure: $error_description${NC}"
else
  # If there is no error, print the success message
  echo -e "${PURPLE}${BOLD}Success: Tenant has been created successfully using a tenanted url.${NC}"

  # Print the details of the successful response
  echo -e "${PURPLE}${BOLD}Response Details:${NC}"
  echo "$response" | jq '.'

  # Print the additional information with all the details
  echo -e "${PURPLE}${BOLD}A tenant has been created with a user.${NC}"
  echo -e "Tenant name: ${PURPLE}dummyuser@wso2.com${NC}"
  echo -e "User name: ${PURPLE}$USERNAME${NC}"
  echo
  echo -e "${PURPLE}${BOLD}Additional Details:${NC}"
  echo -e "Domain: ${PURPLE}wso2.com${NC}"
  echo -e "Owner:"
  echo -e "  Username: ${PURPLE}$USERNAME${NC}"
  echo -e "  Password: ${PURPLE}$PASSWORD${NC}"
  echo -e "  Email: ${PURPLE}$EMAIL${NC}"
  echo -e "  First Name: ${PURPLE}$FIRSTNAME${NC}"
  echo -e "  Last Name: ${PURPLE}$LASTNAME${NC}"
  echo -e "  Provisioning Method: ${PURPLE}inline-password${NC}"
  echo -e "  Additional Claims:"
  echo -e "    Claim: ${PURPLE}http://wso2.org/claims/telephone${NC}"
  echo -e "    Value: ${PURPLE}$TELEPHONE${NC}"
fi

# Extract tenant ID from the response
tenant_id=$(echo "$response" | jq -r '.tenant_id')

# Encode client_id:client_secret in base64
base64_encoded_sp=$(echo -n "dummyuser@wso2.com:dummypassword")

# Register a service provider inside the tenant
response=$(curl -k --location --request POST 'https://localhost:9443/t/wso2.com/api/identity/oauth2/dcr/v1.1/register' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic ZHVtbXl1c2VyQHdzbzIuY29tOmR1bW15cGFzc3dvcmQ=' \
--data-raw '{
  "client_name": "tenantapp",
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
  echo -e "${PURPLE}Response${NC}: $response"
  echo -e "${PURPLE}Service provider '$client_name' registered successfully${NC}"
  echo
fi

# Extract client_id and client_secret
client_id=$(echo "$response" | jq -r '.client_id')
client_secret=$(echo "$response" | jq -r '.client_secret')

# Store client_id and client_secret in a file
client_credentials_file="/home/runner/work/Automating-Product-Migration-Testing/Automating-Product-Migration-Testing/data-population-and-validation/2-tenant-creation/client_credentials"

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

# Generate access token
access_token_response=$(curl -k -i --location --request POST "https://localhost:9443/t/wso2.com/oauth2/token" \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --header "Authorization: Basic $base64_encoded" \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'username=dummyuser@wso2.com' \
  --data-urlencode 'password=dummypassword' \
  --data-urlencode 'scope=samplescope')

# Check if the response contains any error message
if echo "$access_token_response" | grep -q '"error":'; then
  # If there is an error, print the failure message with the error description
  error_description=$(echo "$access_token_response" | jq -r '.error_description')
  echo -e "${RED}No access token generated from the tenant.${NC}"
  echo -e "${RED}${BOLD}Failure: $error_description${NC}"
else
  # If there is no error, print the success message
  echo -e "${PURPLE}${BOLD}Success: Access token generated from the service provider registered in the tenant successfully.${NC}"

  # Print the details of the successful response
  echo -e "${PURPLE}Response Details:${NC}"
  echo "$access_token_response"

  # Extract access token from response
  access_token=$(echo "$access_token_response" | grep -o '"access_token":"[^"]*' | cut -d':' -f2 | tr -d '"')

  if [ -n "$access_token" ]; then
    # Store access token in a file
    echo "access_token=$access_token" >>tenant_credentials

    # Print tenant access token in file
    echo -e "${PURPLE}Tenant Access Token:${NC}"
    cat tenant_credentials

    # Print success message
    echo -e "${PURPLE}Generated an access token from the service provider registered in the tenant successfully!${NC}"
  else
    # Print error message
    echo -e "${RED}No access token generated from the service provider registered in tenant.${NC}"
  fi
fi

