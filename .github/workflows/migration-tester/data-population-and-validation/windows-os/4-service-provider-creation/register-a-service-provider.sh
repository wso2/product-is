
#curl -k --location --request POST "$SP_REGISTER_EP" \
#--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
#--header 'Content-Type: application/json' \
#--data-raw '{  "client_name": "'$CLIENT_NAME'", "grant_types": ["authorization_code","implicit","password","client_credentials","refresh_token"], "redirect_uris":["'$CALLBACK_URI'"] }'


curl -k --location --request POST 'https://localhost:9443/api/identity/oauth2/dcr/v1.1/register' \
--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
--header 'Content-Type: application/json' \
--data-raw '{  "client_name": "test migration app", "grant_types": ["authorization_code","implicit","password","client_credentials","refresh_token"], "redirect_uris":["http://localhost:8080/playground2"] }'
echo "Registered a service provider successfully!"

echo "\033[1;38;5;206mA new service provider has been registered. Service provider name=$CLIENT_NAME\033[0m"
echo
