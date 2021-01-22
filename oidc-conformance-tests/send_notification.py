import export_results
import requests
import json
import warnings
import sys

conformance_suite_url = sys.argv[1]
github_run_number = str(sys.argv[2])
workflow_status = sys.argv[3]
github_repository_name = sys.argv[4]
github_run_id = str(sys.argv[5])
google_webhook_url = sys.argv[6]

failed_count = 0
warnings_count = 0
total_tests_count = 0
warnings.filterwarnings("ignore")
plan_list = json.loads(requests.get(url=conformance_suite_url + "/api/plan?length=50", verify=False).content)
# loop through all test plans and count fails, warnings and total test cases
for test_plan in plan_list['data']:
    failed_tests_list = export_results.get_failed_tests(test_plan)
    if len(failed_tests_list['fails']) > 0:
        failed_count += len(failed_tests_list['fails'])
        total_tests_count += len(failed_tests_list['fails'])
    if len(failed_tests_list['warnings']) > 0:
        warnings_count += len(failed_tests_list['warnings'])
        total_tests_count += len(failed_tests_list['warnings'])
    if len(failed_tests_list['others']) > 0:
        total_tests_count += len(failed_tests_list['others'])

# send google chat notification
request_body = {
    'text': 'Hi all, OIDC conformance test run #' + github_run_number + ' completed with status: '+ workflow_status +
            ' \n Total test cases: ' + str(total_tests_count) +
            ' \n Failed test cases: ' + str(failed_count) +
            ' \n Test cases with warnings: ' + str(warnings_count) +
            ' \n https://github.com/' + github_repository_name + '/actions/runs/' + github_run_id
}
response = requests.post(google_webhook_url, json=request_body)
print(response.text)
