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

import re
import warnings
import psutil
import requests
import json
from urllib.parse import urlencode
from zipfile import ZipFile
import subprocess
import os
import sys
from requests.exceptions import HTTPError
import constants
from config import browser_configuration

headers = {
    'Content-Type': 'application/json',
    'Connection': 'keep-alive',
    'Authorization': 'Bearer '
}
# path to product is zip file
path_to_is_zip = str(sys.argv[1])


# use dcr to register a client
def dcr():
    print("\nDynamic Client Registration")
    try:
        response = requests.post(url=constants.DCR_ENDPOINT, headers=constants.DCR_HEADERS,
                             data=json.dumps(constants.DCR_BODY), verify=False)
        response.raise_for_status()
    except HTTPError as http_error:
        print(http_error)
        print(response.text)
        exit(1)
    except Exception as error:
        print("\nError occurred: " + str(error))
        exit(1)
    else:
        print("\nCompleted with status: " + str(response.status_code))
        print(response.text)


# obtain an access token with given client details and scope
def get_access_token(client_id, client_secret, scope, url):
    body = {
        'grant_type': 'password',
        'username': 'admin',
        'password': 'admin',
        'client_id': client_id,
        'client_secret': client_secret,
        'scope': scope
    }

    token_headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Connection': 'keep-alive',
    }
    print("\nGetting access token")
    try:
        response = requests.post(url=url, headers=token_headers, data=urlencode(body), verify=False)
        response.raise_for_status()
        response_map = json.loads(response.content)
        print(response_map)
        if response_map['access_token']:
            return response_map['access_token']
        else:
            print("\nError: No access token found")
    except HTTPError as http_error:
        print(http_error)
        print(response.text)
        exit(1)
    except Exception as error:
        print("\nError occurred: " + str(error))
        exit(1)


# returns service provider details with given application id
def get_service_provider_details(application_id):
    try:
        response = requests.get(url=constants.APPLICATION_ENDPOINT + "/" + application_id + "/inbound-protocols/oidc",
                                headers=headers, verify=False)
        response.raise_for_status()
        response_json = json.loads(response.content)
        return {"clientId": response_json['clientId'], "clientSecret": response_json['clientSecret'], "applicationId": application_id}
    except HTTPError as http_error:
        print(http_error)
        print(response.text)
        exit(1)
    except Exception as error:
        print("Error occurred: " + str(error))
        exit(1)


# register a service provider with given configuration
def register_service_provider(config_file_path):
    with open(config_file_path) as file:
        body = json.load(file)
    name = body["name"]

    print("\nRegistering service provider " + name)
    try:
        response = requests.post(url=constants.APPLICATION_ENDPOINT, headers=headers, data=json.dumps(body), verify=False)
        response.raise_for_status()
        print("Service provider " + name + " registered")
        response = requests.get(url=constants.APPLICATION_ENDPOINT + "?filter=name+eq+" + name, headers=headers,
                            verify=False)
        response.raise_for_status()
        response_map = json.loads(response.content)
        print(response_map)
        if response_map['count'] == 0:
            print("error application not found")
        else:
            return get_service_provider_details(response_map['applications'][0]['id'])
    except HTTPError as http_error:
        print(http_error)
        print(response.text)
        exit(1)
    except Exception as error:
        print("\nError occurred: " + str(error))
        exit(1)


# set values for user claims using given config file
def set_user_claim_values(config_file_path):
    with open(config_file_path) as file:
        body = json.load(file)

    print("\nSetting user claim values")
    try:
        response = requests.patch(url=constants.BASE_URL + "/scim2/Me", headers=headers, data=json.dumps(body), verify=False)
        response.raise_for_status()
    except HTTPError as http_error:
        print(http_error)
        print(response.text)
        exit(1)
    except Exception as error:
        print("\nError occurred: " + str(error))
        exit(1)
    else:
        print("\nCompleted with status: " + str(response.status_code))
        print(response.text)


# change the local mapping of a given claim
def change_local_claim_mapping(body, url):
    print("\nChanging local claim mapping for " + body['claimURI'])
    json_body = json.dumps(body)
    try:
        response = requests.put(url=url, headers=headers, data=json_body, verify=False)
        response.raise_for_status()
    except HTTPError as http_error:
        print(http_error)
        print(response.text)
        exit(1)
    except Exception as error:
        print("\nError occurred: " + str(error))
        exit(1)
    else:
        print("\nCompleted successfully with status: " + str(response.status_code))
        print(response.text)


# add claims to the service provider with given id using given claims config file
def add_claim_service_provider(application_id, config_file_path):
    with open(config_file_path) as file:
        body = json.load(file)

    print("\nAdding claims to service provider")
    try:
        response = requests.patch(url=constants.APPLICATION_ENDPOINT + "/" + application_id, headers=headers, data=json.dumps(body),
                                verify=False)
        response.raise_for_status()
    except HTTPError as http_error:
        print(http_error)
        print(response.text)
        exit(1)
    except Exception as error:
        print("\nError occurred: "+ str(error))
        exit(1)
    else:
        print("\nCompleted successfully with status: " + str(response.status_code))
        print(response.text)


# perform advanced authentication configuration for given service provider using given config file
def configure_acr(application_id, config_file_path):
    with open(config_file_path) as file:
        body = json.load(file)

    print("\nSetup advanced authentication scripts")
    try:
        response = requests.patch(url=constants.APPLICATION_ENDPOINT + "/" + application_id, headers=headers,
                                  data=json.dumps(body), verify=False)
        response.raise_for_status()
    except HTTPError as http_error:
        print(http_error)
        print(response.text)
        exit(1)
    except Exception as error:
        print("\nError occurred: " + str(error))
        exit(1)
    else:
        print("\nCompleted successfully with status: " + str(response.status_code))
        print(response.text)


# update the scope with given scope id
def edit_scope(scope_id, body):
    print("\nChanging scope: " + scope_id)
    json_body = json.dumps(body)
    try:
        response = requests.put(url=constants.BASE_URL + "/api/server/v1/oidc/scopes/" + scope_id, headers=headers,
                                data=json_body, verify=False)
        response.raise_for_status()
    except HTTPError as http_error:
        print(http_error)
        print(response.text)
        exit(1)
    except Exception as error:
        print("\nError occurred: " + str(error))
        exit(1)
    else:
        print("\nCompleted successfully with status: " + str(response.status_code))
        print(response.text)


# unpack product-is zip file and run
def unpack_and_run(zip_file_name):
    try:
        # extract IS zip
        with ZipFile(zip_file_name, 'r') as zip_file:
            print("Extracting " + zip_file_name)
            zip_file.extractall()

        dir_name = ''
        # start identity server
        print("\nStarting Server")
        dir_list = os.listdir()
        r = re.compile('(?=^wso2is)(?=^((?!zip).)*$)')
        for line in dir_list:
           if r.match(line):
                print(line)
                dir_name = line
                break

        os.chmod("./" + dir_name + "/bin/wso2server.sh", 0o777)
        process = subprocess.Popen("./" + dir_name + "/bin/wso2server.sh", stdout=subprocess.PIPE)
        while True:
            output = process.stdout.readline()
            if b'..................................' in output:
                print("\nServer Started")
                break
            if output:
                print(output.strip())
        rc = process.poll()
        return rc
    except FileNotFoundError:
        print()
        raise


# creates the IS_config.json file needed to run OIDC test plans and save in the given path
def json_config_builder(service_provider_1, service_provider_2, output_file_path, plan_name):
    config = {
        "alias": constants.ALIAS,
        "server": {
            "issuer": constants.BASE_URL + "/oauth2/token",
            "jwks_uri": constants.BASE_URL + "/oauth2/jwks",
            "authorization_endpoint": constants.BASE_URL + "/oauth2/authorize",
            "token_endpoint": constants.BASE_URL + "/oauth2/token",
            "userinfo_endpoint": constants.BASE_URL + "/oauth2/userinfo",
            "acr_values": "acr1"
        },
        "client": {
            "client_id": service_provider_1['clientId'],
            "client_secret": service_provider_1['clientSecret']
        },
        "client2": {
            "client_id": service_provider_2['clientId'],
            "client_secret": service_provider_2['clientSecret']
        },
        "client_secret_post": {
            "client_id": service_provider_1['clientId'],
            "client_secret": service_provider_1['clientSecret']
        },
        "browser": browser_configuration.CONFIG[plan_name]["browser"],
        "override": browser_configuration.CONFIG[plan_name]["override"]
    }

    json_config = json.dumps(config, indent=4)
    f = open(output_file_path, "w")
    f.write(json_config)
    f.close()


# returns true if the process with given name is running
def is_process_running(process_name):
    process_list = []

    # Iterate over the all the running process
    for proc in psutil.process_iter():
        try:
            pinfo = proc.as_dict(attrs=['pid', 'name', 'create_time'])
            # Check if process name contains the given name string.
            if process_name.lower() in pinfo['name'].lower():
                process_list.append(pinfo)
        except (psutil.NoSuchProcess, psutil.AccessDenied, psutil.ZombieProcess):
            pass

    if len(process_list) > 0:
        return True
    else:
        return False


# perform all configurations and generate config file for a single OIDC test plan
def generate_config_for_plan(service_provider1_config, service_provider2_config, output_file_path, plan_name):
    service_provider_1 = register_service_provider(service_provider1_config)
    service_provider_2 = register_service_provider(service_provider2_config)
    print(service_provider_1)
    print(service_provider_2)

    add_claim_service_provider(service_provider_1['applicationId'], "./config/service_provider_claim_config.json")
    add_claim_service_provider(service_provider_2['applicationId'], "./config/service_provider_claim_config.json")

    configure_acr(service_provider_1['applicationId'], "./config/acr_config.json")
    configure_acr(service_provider_2['applicationId'], "./config/acr_config.json")

    json_config_builder(service_provider_1, service_provider_2, output_file_path, plan_name)


warnings.filterwarnings("ignore")
if not is_process_running("wso2server"):
    unpack_and_run(path_to_is_zip)
else:
    print("IS already running")


dcr()
access_token = get_access_token(constants.DCR_CLIENT_ID, constants.DCR_CLIENT_SECRET, constants.SCOPES,
                                 constants.TOKEN_ENDPOINT)
headers['Authorization'] = "Bearer " + access_token

set_user_claim_values("./config/user_claim_value_config.json")

# change phone number to mobile
change_local_claim_mapping(
    {
        "claimURI": "phone_number",
        "mappedLocalClaimURI": "http://wso2.org/claims/mobile"
    },
    constants.BASE_URL + "/api/server/v1/claim-dialects/aHR0cDovL3dzbzIub3JnL29pZGMvY2xhaW0/claims/cGhvbmVfbnVtYmVy")

# change website from url to organization
change_local_claim_mapping(
    {
        "claimURI": "website",
        "mappedLocalClaimURI": "http://wso2.org/claims/organization"
    },
    constants.BASE_URL + "/api/server/v1/claim-dialects/aHR0cDovL3dzbzIub3JnL29pZGMvY2xhaW0/claims/d2Vic2l0ZQ")

# change name from fullname to formatted
change_local_claim_mapping(
    {
        "claimURI": "name",
        "mappedLocalClaimURI": "http://wso2.org/claims/formattedName"
    },
    constants.BASE_URL + "/api/server/v1/claim-dialects/aHR0cDovL3dzbzIub3JnL29pZGMvY2xhaW0/claims/bmFtZQ")

edit_scope("openid", {
    "claims": [
        "sub"
    ],
    "description": "",
    "displayName": "openid"
})

generate_config_for_plan("./basic/config/service_provider1_config.json",
                         "./basic/config/service_provider2_config.json",
                         "basic/IS_config_basic.json",
                         "basic")

generate_config_for_plan("./implicit/config/service_provider1_config.json",
                         "./implicit/config/service_provider2_config.json",
                         "implicit/IS_config_implicit.json",
                         "implicit")

generate_config_for_plan("./hybrid/config/service_provider1_config.json",
                         "./hybrid/config/service_provider2_config.json",
                         "hybrid/IS_config_hybrid.json",
                         "hybrid")

generate_config_for_plan("./formpost-basic/config/service_provider1_config.json",
                         "./formpost-basic/config/service_provider2_config.json",
                         "formpost-basic/IS_config_formpost_basic.json",
                         "formpost-basic")

generate_config_for_plan("./formpost-implicit/config/service_provider1_config.json",
                         "./formpost-implicit/config/service_provider2_config.json",
                         "formpost-implicit/IS_config_formpost_implicit.json",
                         "formpost-implicit")

generate_config_for_plan("./formpost-hybrid/config/service_provider1_config.json",
                         "./formpost-hybrid/config/service_provider2_config.json",
                         "formpost-hybrid/IS_config_formpost_hybrid.json",
                         "formpost-hybrid")
