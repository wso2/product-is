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

import export_results
from httplib2 import Http
import requests
import json
import warnings
import sys

conformance_suite_url = sys.argv[1]
github_run_number = str(sys.argv[2])
workflow_status = sys.argv[3]
github_repository_name = sys.argv[4]
github_run_id = str(sys.argv[5])
google_chat_webhook = sys.argv[6]
wso2_is_version = sys.argv[7]
workflow_name = sys.argv[8]

failed_count = 0
errors_count = 0

warnings.filterwarnings("ignore")

# Try to fetch plans; mark failure and include error if unreachable
unreachable_error = None
try:
    resp = requests.get(
        url=conformance_suite_url + "/api/plan?length=50",
        verify=False,
        timeout=5
    )
    resp.raise_for_status()
    plan_list = json.loads(resp.content)
except Exception as e:
    unreachable_error = f"Conformance Suite is unreachable at {conformance_suite_url}. Error: {type(e).__name__}"
    plan_list = {"data": []}
    workflow_status = "failure"

# loop through all test plans and count fails and errors
for test_plan in plan_list['data']:
    failed_tests_list = export_results.get_failed_tests(test_plan)
    if len(failed_tests_list['fails']) > 0:
        failed_count += len(failed_tests_list['fails'])
    if len(failed_tests_list['errors']) > 0:
        errors_count += len(failed_tests_list['errors'])

font_color = '#009944'
if workflow_status == 'failure':
    font_color = '#ff0000'

message_headers = {'Content-Type': 'application/json; charset=UTF-8'}
error_line = f"\nError: {unreachable_error}" if unreachable_error else ""
message = {
    "cards": [
        {
            "header": {
                "title": workflow_name,
                "subtitle": "GitHub Action #" + github_run_number

            },
            "sections": [
                {
                    "widgets": [
                        {
                            "textParagraph": {
                                "text": f"Identity Server version: <b>{wso2_is_version}</b>"
                            }
                        },
                        {
                            "textParagraph": {
                                "text": f"Status: <b><font color= {font_color}> {workflow_status}"
                                        f"</font></b></br>\nFailed test cases: {str(failed_count)}" 
                                        f"\nTest cases with errors: {str(errors_count)}"
                                        f"{error_line}"
                            }
                        },
                        {
                            "keyValue": {
                                "topLabel": "Build Job:",
                                "content": "GitHub Action",
                                "contentMultiline": "false",
                                "bottomLabel": "",
                                "button": {
                                    "textButton": {
                                        "text": "VIEW",
                                        "onClick": {
                                            "openLink": {
                                                "url": f"https://github.com/{github_repository_name}/actions/runs/"
                                                       f"{github_run_id}"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    ]
                }
            ]
        }
    ]
}

http_obj = Http()

try:
    # send google chat notification
    response = http_obj.request(
        uri=google_chat_webhook,
        method='POST',
        headers=message_headers,
        body=json.dumps(message),
    )
    print(response)
    response.raise_for_status()
except Exception as error:
    print(error)
