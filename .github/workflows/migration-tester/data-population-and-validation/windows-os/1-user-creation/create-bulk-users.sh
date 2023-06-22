curl -k --location --request POST "$SCIM_BULK_EP" \
--header 'Content-Type: application/scim+json' \
--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
--data-raw '{"failOnErrors":1,"schemas":["urn:ietf:params:scim:api:messages:2.0:BulkRequest"],"Operations":[{"method": "POST","path": "/Users","bulkId": "qwerty","data":{"schemas":["urn:ietf:params:scim:schemas:core:2.0:User"],"userName": "Jayana","password":"jayanapass"}},{"method": "POST","path": "/Users","bulkId": "qwerty","data":{"schemas":["urn:ietf:params:scim:schemas:core:2.0:User"],"userName": "Randul","password":"Randulpass"}},{"method": "POST","path": "/Users","bulkId": "qwerty","data":{"schemas":["urn:ietf:params:scim:schemas:core:2.0:User"],"userName": "Rukshan","password":"rukshanpass"}},{"method": "POST","path": "/Users","bulkId": "qwerty","data":{"schemas":["urn:ietf:params:scim:schemas:core:2.0:User"],"userName": "Chithara","password":"chitharapass"}},{"method": "POST","path": "/Users","bulkId":"ytrewq","data":{"schemas":["urn:ietf:params:scim:schemas:core:2.0:User","urn:ietf:params:scim:schemas:extension:enterprise:2.0:User"],"userName":"Chamath","password":"chamathpass","urn:ietf:params:scim:schemas:extension:enterprise:2.0:User":{"employeeNumber": "11250","mentor": {"value": "bulkId:qwerty"}}}},{"method": "POST","path": "/Users","bulkId":"ytrewq","data":{"schemas":["urn:ietf:params:scim:schemas:core:2.0:User","urn:ietf:params:scim:schemas:extension:enterprise:2.0:User"],"userName":"Ashen","password":"ashenpass","urn:ietf:params:scim:schemas:extension:enterprise:2.0:User":{"employeeNumber": "11251","": {"value": "bulkId:qwerty"}}}}]}'

echo "\033[1;33mThis is a bulk import of users to IS. User roles have also been assigned to some users.\033[0m"
echo
