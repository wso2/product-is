# ----------------------------------------------------------------------------
#  Copyright 2021 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

import json
import warnings
import requests
import sys

conformance_suite_url = sys.argv[1]


# export test results of a given test plan
def save_results(plan):
    response = requests.get(url=conformance_suite_url + "/api/plan/exporthtml/" + plan['_id'], stream=True, verify=False)
    with open("./" + plan['planName'] + "_test_results.zip", 'wb') as fileDir:
        for chunk in response.iter_content(chunk_size=128):
            fileDir.write(chunk)


# return names of all failed, warnings and other test cases of a given test plan
def get_failed_tests(plan):
    test_fails = []
    test_warnings = []
    test_others = []
    test_log = json.loads(requests.get(url=conformance_suite_url + "/api/log?length=10000&search=" + plan['_id'], verify=False).content)
    for test in test_log['data']:
        if "result" in test and test['result'] == "FAILED":
            test_fails.append('Test Name: ' + test['testName'] + '  id: ' + test['testId'])
        elif "result" in test and test['result'] == "WARNING":
            test_warnings.append('Test Name: ' + test['testName'] + '  id: ' + test['testId'])
        else:
            test_others.append('Test Name: ' + test['testName'] + '  id: ' + test['testId'])
    return {
        'fails': test_fails,
        'warnings': test_warnings,
        'others': test_others
    }


if __name__ == '__main__':
    failed_plan_details = dict()
    contains_fails = False
    warnings.filterwarnings("ignore")
    plan_list = json.loads(requests.get(url=conformance_suite_url + "/api/plan?length=50", verify=False).content)
    print("======================\nExporting test results\n======================")
    for test_plan in plan_list['data']:
        save_results(test_plan)
        failed_tests_list = get_failed_tests(test_plan)
        if len(failed_tests_list['fails']) > 0 or len(failed_tests_list['warnings']) > 0:
            failed_plan_details[test_plan['planName']] = failed_tests_list
            if len(failed_tests_list['fails']) > 0:
                contains_fails = True

    if failed_plan_details:
        print("Following tests have fails/warnings\n===========================")
        for test_plan in failed_plan_details:
            failed_count = len(failed_plan_details[test_plan]['fails'])
            warnings_count = len(failed_plan_details[test_plan]['warnings'])
            success_count = len(failed_plan_details[test_plan]['others'])
            total_count = failed_count + warnings_count + success_count
            print("\n"+test_plan+"\n-----------------------------------")
            print("Total Test Cases: " + str(total_count))
            print("Successful: " + str(success_count))
            print("Warnings: " + str(warnings_count))
            print("Failures: " + str(failed_count))
            print("\nFailed Test Cases\n-----")
            print(*failed_plan_details[test_plan]['fails'], sep="\n")
            print("\nTest Cases with Warnings\n--------")
            print(*failed_plan_details[test_plan]['warnings'], sep="\n")
        if contains_fails:
            sys.exit(1)
        else:
            sys.exit(0)
    else:
        print("\nAll test plans finished successfully")
        sys.exit(0)
