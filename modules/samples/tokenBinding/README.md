# TokenBindingSample
This is a **Php based Oauth sample** application for WSO2 Identity server.

This applicaiton can generate simple oauth requests that can support **Token binding**.Can be used to check normal oauth requests also.


How to run:
1. Change the IS address in the code to your IS server address (default address : https://localhost/oauth2)
2. Host this application in lampp or nginx (any php webserver) 
3. Go to the index page and select the grant type
4. Fill the details and get the response in callback URL

concerns:
* In authorization code type it will send PKCE headers related to token binding. So for normal usage don't use this grant type unless you have token binding supported IS version.
* Authorization grant type is used to check authorization code validation and it won't bind access token or refresh token


