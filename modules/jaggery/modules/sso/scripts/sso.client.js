/**
 * Following module act as a client to create a saml request and also to
 * unwrap and return attributes of a returning saml response
 * @type {{}}
 */

var client = {};

(function (client) {

    var Util = Packages.org.wso2.store.sso.common.util.Util,
        carbon = require('carbon'),
        log = new Log();

    /**
     * obtains an encoded saml response and return a decoded/unmarshalled saml obj
     * @param samlResp
     * @return {*}
     */
    client.getSamlObject = function (samlResp) {
        var decodedResp = Util.decode(samlResp);
        return Util.unmarshall(decodedResp);
    };

    /**
     * validating the signature of the response saml object
     */
    client.validateSignature = function (samlObj, config) {
        var tDomain = Util.getDomainName(samlObj);
        var tId = carbon.server.tenantId({domain: tDomain});

        return Util.validateSignature(samlObj,
            config.KEY_STORE_NAME, config.KEY_STORE_PASSWORD, config.IDP_ALIAS, tId, tDomain);
    };

    /**
     * Checking if the request is a logout call
     */
    client.isLogoutRequest = function (samlObj) {
        return samlObj instanceof Packages.org.opensaml.saml2.core.LogoutRequest;
    };

    /**
     * Checking if the request is a logout call
     */
    client.isLogoutResponse = function (samlObj) {
        return samlObj instanceof Packages.org.opensaml.saml2.core.LogoutResponse;
    };

    /**
     * getting url encoded saml authentication request
     * @param issuerId
     */
    client.getEncodedSAMLAuthRequest = function (issuerId) {
        return Util.encode(
            Util.marshall(
                new Packages.org.wso2.store.sso.common.builders.AuthReqBuilder().buildAuthenticationRequest(issuerId)
            ));
    };

    /**
     * get url encoded saml logout request
     */
    client.getEncodedSAMLLogoutRequest = function (user, sessionIndex, issuerId) {
        return Util.encode(
            Util.marshall(
                new Packages.org.wso2.store.sso.common.builders.LogoutRequestBuilder().buildLogoutRequest(user, sessionIndex,
                    Packages.org.wso2.store.sso.common.constants.SSOConstants.LOGOUT_USER,
                    issuerId)));
    };

    /**
     * Reads the returning SAML login response and populates a session info object
     */
    client.decodeSAMLLoginResponse = function (samlObj, samlResp, sessionId) {
        var samlSessionObj = {
            // sessionId, loggedInUser, sessionIndex, samlToken
        };

        if (samlObj instanceof Packages.org.opensaml.saml2.core.Response) {

            var assertions = samlObj.getAssertions();

            // extract the session index
            if (assertions != null && assertions.size() > 0) {
                var authenticationStatements = assertions.get(0).getAuthnStatements();
                var authnStatement = authenticationStatements.get(0);
                if (authnStatement != null) {
                    if (authnStatement.getSessionIndex() != null) {
                        samlSessionObj.sessionIndex = authnStatement.getSessionIndex();
                    }
                }
            }

            // extract the username
            if (assertions != null && assertions.size() > 0) {
                var subject = assertions.get(0).getSubject();
                if (subject != null) {
                    if (subject.getNameID() != null) {
                        samlSessionObj.loggedInUser = subject.getNameID().getValue();
                    }
                }
            }
            samlSessionObj.sessionId = sessionId;
            samlSessionObj.samlToken = samlResp;
        }

        return samlSessionObj;
    };

    /**
     * This method is to get the session index when a single logout happens
     * The IDP sends a logout request to the ACS with the session index, so that
     * the app can invalidate the associated HTTP Session
     */
    client.decodeSAMLLogoutRequest = function (samlObj) {
        var sessionIndex = null;

        if (samlObj instanceof org.opensaml.saml2.core.LogoutRequest) {
            var sessionIndexes = samlObj.getSessionIndexes();
            if (sessionIndexes != null && sessionIndexes.size() > 0) {
                sessionIndex = sessionIndexes.get(0).getSessionIndex();
            }
        }

        return sessionIndex;

    };

}(client));