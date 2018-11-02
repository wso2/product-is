# Test Case Template

### 1.1.1 Provision user in identity server using SCIM2 API

| TestCaseID | TestCase                                             | Test steps                                                            | Status      |
|------------|------------------------------------------------------|-----------------------------------------------------------------------|-------------|
| 1.1.1.1    | Provision a single user in IS using the SCIM2 API    | **Given**: Test environment is set properly                           | Automated   |
|            |                                                      | **When** : A request sends to create a user using SCIM2 API           |             |
|            |                                                      | **Then** : The user should be provisioned in IS                       |             |
| 1.1.1.2    | Provision user with malformed request using SCIM2    | **Given**: A malformed requests payload exists                        | Not Started |
|            |                                                      | **When** : A malformed request sends to create a user using SCIM2 API |             |
|            |                                                      | **Then** : The user shouldn't be created & valid error message appear |             |
|            |                                                      |                                                                       |             |

