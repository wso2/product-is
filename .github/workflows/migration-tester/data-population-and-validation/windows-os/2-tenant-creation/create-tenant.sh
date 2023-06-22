#curl -k --location --request POST "$TENANT_EP" \
#--header 'accept: */*' \
#--header 'Content-Type: application/json' \
#--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
#--data-raw '{"domain":"wso2.com","owners":[{"username":"'$TENANT_USER_NAME'","password":"natsume123","email":"natsumehyuga@wso2.com","firstname":"natsume","lastname":"hyuga","provisioningMethod":"inline-password","additionalClaims":[{"claim":"http://wso2.org/claims/telephone","value":"+94 562 8723"}]}]}'

curl -k --location --request POST "$TENANT_EP" \
--header 'accept: */*' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
--data-raw '{"domain":"iit.com","owners":[{"username":"iit","password":"iit123","email":"iit@iit.com","firstname":"iit","lastname":"iit","provisioningMethod":"inline-password","additionalClaims":[{"claim":"http://wso2.org/claims/telephone","value":"+94 76 318 6705"}]}]}'

echo "\033[0;36mA tenant has been created with a user. Tenant name=$TENANT_USER_NAME. User name=$TENANT_USER_NAME\033[0m"
echo

echo "\033[0;36mA tenant has been created with a user. Tenant name=iit. User name=iit@iit.com\033[0m"
echo
