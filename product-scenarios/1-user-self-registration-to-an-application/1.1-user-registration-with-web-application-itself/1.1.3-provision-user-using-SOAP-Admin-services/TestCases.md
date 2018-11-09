| # Test Case Template 	|  	|
|-------------------------------------------------------------------------------------------------------------------------------------------------------------	|-----------------------------------------------------------------------------------------------------	|
|  	|  	|
| ### 1.1.1 Provision user in identity server using SCIM2 API 	|  	|
|  	|  	|
| | TestCaseID | TestCase                                             | Test steps                                                            | Status      | 	|  	|
| |------------|------------------------------------------------------|-----------------------------------------------------------------------|-------------| 	|  	|
| 1.1.3.1    | Provision a new user role using SOAP Service            | **Given** 	| Test environment is set properly                           | In Progress   | 	|
| |            |                                                      | **When** 	| A request sends to create a user role  using a SOAP Service API           |             | 	|
|  	|  	|
| |            |                                                      | **Then** 	| The user role  should be provisioned in identity server 	|
|  	|  	|
| 1.1.3.2    | Provision a new user  using SOAP Service              | **Given** 	| Test environment is set properly                           | In Progress   | 	|
| |            |                                                      | **When** 	| A request sends to create a user  using a SOAP Service API           |             | 	|
|  	|  	|
| |            |                                                      | **Then** 	| The user  should be provisioned in IS                       | 	|
| | 1.1.3.3    | Update a user password using SOAP Service            | **Given** 	| A user is already provisioned exists                        | Not Started | 	|
| |            |                                                      | **When** 	| Details are entered to update the user password by calling a SOAP Service |             | 	|
| |            |                                                      | **Then** 	| The user password should  be updated & valid message appear |             | 	|
| | 	|  	|
| IS                       | 	|  	|
| | 1.1.3.4    | List the available users using SOAP Service          | **Given** 	| A user recors already provisioned exists                        | Not Started | 	|
| |            |                                                      | **When** 	| Details are entered to obtain a list of user by calling a SOAP Service |             | 	|
| |            |                                                      | **Then** 	| The user list should be extracted |             | 	|
| | 	|  	|
| |                                                      |                                                                       |             | 	|  	|
| | 	|  	|
| | 1.1.3.5    | Add an existing username via SOAP Service            | **Given** 	| A user is already provisioned exists                        | Not Started | 	|
| |            |                                                      | **When** 	| A request is sent to add an existing user by calling a SOAP Service |             | 	|
| |            |                                                      | **Then** 	| The user records should  not be  added |             | 	|
| |            |                                                      | 	|  	|
| | 	|  	|
| | 1.1.3.6    | Add a user through SOAP Service  without passing the 	|  	|
| user name                                                          | **Given** 	| A user is not already provisioned exists                        | Not Started | 	|
| |            |                                                      | **When** 	| A request is sent to add a user without passing the username calling a SOAP Service |             | 	|
| |            |                                                      | **Then** 	| The user records should  not be added  |             | 	|
| |            |                                                      | 	|  	|
| |                                                                                |             | 	|  	|
| | 1.1.3.7   | Update a user role permission via SOAP Service        | **Given** 	| A user role  is already provisioned exists                        | Not Started | 	|
| |            |                                                      | **When** 	| Details are entered to update the user role-permission by calling a SOAP Service 	|
| |            |                                                      | **Then** 	| The user role-permission should  be updated |             | 	|
| | 	|  	|
| | 1.1.3.8    | List the existing user roles                         | **Given** 	| A user role   already provisioned exists                        | Not Started | 	|
| |            |                                                      | **When** 	| A request is sent to get a list of available user roles by calling a SOAP Service |             | 	|
| |            |                                                      | **Then** 	| The user role should  be listed |             | 	|
| |            |                                                      | 	|  	|
| |                                                                       |             | 	|  	|
