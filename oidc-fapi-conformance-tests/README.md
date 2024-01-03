This folder contains the configuration files and scripts that can be used to automate OIDC FAPI conformance testing. These tests can be run using GitHub actions or locally.

## Testing using GitHub actions

OIDC FAPI conformance test workflow can be used to for this purpose.
1. Go to the Actions tab in the wso2/product-is github repository
2. Click on FAPI OIDC Conformance Test workflow
3. Click on Run workflow
4. You need to provide the product-is release tag version you want to test (by default, it builds the latest IS by source)
5. Also by default FAPI conformance suite is built from the latest released branch in https://gitlab.com/openid/conformance-suite.git. You can run against a specific conformance suite version also.
6. Set 'Send test results to email' to 'yes' if you want to send test summary to a list of pre-configured email addresses. (default is 'no')
7. Click on Run workflow
8. After tests are completed you can view test results on the test summary page
9.  Two types of artifacts are saved after the test execution is completed
   - test-logs - a log file is generated for each test plan. This log contains a summary of test cases with failures and warnings
   - test-results - a zip file is generated for each test plan. You can use a web browser to view a detailed report of the test plan by extracting this zip file

An email containing the same test summary will also be sent to a pre-configured list of email addresses. The sender email, password and the receiver emil list are the same as for OIDC action build. If you need to have a seperate receiver email list for FAPI Conformance action build,
* Create a github secret with name `FAPI_RECEIVER_LIST` and add the list of receiver emails seperated by commas.

Default configuration is to use Gmail SMTP server. You can change that by modifying `SMTP_SERVER` and `SMTP_SERVER_PORT` in `constants_fapi.py`

This workflow is scheduled to run daily at 08:30 UTC (2:00 AM SL time) and will also automatically trigger after a release or a pre-release.

To locally setup and run the test suite, follow the [fapi-oidc-conformance-test.yml](.github/workflows/fapi-oidc-conformance-test.yml) script and execute the steps.

## Test Profiles

Running fapi test profiles are in [test_runner_fapi.sh](oidc-fapi-conformance-tests/test_runner_fapi.sh) script. Currently Running profiles,
* private_key_jwt
* mtls

