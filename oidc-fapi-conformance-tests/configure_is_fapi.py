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
from config.client_configs import client_configs

def decode_secret(secret):
    decoded_string=base64.b64decode(secret+"=").decode("utf-8")
    decoded_json = json.loads(decoded_string)
    return decoded_json

# use dcr to register a client
def dcr(app_json):
    print(">>> Making DCR Request.")
    print(">>> SP Name: " + app_json.get("client_name"))
    DCR_BODY = constants.DCR_BODY
    DCR_BODY['client_name'] = app_json.get("client_name")
    DCR_BODY['redirect_uris'] = app_json.get("redirect_uris")
    DCR_BODY['jwks_uri'] = app_json.get("jwks_uri")
    DCR_BODY['token_endpoint_auth_method'] = app_json.get("token_endpoint_auth_method")
    DCR_BODY['token_endpoint_allow_reuse_pvt_key_jwt'] = app_json.get("token_endpoint_allow_reuse_pvt_key_jwt")
    DCR_BODY['ext_param_client_id'] = app_json.get("client_id")
    DCR_BODY['ext_param_client_secret'] = app_json.get("client_secret")
    DCR_BODY['require_pushed_authorization_requests'] = app_json.get("require_pushed_authorization_requests")
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

# set application scope claims for the given application, this is needed to allow or deny consent with provided scope
def set_application_scopes_for_consent(application_id):
    print(">>> Setting Application scope claims.")
    try:
        body = json.dumps(constants.SET_SCOPE_CLAIMS_BODY_PAYLOAD)
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
        print(">>> Application scope claims set successfully.")

#set hybrid flow response type for the application
def set_hybridFlow_config(application_id):
    print(">>> Setting hybrid flow configuration.")
    try:
        app_details = get_service_provider_details(application_id)
        app_details['hybridFlow'] = constants.ENABLE_HYBRID_FLOW
        body = json.dumps(app_details)
        response = requests.put(url=constants.APPLICATION_ENDPOINT + "/" + application_id + "/inbound-protocols/oidc",
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
        print(">>> Hybrid flow configuration added successfully.")

# Skip login consent is true by default, here we disable it to go consent flows
def disable_skipping_consent(application_id):
    print(">>> Setting Skip Login consent to false.")
    try:
        body = json.dumps(constants.DISABLE_SKIP_CONSENT_BODY_PAYLOAD)
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
        print(">>> Disabled Skip Login consent successfully.")

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

def addCertsToKeystore(rootCertPath, issuerCertPath, ISPath):
    print(">>> Adding certs to keystore...")
    try:
        # add root cert to keystore
        os.system("keytool -import -noprompt -trustcacerts -alias obroot -file " + rootCertPath + " -storetype JKS -keystore " + ISPath + "/repository/resources/security/client-truststore.jks -storepass wso2carbon")
        # add issuer cert to keystore
        os.system("keytool -import -noprompt -trustcacerts -alias obissuer -file " + issuerCertPath + " -storetype JKS -keystore " + ISPath + "/repository/resources/security/client-truststore.jks -storepass wso2carbon")
    except Exception as error:
        print("\nError occurred: " + str(error))
        exit(1)
    else:
        print(">>> Certs added to keystore successfully.")


# unpack product-is zip file and run
def unpack_and_run():
    # path to product is zip file
    zip_file_name = str(sys.argv[1])
    print("Path to zip: ", zip_file_name)
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

        # add root issuer certs to keystore
        ISPath = "./" + dir_name
        addCertsToKeystore("config/sandbox-certs/OB_SandBox_PP_Root_CA.cer", "config/sandbox-certs/OB_SandBox_PP_Issuing_CA.cer", ISPath)

        process = subprocess.Popen("./" + dir_name + "/bin/wso2server.sh", stdout=subprocess.PIPE)
        while True:
            output = process.stdout.readline()
            if b'WSO2 Carbon started' in output:
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
        "description": plan_name,
        "server": {
            "discoveryUrl": constants.BASE_URL + "/oauth2/token/.well-known/openid-configuration"
        },
        "resource": {
            "resourceUrl": constants.RESOURCE_ENDPOINT_URL
        },
        "client": {
            "client_id": service_provider_1['clientId'],
            "scope": "openid profile",
            "jwks": client_configs['client']['jwks']
        },
        "client2": {
            "client_id": service_provider_2['clientId'],
            "scope": "openid profile",
            "jwks": client_configs['client2']['jwks']
        },
        "mtls": client_configs['mtls'],
        "mtls2": client_configs['mtls2'],
        "browser": browser_configuration.CONFIG["basic"]["browser"],
        "override": browser_configuration.CONFIG["basic"]["override"]
    }

    json_config = json.dumps(config, indent=4)
    f = open(output_file_path, "w")
    f.write(json_config)
    f.close()

def createNewUser(username, password):
    try:
        body = {"userName":username,"password":password}
        response = requests.post(url=constants.CREATE_USER_ENDPOINT, headers=constants.HEADERS_WITH_AUTH,
                                 data=json.dumps(body), verify=False)
        response.raise_for_status()
        print("\n>>> User created successfully.")
    except HTTPError as http_error:
        print(http_error)
        print(response.text)
        exit(1)
    except Exception as error:
        print("\nError occurred: " + str(error))
        exit(1)

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
    unpack_and_run()
else:
    print("\n>>> IS already running ...")
print ("==============================================\n")

# Create a new user to reject consent
createNewUser("user1", "User1@password")

def createSPApp(app_json):
    print("\n")
    dcr(app_json)
    app_id = get_application_id_by_sp_name(app_json.get("client_name"))
    app_details = get_service_provider_details(app_id)
    set_application_scopes_for_consent(app_id)
    disable_skipping_consent(app_id)
    set_hybridFlow_config(app_id)
    configure_acr(app_id)
    return app_details

def generateConfigForPlan(app1, app2, consfigOutputFilePath, plan_name):
    app1_details = createSPApp(app1)
    app2_details = createSPApp(app2)
    json_config_builder(app1_details, app2_details, consfigOutputFilePath, plan_name)

print("\n>>> Configs for pvtkeyjwt test plan")
generateConfigForPlan(constants.PVTKEYJWT_APP1, constants.PVTKEYJWT_APP2, "config/IS_config_fapi_pvtkeyjwt.json", "pvtkeyjwt")

print("\n>>> Configs for mtls test plan")
generateConfigForPlan(constants.MTLS_APP1, constants.MTLS_APP2, "config/IS_config_fapi_mtls.json", "mtls")

print("\n>>> Configs for pvtkeyjwt par test plan")
generateConfigForPlan(constants.PVTKEYJWT_PAR_APP1, constants.PVTKEYJWT_PAR_APP2, "config/IS_config_fapi_pvtkeyjwt_par.json", "pvtkeyjwt_par")

print("\n>>> Configs for mtls par test plan")
generateConfigForPlan(constants.MTLS_PAR_APP1, constants.MTLS_PAR_APP2, "config/IS_config_fapi_mtls_par.json", "mtls_par")
