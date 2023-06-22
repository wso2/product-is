#!/bin/bash

# Register service provider
response=$(curl -k --location --request POST 'https://localhost:9443/api/identity/oauth2/dcr/v1.1/register' \
--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
--header 'Content-Type: application/json' \
--data-raw '{  "client_name": "Migration Application", "grant_types": ["authorization_code","implicit","password","client_credentials","refresh_token"], "redirect_uris":["http://localhost:8080/playground2"] }')

# Extract client_name from response
client_name=$(echo $response | jq -r '.client_name')

# Define colors
RED='\033[0;31m'
GREEN='\033[1;38;5;206m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${YELLOW}Response: $response${NC}"
echo "${GREEN}Service provider '$client_name' registered successfully${NC}"
echo

# Extract client_id and client_secret
client_id=$(echo $response | jq -r '.client_id')
client_secret=$(echo $response | jq -r '.client_secret')

# Store client_id and client_secret in a file
if [ -f "client_credentials" ]; then
  echo "client_id=$client_id" >> client_credentials
  echo "client_secret=$client_secret" >> client_credentials
else
  echo "client_id=$client_id" > client_credentials
  echo "client_secret=$client_secret" >> client_credentials
fi

# Print client_id and client_secret
echo "${YELLOW}Client ID: $client_id${NC}"
echo "${YELLOW}Client Secret: $client_secret${NC}"

# Encode client_id:client_secret as base64
base64_encoded=$(echo -n "$client_id:$client_secret" | base64)

# Get access token
access_token_response=$(curl -k -X POST https://localhost:9443/oauth2/token -H "Authorization: Basic $base64_encoded" -H 'Content-Type: application/x-www-form-urlencoded' -d 'grant_type=password&username=admin&password=admin&scope=somescope_password')

# Extract access token from response
access_token=$(echo $access_token_response | jq -r '.access_token')

# Store access token in a file
echo "access_token=$access_token" >> client_credentials

# Print client credentials and access token in file
echo "${YELLOW}Client Credentials and Access Token:${NC}"
cat client_credentials

# Print access token
echo "${GREEN}An access token generated successfully from the registered service provider: $access_token${NC}"

echo "\033[1;38;5;206mAn oAuth token has been generated.\033[0m"
echo

