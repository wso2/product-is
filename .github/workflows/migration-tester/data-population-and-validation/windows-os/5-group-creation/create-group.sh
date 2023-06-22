curl -k --location --request POST "$SCIM2_GROUP_EP" \
--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
--header 'Content-Type: application/json' \
--data-raw '{
    "displayName": "'$GROUP_NAME'",
    "schemas": [
        "urn:ietf:params:scim:schemas:core:2.0:Group"
    ]
}'

echo "\033[1;34mA group has been created. Group name=$GROUP_NAME\033[0m"
echo
