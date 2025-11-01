# Contributing to WSO2 Identity Server

## Overview

This document provides guidelines for contributing to the WSO2 Identity Server project. We welcome contributions from the community, including bug fixes, new features, and documentation improvements.

## Project Resources

- **Home page:**           http://wso2.com/products/identity-server
- **Library:**             https://wso2.com/library/identity-and-access-management
- **Docs:**                https://is.docs.wso2.com/en/latest/
- **Issue Tracker:**       https://github.com/wso2/product-is/issues
- **Forums:**              http://stackoverflow.com/questions/tagged/wso2is/
- **IAM Developer List:**  iam-dev@wso2.org

## Building the Distribution from Source

To build the WSO2 Identity Server distribution from source, follow these steps:

1. Install **Java SE Development Kit 11 - 21**.
2. Install **Apache Maven 3.x.x** ([Download Maven](https://maven.apache.org/download.cgi#)).
3. Clone the repository: `https://github.com/wso2/product-is.git` or download the source.
4. From the `product-is` directory, run one of the following Maven commands:
    - `export JAVA_TOOL_OPTIONS="-Djdk.util.zip.disableZip64ExtraFieldValidation=true -Djdk.nio.zipfs.allowDotZipEntry=true"` (Linux/macOS)
      
      `set JAVA_TOOL_OPTIONS="-Djdk.util.zip.disableZip64ExtraFieldValidation=true -Djdk.nio.zipfs.allowDotZipEntry=true"` (Windows)
      _(To avoid the zip64 error)_
    - `mvn clean install`  
      _(Builds the binary and source distributions with tests)_
    - `mvn clean install -Dmaven.test.skip=true`  
      _(Builds the binary and source distributions without running any unit/integration tests)_
5. The binary distribution will be available in the `product-is/modules/distribution/target` directory.

## Installation and Running/Debugging

1. Extract the downloaded or built binary distribution zip file.
2. Run `wso2server.sh` (Linux/macOS) or `wso2server.bat` (Windows) from the `/bin` directory.
3. To run in debug mode, use the `-debug` option:  
   `wso2server.sh -debug` or `wso2server.bat -debug`
4. Developer and Administrator Console: [https://localhost:9443/console](https://localhost:9443/console)
5. End User Portal: [https://localhost:9443/myaccount](https://localhost:9443/myaccount)
6. For more information, see the [Installation Guide](https://is.docs.wso2.com/en/latest/deploy/get-started/install/).

## Contributing Code

1. Fork the repository on GitHub.
2. Create a new branch for your changes.
3. Make your changes and commit them with a descriptive message.
4. Push your changes to your forked repository.
5. Create a pull request against the `master` branch of the main repository.
6. Ensure your code adheres to the project's coding standards and passes all tests.
7. Provide a clear description of your changes and the reason for them in the pull request.
8. For more information, refer to [https://wso2.github.io/](https://wso2.github.io/)

## Running/Debugging the Integration Tests

- Build the product using the steps above.
- Navigate to `product-is/modules/integration/test-integration/test-backend` and run:  
  `mvn clean install`  
  _(This will run all the integration tests.)_
- To run a specific integration test:
    1. Open the `testng.xml` file in the `test-backend` directory.
    2. Comment out the tests you do not want to run.  
       _Do not comment out the listeners and the `is-tests-initialize` test group._
    3. Run `mvn clean install`.
- To run integration tests in debug mode:  
  `mvn -Dmaven.surefire.debug test`
- To run/debug integration tests and debug another repo:
    1. Open the `automation.xml` file in the `test-backend` directory.
    2. Uncomment the line:  
       `<parameter name="cmdArg" value="debug 5005" />`
    3. Run/debug the integration tests using:  
       1. `mvn clean install` to run the integration tests.
       2. `mvn -Dmaven.surefire.debug test` to run the integration tests in debug mode.
