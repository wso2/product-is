curl -k --location --request POST "$SCIM_USER_EP_USERSTORE" \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
--data-raw '{"schemas":[],"userName":"'$USERSTORE_USER_NAME'","password":"'$USERSTORE_USER_PASSWORD'","wso2Extension":{"employeeNumber":"000111","costCenter":"111111","organization":"WSO2Org","division":"'$USERSTORE_GROUP_NAME'","department":"Intigration","manager":{"managerId":"111000","displayName":"'$USERSTORE_USER_NAME'"}}}'

echo "\033[1;35mA user has been created in userstore. User name=$USERSTORE_USER_NAME, Group name=$USERSTORE_GROUP_NAME\033[0m"
echo
