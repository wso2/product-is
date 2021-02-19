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

import subprocess
import os
import sys


def start(file):
    try:
        os.chmod(file, 0o777)
        process = subprocess.Popen("sudo -E docker-compose -f " + file + " up", shell=True, stdout=subprocess.PIPE)
        while True:
            output = process.stdout.readline()
            if b'conformance-suite_server_1 exited with code 1' in output:
                raise Exception("Conformance suite failed to start")
            elif b'Starting application' in output:
                print("Server Started")
                break
            elif output:
                print(output.strip())
        rc = process.poll()
        return rc
    except FileNotFoundError:
        print(file + " not found")
        raise

# takes OIDC conformance suite url as an argument
start(sys.argv[1])
