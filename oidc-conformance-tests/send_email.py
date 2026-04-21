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
import requests
import json
import warnings
import sys
import smtplib, ssl
import constants

conformance_suite_url = sys.argv[1]
github_run_number = str(sys.argv[2])
workflow_status = sys.argv[3]
github_repository_name = sys.argv[4]
github_run_id = str(sys.argv[5])
sender_email = str(sys.argv[6])
password = str(sys.argv[7])
receiver_list = sys.argv[8].split(',')
wso2_is_version = str(sys.argv[9])


failed_count = 0
errors_count = 0
warnings.filterwarnings("ignore")
plan_list = json.loads(requests.get(url=conformance_suite_url + "/api/plan?length=50", verify=False).content)
# loop through all test plans and count fails and errors
for test_plan in plan_list['data']:
    failed_tests_list = export_results.get_failed_tests(test_plan)
    if len(failed_tests_list['fails']) > 0:
        failed_count += len(failed_tests_list['fails'])
    if len(failed_tests_list['errors']) > 0:
        errors_count += len(failed_tests_list['errors'])

port = constants.SMTP_SERVER_PORT
smtp_server = constants.SMTP_SERVER
message = """\
Subject: OIDC Conformance Test #""" + github_run_number + """ Summary
From: """ + sender_email + """
To: """ + sys.argv[8] + """

OIDC conformance  test run # """ + github_run_number + """ completed with status: """ + workflow_status + """
Identity Server Version: """ + wso2_is_version + """
Failed test cases: """ + str(failed_count) + """
Test cases with errors: """ + str(errors_count) + """
https://github.com/""" + github_repository_name + """/actions/runs/""" + github_run_id

context = ssl.create_default_context()
with smtplib.SMTP_SSL(smtp_server, port, context=context) as server:
    server.login(sender_email, password)
    server.sendmail(sender_email, receiver_list, message)
    server.close()
