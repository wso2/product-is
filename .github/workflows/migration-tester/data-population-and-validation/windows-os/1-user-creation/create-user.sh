#!/bin/bash

#curl -k --location --request POST "https://localhost:9443/scim2/Users" \
#--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
#--header 'Content-Type: application/json' \
#--data-raw '{
#  "schemas": [],
#  "name": {
#    "givenName": 'Prabhanu',
#    "familyName": 'Gunaweera'
#  },
#  "userName": 'Prabhanu',
#  "password": 'Prabhanu123456789',
#  "emails": [
#    {
 #     "type": "home",
 #     "value": 'prabhanu@iit.ac.lk',
 #     "primary": true
 #   },
#    {
#      "type": "work",
#      "value": 'prabhanu@iit2.ac.lk'
#    }
#  ],
#  "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User": {
#    "employeeNumber": "1234A",
#    "manager": {
#      "value": "Taylor"
#    }
#  }
#}'


# make the curl request and capture the response
response=$(curl -k --location --request POST "$SCIM_USER_EP" \
--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
--header 'Content-Type: application/json' \
--data-raw '{
  "schemas": [],
  "name": {
    "givenName": '$GIVEN_NAME',
    "familyName": '$GIVEN_FAMILY_NAME'
  },
  "userName": '$GIVEN_USER_NAME',
  "password": '$GIVEN_PASSWORD',
  "emails": [
    {
      "type": "home",
      "value": '$GIVEN_USER_EMAIL_HOME',
      "primary": true
    },
    {
      "type": "work",
      "value": '$GIVEN_USER_EMAIL_WORK'
    }
  ],
  "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User": {
    "employeeNumber": "1234A",
    "manager": {
      "value": "Taylor"
    }
  }
}')

# check if the response contains any error message
if echo "$response" | grep -q '"Errors":'; then
  # if there is an error, print the failure message with the error description
  error_description=$(echo "$response" | jq '.Errors[0].description')
  echo "Failure: $error_description"
else
  # if there is no error, print the success message with the output
  echo "Success: $response"
fi

echo "\033[1;33mA user has been created in Identity Server. User name=$GIVEN_USER_NAME\033[0m"
echo

