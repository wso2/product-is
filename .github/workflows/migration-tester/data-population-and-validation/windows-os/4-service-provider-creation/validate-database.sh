#!/bin/bash

# Read client_id and client_secret from file
source client_credentials

# Encode client_id:client_secret as base64
base64_encoded=$(echo -n "$client_id:$client_secret" | base64)

# Get access token
access_token_response=$(curl -k -X POST https://localhost:9443/oauth2/token -H "Authorization: Basic $base64_encoded" -H 'Content-Type: application/x-www-form-urlencoded' -d 'grant_type=password&username=admin&password=admin&scope=somescope_password')

# Extract access token from response
access_token=$(echo $access_token_response | jq -r '.access_token')

echo "Generated access token successfully: $access_token"

# Validate the database
curl -k -H "Authorization: Bearer $access_token" -H 'Content-Type: application/json' -X GET https://localhost:9443/api/identity/oauth2/dcr/v1.1/register/$client_id
