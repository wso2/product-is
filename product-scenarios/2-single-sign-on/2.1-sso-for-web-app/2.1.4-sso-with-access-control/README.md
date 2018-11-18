# 2.1.4 Control access to an application while authentication.

## Business use-case narrative
In today's world, businesses and their customers need to access multiple service providers that support multiple 
heterogeneous identity federation protocols. Each service provider needs to define an authorization policy at the 
identity provider to decide whether a given user is eligible to log into the corresponding service provider. For 
example, one service provider only allows the administrator to sign in to the system after 6 PM. Another service 
provider only allows the users from North America to sing in. To meet all these requirements the Identity provider 
needs to provide fine-grained authorization

## Persona
End User

## Sub-Scenarios
- [2.1.4.1 Allow login to an application for the user who has a particular role only]()
- [2.1.4.2 Allow login to an application when a defined time duration only]()