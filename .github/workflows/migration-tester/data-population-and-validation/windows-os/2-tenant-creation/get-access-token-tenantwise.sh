#!/bin/bash
# Create tenant
response=$(curl -k --location --request POST 'https://localhost:9443/t/carbon.super/api/server/v1/tenants' \
--header 'accept: */*' \
--header 'Content-Type: application/json' \
--data-raw '{"domain":"iit.com","owners":[{"username":"jayana","password":"jayana123","email":"jayana@wso2.com","firstname":"jayana","lastname":"gunaweera","provisioningMethod":"inline-password","additionalClaims":[{"claim":"http://wso2.org/claims/telephone","value":"+94 562 8723"}]}]}')

# Extract client_id and client_secret from response
client_id=$(echo $response | jq -r '.client_id')
client_secret=$(echo $response | jq -r '.client_secret')

# Encode client_id:client_secret in base64
base64_encoded=$(echo -n "$client_id:$client_secret" | base64)

# Register service provider
response=$(curl -k --location --request POST 'https://localhost:9443/t/iit.com/api/server/v1/service/register' \
--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
--header 'Content-Type: application/json' \
--data-raw '{  "client_name": "migration app", "grant_types": ["authorization_code","implicit","password","client_credentials","refresh_token"], "redirect_uris":["http://localhost:8080/playground2"] }')
echo "Registered a service provider successfully"

# Extract client_id and client_secret
client_id=$(echo $response | jq -r '.client_id')
client_secret=$(echo $response | jq -r '.client_secret')

# Encode client_id:client_secret as base64
base64_encoded=$(echo -n "$client_id:$client_secret" | base64)

# Generate access token
response=$(curl -k --location --request POST 'https://localhost:9443/t/iit.com/oauth2/token' \
--header "Content-Type: application/x-www-form-urlencoded" \
--header "Authorization: Basic $base64_encoded" \
--data-urlencode 'grant_type=password' \
--data-urlencode 'username=jayana@iit.com' \
--data-urlencode 'password=jayana123' \
--data-urlencode 'scope=samplescope')

# Extract access token from response
access_token=$(echo $response | jq -r '.access_token')
if [ -n "$access_token" ]; then
  # Store access token in a file
  echo "access_token=$access_token" >> tenant_credentials

  # Print tenant access token in file
  echo "${YELLOW}Tenant Access Token:${NC}"
  cat Tenant_credentials
  
  # Print success message
  echo "${GREEN}Generated an access token from a service provider registered in the tenant successfully!.${NC}"
else
  # Print error message
  echo "${RED}No access token generated from tenant.${NC}"
fi

echo "\033[0;36mSome tokens have been generated from a registered application in the tenant=$TENANT_NAME\033[0m"
echo
