# Identity Server Product Scenarios

## [01. Manage Users and Roles](1-manage-users-roles/README.md)
## [02. Single sign on accross multiple applications](2-single-sign-on/README.md)
## [03. Access control](3-access-control/README.md)
## [04. Accessing a secured resource](4-secure-apis/README.md)
## [05. Monitoring user session and login analytics](5-analytics/README.md)

### Business use-case narrative

Let's take a scenario where an organization has an web application that provides a particular service. To consume 
that service, end users need to create an account in this organization. Also, it is required to do other user 
operations such as delete/update users as well.

An organization can have two or more web applications. Single sign on can be used to support seamless login to the 
multiple applications. 

An organization can have an application which should be able to access during day time only (9AM-5PM). Access control
 can be used to deny/permit authorization based on such predefined policies (rules)
 
APIs should be accessed by the authorized parties only.   