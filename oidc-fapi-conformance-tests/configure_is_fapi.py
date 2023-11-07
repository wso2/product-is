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
import constants_fapi as constants
import base64
from config import browser_configuration

# path to product is zip file
path_to_is_zip = str(sys.argv[1])
print("Path to zip: ", path_to_is_zip)

def decode_secret(secret):
    decoded_string=base64.b64decode(secret+"=").decode("utf-8")
    decoded_json = json.loads(decoded_string)
    return decoded_json

client1_jwks_en = str(sys.argv[2])
client2_jwks_en = str(sys.argv[3])
client1_mtls_en = str(sys.argv[4])
client2_mtls_en = str(sys.argv[5])

client1_jwks = decode_secret(client1_jwks_en)
client2_jwks = decode_secret(client2_jwks_en)
client1_mtls = decode_secret(client1_mtls_en)
client2_mtls = decode_secret(client2_mtls_en)

resource_url = str(sys.argv[6])

# use dcr to register a client
def dcr(client_name, redirect_uris, jwks_uri, auth_method, client_id, client_secret):
    print(">>> Making DCR Request.")
    print(">>> SP Name: " + client_name)
    DCR_BODY = constants.DCR_BODY
    DCR_BODY['client_name'] = client_name
    DCR_BODY['redirect_uris'] = redirect_uris
    DCR_BODY['jwks_uri'] = jwks_uri
    DCR_BODY['token_endpoint_auth_method'] = auth_method
    DCR_BODY['ext_param_client_id'] = client_id
    DCR_BODY['ext_param_client_secret'] = client_secret
    try:
        response = requests.post(url=constants.DCR_ENDPOINT, headers=constants.HEADERS_WITH_AUTH,
                                 data=json.dumps(DCR_BODY), verify=False)
        response.raise_for_status()
    except HTTPError as http_error:
        print(http_error)
        print(response.text)
        exit(1)
    except Exception as error:
        print("\nError occurred: " + str(error))
        exit(1)
    else:
        print(">>> ClientID: " + str(response.json()['client_id']))

# get application id of the service provider using the SP name
def get_application_id_by_sp_name(name):
    try:
        response = requests.get(url=constants.APPLICATION_ENDPOINT + "?filter=name+eq+" + name,
                                headers=constants.HEADERS_WITH_AUTH, verify=False)
        response.raise_for_status()
        response_json = json.loads(response.content)
        application_id = response_json['applications'][0]['id']
        print(">>> Application ID: " + application_id)
        return application_id
    except HTTPError as http_error:
        print(http_error)
        print(response.text)
        exit(1)
    except Exception as error:
        print("Error occurred: " + str(error))
        exit(1)

# returns service provider details with given application id
def get_service_provider_details(application_id):
    try:
        response = requests.get(url=constants.APPLICATION_ENDPOINT + "/" + application_id + "/inbound-protocols/oidc",
                                headers=constants.HEADERS_WITH_AUTH, verify=False)
        response.raise_for_status()
        response_json = json.loads(response.content)
        return response_json
    except HTTPError as http_error:
        print(http_error)
        print(response.text)
        exit(1)
    except Exception as error:
        print("Error occurred: " + str(error))
        exit(1)

# set access token type of the service provider to JWT
def set_service_provider_access_token_type(application_id, app_details, token_type):
    body = app_details
    body['accessToken']['type'] = token_type

    print(">>> Set access token type...")
    try:
        response = requests.put(url=constants.APPLICATION_ENDPOINT + "/" + application_id + "/inbound-protocols/oidc",
                                headers=constants.HEADERS_WITH_AUTH, data=json.dumps(body), verify=False)
        response.raise_for_status()
    except HTTPError as http_error:
        print(http_error)
        print(response.text)
        exit(1)
    except Exception as error:
        print("\nError occurred: " + str(error))
        exit(1)
    else:
        print(">>> Access token type saved successfully.")

# perform advanced authentication configuration for given service provider
def configure_acr(application_id):
    body = json.dumps(constants.ACR)

    print(">>> Setting up advanced authentication scripts...")
    try:
        response = requests.patch(url=constants.APPLICATION_ENDPOINT + "/" + application_id, 
                                  headers=constants.HEADERS_WITH_AUTH, data=body, verify=False)
        response.raise_for_status()
    except HTTPError as http_error:
        print(http_error)
        print(response.text)
        exit(1)
    except Exception as error:
        print("\nError occurred: " + str(error))
        exit(1)
    else:
        print(">>> ACR script saved successfully.")

# unpack product-is zip file and run
def unpack_and_run(zip_file_name):
    try:
        # extract IS zip
        with ZipFile(zip_file_name, 'r') as zip_file:
            print(">>> Extracting " + zip_file_name)
            zip_file.extractall()

        dir_name = ''
        # start identity server
        print("\n>>> Starting Server")
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
                print("\n>>> Server Started")
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
        "description": "FAPI conformance suite for wso2 identity server.",
        "server": {
            "discoveryUrl": constants.BASE_URL + "/oauth2/token/.well-known/openid-configuration"
        },
        "resource": {
            "resourceUrl": resource_url
        },
        "client": {
            "client_id": service_provider_1['clientId'],
            "scope": "openid profile abc",
            "jwks": client1_jwks
        },
        "client2": {
            "client_id": service_provider_2['clientId'],
            "scope": "openid profile abc",
            "jwks": client2_jwks
        },
        "mtls": client1_mtls,
        "mtls2": client2_mtls,
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

warnings.filterwarnings("ignore")

if not is_process_running("wso2server"):
    unpack_and_run(path_to_is_zip)
else:
    print("\n>>> IS already running ...")
print ("==============================================\n")

# CREATE AND CONFIGURE APP 1
dcr(constants.APP_1_CLIENT_NAME, constants.APP_1_REDIRECT_URIS, constants.APP_1_JWKS_URI, constants.APP_1_AUTH_METHOD,
    constants.APP_1_CLIENT_ID, constants.APP_1_CLIENT_SECRET)
app1_id = get_application_id_by_sp_name(constants.APP_1_CLIENT_NAME)
app1_details = get_service_provider_details(app1_id)
set_service_provider_access_token_type(app1_id, app1_details, "JWT")
configure_acr(app1_id)

print("\n")

# CREATE AND CONFIGURE APP 2
dcr(constants.APP_2_CLIENT_NAME, constants.APP_2_REDIRECT_URIS, constants.APP_2_JWKS_URI, constants.APP_2_AUTH_METHOD,
    constants.APP_2_CLIENT_ID, constants.APP_2_CLIENT_SECRET)
app2_id = get_application_id_by_sp_name(constants.APP_2_CLIENT_NAME)
app2_details = get_service_provider_details(app2_id)
set_service_provider_access_token_type(app2_id, app2_details, "JWT")
configure_acr(app2_id)

# generate config file for OIDC FAPI test plan
json_config_builder(app1_details, app2_details, "config/IS_config_fapi.json", "basic")

# If the SP app auth method is MTLS, add relevant CA certs to IS keystore