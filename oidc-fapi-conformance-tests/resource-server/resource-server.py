import base64
import uuid
import requests
import jwt
from flask import request
from flask import Flask, jsonify
from cryptography.hazmat.primitives import hashes
from cryptography import x509
from cryptography.hazmat.backends import default_backend
from urllib.parse import unquote
from jwks_to_host import serverjwks, serverjwks2


app = Flask(__name__)
app.config['SECRET_KEY'] = 'secret'

class Unauthorized(Exception):
    status_code = 401

    def __init__(self, message, status_code=None, payload=None):
        Exception.__init__(self)
        self.message = message
        if status_code is not None:
            self.status_code = status_code
        self.payload = payload

    def to_dict(self):
        rv = dict(self.payload or ())
        rv['message'] = self.message
        return rv

def getAccessToken(request):
    authHeader =  request.headers.get('Authorization')
    if authHeader is None:
        raise Unauthorized('Missing Authorization header.')
    authHeaderParts = authHeader.split(" ")
    if len(authHeaderParts) != 2 or authHeaderParts[0].lower() != 'bearer':
        raise Unauthorized('Bearer token missing.')
    accessToken = authHeaderParts[1]
    if accessToken == None or accessToken == '':
        raise Unauthorized('Bearer token is null.')
    return accessToken

def isJwt(accessToken):
    try:
        decodedToken = jwt.decode(accessToken, options={"verify_signature": False}, algorithms=['RS256'])
        return True
    except jwt.DecodeError:
        return False
    
def getCnfFromIntrospect(accessToken):
    headers = {'Authorization': 'Basic ' +  "YWRtaW46YWRtaW4=", 'Content-Type': 'application/x-www-form-urlencoded'}
    data = {'token': accessToken}
    response = requests.post('https://localhost:9443/oauth2/introspect', headers=headers, data=data, verify=False)
    if response.status_code == 200:
        introspect = response.json()
        print ("[INFO] Introspect response: \n", introspect)
        cnf = introspect.get('cnf')
        print ("[INFO] Introspect cnf: ", cnf)
        if cnf == None:
            raise Unauthorized('No cnf in introspect response.')
        return cnf
    else:
        raise Exception('Failed to introspect: ' + response.text)

def getThumbprintFromAccessToken(accessToken):
    if not isJwt(accessToken):
        cnf = getCnfFromIntrospect(accessToken)
    else:
        try:
            decodedToken = jwt.decode(accessToken, options={"verify_signature": False}, algorithms=['RS256'])
        except jwt.DecodeError as e:
            print ("Decode Error: ", e)
            raise Unauthorized('Unable to decode Access Token.')
        try:
            cnf = decodedToken.get('cnf')
        except Exception as e:
            raise Unauthorized('Unable to get cnf from Access Token jwt.')
    if cnf is not None:
        thumbprint_b64 = cnf.get('x5t#S256')
        if thumbprint_b64 is not None:
            thumbprint_bytes = base64.urlsafe_b64decode(thumbprint_b64 + '==')
            thumbprint_hex = thumbprint_bytes.hex()
            print ("[INFO] Thumbprint from Access Token: ", thumbprint_hex)
            return thumbprint_hex
        else:
            raise Unauthorized('Unable to get x5t#S256 from cnf.')
    else:
        raise Unauthorized('cnf is null.')

def computeSHA256Thumbprint(clientCertificate):
    try:
        cert = unquote(clientCertificate)
        cert_obj = x509.load_pem_x509_certificate(cert.encode('utf-8'), default_backend())
        thumbprint = cert_obj.fingerprint(hashes.SHA256())
        thumbprint_hex= thumbprint.hex()
        print ("[INFO] Thumbprint from Client Certificate: ", thumbprint_hex)
        return thumbprint_hex
    except Exception as e:
        raise Unauthorized('Unable to compute SHA256 thumbprint from client certificate.')

def getThumbprintFromClientCertificate(request):
    clientCertificate = request.headers.get('X-Ssl-Cert')
    if clientCertificate is None:
        raise Unauthorized('Missing X-Ssl-Cert header.')
    clientCertificate = clientCertificate.strip().split(" ")[0]
    if clientCertificate == None or clientCertificate == '':
        raise Unauthorized('X-Ssl-Cert header is null.')
    return computeSHA256Thumbprint(clientCertificate)

def validateTokenWithIntrospect(accessToken):
    headers = {'Authorization': 'Basic ' +  "YWRtaW46YWRtaW4=", 'Content-Type': 'application/x-www-form-urlencoded'}
    data = {'token': accessToken}
    response = requests.post('https://localhost:9443/oauth2/introspect', headers=headers, data=data, verify=False)
    if response.status_code == 200:
        introspect = response.json()
        activeState = introspect.get('active')
        print ("[INFO] Introspect active state: ", activeState)
        if activeState == False:
            raise Unauthorized('Access token validation failed. Introspect active state is false.')
        return activeState
    else:
        raise Exception('Failed to introspect token: ' + response.text)

@app.route('/resource')
def resource():
    print ("[INFO] Resource endpoint...")
    print ("[INFO] Headers : \n", request.headers)
    print ("==================")
    accesToekn = getAccessToken(request)
    cnf = getThumbprintFromAccessToken(accesToekn)
    clientCertificateHash = getThumbprintFromClientCertificate(request)
    if cnf != clientCertificateHash:
        raise Unauthorized('Thumbprints from Cleint Certificate and Access Token do not match.')
    validateTokenWithIntrospect(accesToekn)
    print ("==================")

    response_data = {'message': 'This is a mock resource endpoint with FAPI validations.'}
    response = jsonify(response_data)
    xFapi = request.headers.get('X-Fapi-Interaction-Id')
    if xFapi is None:
        xFapi = uuid.uuid4()

    response.headers['x-fapi-interaction-id'] = xFapi
    return response

@app.route('/jwks1')
def jwks1():
    print ("[INFO] JWKS endpoint 1")
    return serverjwks.jwks

@app.route('/jwks2')
def jwks2():
    print ("[INFO] JWKS endpoint 2")
    return serverjwks2.jwks

@app.errorhandler(Unauthorized)
def handleUnauthorized(error):
    response = jsonify(error.to_dict())
    response.status_code = error.status_code
    return response

if __name__ == '__main__':
    app.run(debug=False, port=5002, host='0.0.0.0')
