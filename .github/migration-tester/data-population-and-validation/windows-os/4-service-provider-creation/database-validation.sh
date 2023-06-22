#!/bin/bash

# set variables
HOSTNAME=localhost
PORT=9443
USERNAME=admin
PASSWORD=admin
SP_APP=automating_product_migration_testing
SP_USERNAME=Jayana123
SP_PASSWORD=Jayana123

# get access token for registered service provider
set -e
ACCESS_TOKEN=$(curl -k -d "grant_type=password&username=${SP_USERNAME}&password=${SP_PASSWORD}&scope=openid" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "${SP_APP}:${SP_APP}" \
  "https://${HOSTNAME}:${PORT}/oauth2/token" | jq -r '.access_token') || exit 1

# check if access token is not empty
if [ -z "${ACCESS_TOKEN}" ]; then
  echo "Access token is not generated."
  exit 1
fi

# test database connection
set -e
RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  "https://${HOSTNAME}:${PORT}/api/server/v1/datasources") || exit 1

# check if database connection is successful
if [ "${RESPONSE_CODE}" -eq 200 ]; then
  echo "Database validation successful."
  exit 0
else
  echo "Database validation failed with response code: ${RESPONSE_CODE}"
  exit 1
fi
