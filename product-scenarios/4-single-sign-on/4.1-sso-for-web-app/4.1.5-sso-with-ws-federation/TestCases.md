# Test Case Template

### 2.1.1.1 Sing sign-on with OAuth

| TestCaseID | TestCase                                                                 | Test Behaviour                                                                                                                                                                                            
|------------|--------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
| 2.1.1.3.1  | Create Service Provider Application for Passive STS                      | **Given**: Test environment is set properly</br> **When** : An application not available in IS</br> **Then** : The application  is created
| 2.1.1.3.2  | Update Service Provider Application for Passive STS config               | **Given**: Test environment is set properly</br> **When** : An application is created</br> **Then** : Created Application is updated with Passive STS config  
| 2.1.1.3.3  | Update service provider with claim configurations                        | **Given**: Test environment is set properly</br> **When** : AAn application is created</br> **Then** :  Claim mappings are updated
| 2.1.1.3.4  | Invoke PassiveSTSSampleApp                                               | **Given**: Test environment is set properly</br> **When** : A SamplePassiveSTSAPP is deployed</br> **Then** :   
| 2.1.1.3.5  | Send Passive STS login post request                                      | **Given**: Test environment is set properly</br> **When** : A SamplePassiveSTSAPP is deployed and authentication flow is initiated. Submit credentials for login.</br> **Then** :   User logs in successfully. 'wresult' is sent back to SP. Requested claims are returned in assertion.
| 2.1.1.3.6  | Test PassiveSTS SAML2 Assertion                                          | **Given**: Test environment is set properly</br> **When** : Successful login session is available in the client.</br> **Then** :  'wresult' is sent back to SP, with SAML2 assertion.
| 2.1.1.3.7  | Test PassiveSTS SAML2 Assertion with WReply URL in passive-sts request   | **Given**: Test environment is set properly</br> **When** : Successful login session is available in the client.</br> **Then** :  'wresult' is sent back to SP, with SAML2 assertion.
| 2.1.1.3.8  | Test Soap fault in case invalid WReply URL                               | **Given**: Test environment is set properly</br> **When** : Successful login session is available in the client.</br> **Then** :  'wresult' is sent back to SP, with SOAP Fault.
