# 1.1.3 provision-user-using-SOAP-Admin-services 

## When to use this approach

This is used if you want to add a new user role and a user using the SOAP Admin services.

## Sample use case
Invoking Identity Server SCIM2 user endpoint to provision a user. In order to invoke SCIM2 endpoints, client should to authenticate & authorize properly and in order to do that, its use basic authentication using a credential of privileged user in identity server which have permission[2] to access SCIM APIs.

Invokes the add role function to add a new user role via the SOAP service. Then we call the addUser function to add a new user via the SOAP service using the SOAP service and assign the added user role to the new user

## Prerequisites
1.An Identity server should be started. 

2.The infrastructure.properties file should be updated with the relavent paramters
ISHttpsUrl
PROTOCOL
HOST
PORT
CARBON_HOME_PATH
## Development 
No development instructions are needed.

## Deployment
No deployment instructions are needed.

## Testing and acceptance criteria

1.If required add the required values to the below parameters and run the command 
String ROLE_NAME
String USER_NAME
String USER_PASSWORD

2.Invoke the command ./test.sh --input-dir </path/to/data/bucket>
Note:</path/to/data/bucket> indicate the location where the infrastructure.properties file is available and also the location where the results file will be created.


```
curl -k -X POST \
  https://localhost:9443/scim2/Users/ \
  -H 'Authorization: Basic YWRtaW46YWRtaW4=' \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/scim+json' \
  -d '{
  "schemas": [
    
  ],
  "name": {
    "givenName": "jane"
  },
  "userName": "jane",
  "password": "jane123"
}'
```


- Create user with valid SCIM 2.0 request (Happy path)
- Create user with malformed request
- Create user without authorization headers


## Observability
N/A

## Maintenance tips
N/A

## APIs
https://docs.wso2.com/display/IS540/apidocs/SCIM2-endpoints/#!/operations#UsersEndpoint#createUser

## See Also
