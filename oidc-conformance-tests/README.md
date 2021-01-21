This folder contains the json config files and python scripts that can be used to automate OIDC conformance testing. These scripts will be used by the oidc-conformace-test github workflow.

## configure_is.py

**Input** - path to product-is zip file

**Output** - IS_config.json file for each test plan

This script will build and run IS as well as make necessary configurations in the IS to run OIDC conformance tests. Performs following configurations in the IS

* Register two service providers for each OIDC test plan
* Advance authentication configuration
* Add claim values to service providers

## start_conformance_suite.py

**Input** - Path to docker-compose file of OIDC conformance suite

This script will start OIDC conformance suite using docker-compose locally

## export_results.py

**Input** - Url of conformance suite(running locally)

This script use OIDC conformance suite APIs to export results of completed test plans as zip files

## send_notification.py

**Inputs** - Url of conformance suite(running locally), GitHub workflow run number, GitHub workflow status, GitHub repository name, GitHub workflow run id, google chat web hook url

This script will obtain counts of test cases with failures and warnings using the API of OIDC conformance. Then send a notification with test summary to google chat using provided webhook

## test_runner.sh

This script is used to run OIDC test plans by calling run-test-plan.py which is part of OIDC conformance suite. Currently there are six folders corresponding to six test plans
* basic
* implicit
* hybrid
* form post basic
* form post implicit
* form post hybrid

JSON config files for each test plans are located within these folders. 
