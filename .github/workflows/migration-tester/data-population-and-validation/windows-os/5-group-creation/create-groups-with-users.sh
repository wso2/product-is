#curl -k --location --request POST "$SCIM2_GROUP_EP" \
#--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
#--header 'Content-Type: application/json' \
#--data-raw '{
   # "displayName": "'$GROUP_DISPLAY_NAME'",
    #"members": [
        #{
        #    "value": "GROUP_USER_ID"
       # }
    #],
    #"schemas": [
       # "urn:ietf:params:scim:schemas:core:2.0:Group"
    #]
#}'


# Create interns group
curl -k -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -d '{"displayName":"interns","members":[{"display":"Jayana","value":"Jayana","ref":"User","operation":"add"},{"display":"Randul","value":"Randul","ref":"User","operation":"add"},{"display":"Chithara","value":"Chithara","ref":"User","operation":"add"},{"display":"Rukshan","value":"Rukshan","ref":"User","operation":"add"}]}' https://localhost:9443/api/identity/group/v1.0/groups

# Create mentors group
curl -k -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -d '{"displayName":"mentors","members":[{"display":"Ashen","value":"Ashen","ref":"User","operation":"add"},{"display":"Chamath","value":"Chamath","ref":"User","operation":"add"}]}' https://localhost:9443/api/identity/group/v1.0/groups

echo "\033[1;34mA group called $GROUP_DISPLAY_NAME has been created with a user-ID=$GROUP_USER_ID\033[0m"
echo
