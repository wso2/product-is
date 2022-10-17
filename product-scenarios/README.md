# Identity Server Product Scenario Tests

## Running Scenario Tests Locally

### Setting Up
* If you are planning to run scenario tests
  ```git clone  https://github.com/wso2/product-is.git```
* Checkout to necessary branch/tag
* CD to product-is > product-scenarios
* Create a new file named infrastructure.properties
* Add the below 2 configurations to that file
```
ISHttpsUrl=https://localhost:9443 
ISSamplesHttpUrl=http://localhost:8080
```
* Up the Identity Server pack that you want to run the test against.

### Deploying Test Samples
To deploy the samples traverse to product-scenarios/test-resources and run the scripts.
* For MacOS
```
sh deploy-samples-mac.sh <ISHttpsURL>
```
* For Linux
```
sh deploy-samples-linux.sh <ISHttpsURL>
```

### Running Tests
* Navigate to product-is > product-scenarios and run the test.sh file with below command to execute the product scenario tests. In the below command replace the path for input-dir and output-dir to a file path in your machine.

```
./test.sh --input-dir <INPUT_DIR> --output-dir <OUTPUT_DIR>
Ex: ./test.sh --input-dir $PWD --output-dir $PWD
```

* Sample Command

```
./test.sh --input-dir /home/ubuntu/code/product-is/product-scenarios --output-dir /home/centos/code/product-is/product-scenarios
```

# Notes
By default Tests will be executed against H2 DB. If you want to execute the tests locally against other DB types you need to do the necessary datasource configurations in the deployment.toml file to point to another DB type in the IS pack.
TG execution will happen against jdbc usestore and different db types.
You can run only a part of the tests by commenting out unnecessary components from the root pom file.


## [01. Manage Users and Roles](1-manage-users-and-roles/README.md)

## [02. User account management](2-user-account-management/README.md)

## [03. Identity Provisioning ](3-identity-provisioning/README.md)

## [04. Single Sign-On ](4-single-sign-on/README.md)

## [05. SSO with Identity Federation ](5-sso-with-identity-federation/README.md)

## [06. Adaptive and Strong Authentication ](6-adaptive-and-strong-authentication/README.md)

## [07. Single Sign-On with Controlled Access ](7-single-sign-on-with-controlled-access/README.md)

## [08. Single Sign-On with Delegated Access Control ](8-single-sign-on-with-delegated-access-control/README.md)

## [09. Access Delegation ](9-access-delegation/README.md)

## [10. Fine grained access control ](10-fine-grained-access-control/README.md)

## [11. Monitor User Logins and Sessions ](11-monitor-user-logins-and-sessions/README.md)
