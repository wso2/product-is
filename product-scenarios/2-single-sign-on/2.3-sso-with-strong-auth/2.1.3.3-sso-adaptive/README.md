# 2.1.3.3 Adaptive Authentication

## Business use-case narrative
An organization can have two or more web applications. Single sign on can be used to support seamless login to the 
multiple applications.

Adaptive authentication is a secure and flexible form of authentication. It enables validating multiple factors to 
determine the authenticity of a login attempt before granting access to a resource. The factors that are used for 
validation can depend on the risk probability associated with the particular user access request. This enables 
adjusting the authentication strength based on the context at hand. 

WSO2 Identity Server (WSO2 IS) supports script-based adaptive authentication, which allows you to use a script to set
 up appropriate authentication factors depending on your scenario. This enables ensuring security without impacting 
 usability at the time of authentication.  
## Persona
End User

## Sub-Scenarios
- [2.1.3.3.1 Risk based authentication]()
- [2.1.3.3.2 Location based authentication]()
- [2.1.3.3.2 Role based authentication]()