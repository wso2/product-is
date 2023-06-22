curl -k --location --request POST "$USERSTORE_EP" \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
--data-raw '{
  "typeId": "SkRCQ1VzZXJTdG9yZU1hbmFnZXI",
  "description": "Sample JDBC user store to add.",
  "name": "'$USERSTORE_NAME'",
  "properties": [
    {
      "name": "userName",
      "value": "testdb"
    },
    {
      "name": "password",
      "value": "testdb"
    },
    {"name": "driverName",
      "value": "com.mysql.jdbc.Driver"
    },
    {
      "name":"url",
      "value":"jdbc:mysql://localhost:3306/testdb?useSSL=false"
    },
    {
      "name":"disabled",
      "value":"false"
    }
  ]
}'

echo "\033[1;35mA userstore has been created in Identity Server. Userstore name=$USERSTORE_NAME\033[0m"
echo
