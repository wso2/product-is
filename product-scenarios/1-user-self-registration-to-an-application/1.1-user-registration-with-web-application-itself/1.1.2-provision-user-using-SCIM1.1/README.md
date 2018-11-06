# 1.1.2 Provision user in identity server using SCIM1.1 API

## When to use this approach

This is the recommended approach to use SCIM 1.1 SDK from the web application.

## Sample use case
Invoking Identity Server SCIM user endpoint to provision a user. In order to invoke SCIM endpoints, client should to authenticate & authorize properly and in order to do that, its use basic authentication using a credential of privileged user in identity server which have permission to access SCIM APIs.


## Prerequisites
A REST client like cURL to invoke the Identity Server API. 

## Development 
No development instructions are needed.

## Deployment
No deployment instructions are needed.

## Testing and acceptance criteria

Invoke the SCIM 1.1 user create request as shown below. Use an HTTP client like cURL.

```
curl -k -X POST \
  https://localhost:9443/wso2/scim/Users/ \
  -H 'Authorization: Basic YWRtaW46YWRtaW4=' \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/json' \
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


- Create user with valid SCIM 1.1 request (Happy path)
- Create user with malformed request
- Create user without authorization headers


## Observability
N/A

## Maintenance tips
N/A

## APIs
https://docs.wso2.com/display/IS520/SCIM+APIs

## See Also
