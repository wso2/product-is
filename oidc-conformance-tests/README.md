This folder contains the json config files and python scripts that can be used to automate OIDC conformance testing. These tests can be done using GitHub actions or by running locally

## Testing using GitHub actions

OIDC conformance test workflow can be used to for this purpose.
1. Go to Actions tab in your repository
2. Click on OIDC conformance test workflow
3. Click on Run workflow
4. You need to provide the tag name of the product-is version you want to test (default value is "v5.12.0-m6")
5. Set send google chat notification to false if you don't want a summary of test results to be sent to a configured google chat group. (default value is true)
6. Click on Run workflow
7. After tests are completed you can view test results on the test summary page
8. Two types of artifacts are saved after the test is completed
   - test-logs - a log file is generated for each test plan. This log contains a summary of test cases with failures and warnings
   - test-results - a zip file is generated for each test plan. You can use a web browser to view a detailed report of the test plan by extracting this zip file

**Note** : this workflow uses google chat API to send test summary details to a specified google chat. To configure this,
1. Obtain the webhook url of the desired group
2. Go to Settings => Secrets and click New Repository Secret
3. for name add "GOOGLE_WEBHOOK"
4. add the webhook url for value
5. Click add secret

## Testing locally

### Prerequisites 

* OIDC conformance suite should be running locally before testing can begin
* product-is zip file

You can use test_runner.sh script to start and configure identity server locally and run OIDC conformence tests
1. open test_runner.sh using a text editor and make the following modifications
   - assign url of conformance suite to CONFORMANCE_SUITE_URL. default is https://localhost:8443
   - assign the path of conformance-suite folder to CONFORMANCE_SUITE_PATH
   - set IS_LOCAL to true. default is false
2. Save and exit
3. Run the script using ```sudo bash test_runner.sh```

By default this script will run following test plans
* Basic certification test plan
* Implicit certification test plan
* Hybrid certification test plan
* Form post basic certification test plan
* Form post implicit certification test plan
* Form post hybrid certification test plan

After testing is completed test-logs and test-results files will be generated same as when running using GitHub actions.

Following is a description of what each script does
### configure_is.py

**Input** - path to product-is zip file

**Output** - IS_config.json file for each test plan

This script will build and run IS as well as make necessary configurations in the IS to run OIDC conformance tests. Performs following configurations in the IS

* Register two service providers for each OIDC test plan
* Advance authentication configuration
* Add claim values to service providers

### start_conformance_suite.py

**Input** - Path to docker-compose file of OIDC conformance suite

This script will start OIDC conformance suite using docker-compose locally

### export_results.py

**Input** - Url of conformance suite(running locally)

This script use OIDC conformance suite APIs to export results of completed test plans as zip files

### send_notification.py

**Inputs** - Url of conformance suite(running locally), GitHub workflow run number, GitHub workflow status, GitHub repository name, GitHub workflow run id, google chat web hook url

This script will obtain counts of test cases with failures and warnings using the API of OIDC conformance. Then send a notification with test summary to google chat using provided webhook

### test_runner.sh

This script is used to run OIDC test plans by calling run-test-plan.py which is part of OIDC conformance suite. Currently there are six folders corresponding to six test plans
* basic
* implicit
* hybrid
* form post basic
* form post implicit
* form post hybrid

JSON config files for each test plans are located within these folders. 
