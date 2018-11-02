# 1.1.1 Provision user in identity server using SCIM2 API

## When to use this approach

This is the recommended approach, unless you are limited to use SCIM 1.1 SDK from the web application.

## Sample use case
Invoking Identity Server SCIM2 user endpoint to provision a user. In order to invoke SCIM2 endpoints, client should to authenticate & authorize properly and in order to do that, its use basic authentication using a credential of privileged user in identity server which have permission[2] to access SCIM APIs.


## Prerequisites
A REST client like cURL to invoke the Identity Server API. 

## Development 
No development instructions are needed.

## Deployment
No deployment instructions are needed.

## Testing and acceptance criteria

Invoke the SCIM 2 user create request as shown below. Use an HTTP client like cURL.

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
