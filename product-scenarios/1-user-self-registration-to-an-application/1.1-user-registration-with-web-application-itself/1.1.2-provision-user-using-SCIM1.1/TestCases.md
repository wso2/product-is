# Test Case Template

### 1.1.2 Provision user in identity server using SCIM1.1 API

| TestCaseID | TestCase                                                      | Test steps                                                              | Status      |
|------------|---------------------------------------------------------------|-------------------------------------------------------------------------|-------------|
| 1.1.2.1    | Provision a single user in IS using the SCIM1.1 API           | **Given**: Test environment is set properly                             | Automated   |
|            |                                                               | **When** : A request sends to create a user using SCIM1.1 API           |             |
|            |                                                               | **Then** : The user should be provisioned in IS                         |             |
| 1.1.2.2    | Provision a user with malformed request using the SCIM1.1 API | **Given**: A malformed requests payload exists                          | Not Started |
|            |                                                               | **When** : A malformed request sends to create a user using SCIM1.1 API |             |
|            |                                                               | **Then** : The user shouldn't be created & valid error message appear   |             |

