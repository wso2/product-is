#!/bin/bash

# set variables
HOSTNAME=localhost
PORT=9443
USERNAME=admin
PASSWORD=admin
SP_APP=automating_product_migration_testing
SP_USERNAME=Jayana123
SP_PASSWORD=Jayana123

# register service provider
curl -k -X POST \
  -H "Authorization: Basic $(echo -n ${USERNAME}:${PASSWORD} | base64)" \
  -H "Content-Type: application/json" \
  -d '{
    "callbackUrl": "https://localhost/callback",
    "clientName": "'${SP_APP}'",
    "tokenType": "JWT",
    "grantTypes": "password refresh_token",
    "saasApp": true
  }' \
  "https://${HOSTNAME}:${PORT}/oauth2/client/register" | jq -r '.clientId + ":" + .clientSecret' > sp_credentials.txt

# get client id and secret
SP_CREDENTIALS=$(cat sp_credentials.txt)

# get access token for registered service provider
ACCESS_TOKEN=$(curl -k -d "grant_type=password&username=${SP_USERNAME}&password=${SP_PASSWORD}&scope=openid" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "${SP_CREDENTIALS}" \
  "https://${HOSTNAME}:${PORT}/oauth2/token" | jq -r '.access_token')

# check if access token is not empty
if [ -z "${ACCESS_TOKEN}" ]; then
  echo "Access token is not generated."
  exit 1
fi

# test database connection
RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  "https://${HOSTNAME}:${PORT}/api/server/v1/datasources")
echo "Response code: ${RESPONSE_CODE}"

# check if database connection is successful
if [ "${RESPONSE_CODE}" -eq 200 ]; then
  echo "Database validation successful."
  exit 0
else
  echo "Database validation failed."
  exit 1
fi
