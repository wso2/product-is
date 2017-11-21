package org.wso2.carbon.identity.samples.sts;

import java.io.File;

public abstract class ClientConstants {
    
    public static final String RESOURCE_PATH = System.getProperty("user.dir") + File.separator + "/src/main/resources/";
    public static final String PROPERTIES_FILE_PATH =  RESOURCE_PATH + "client.properties";
    
    //property file keys
    public static final String SAML_TOKEN_TYPE = "saml.token.type";
    public static final String ENABLE_RELYING_PARTY = "enable.relyingParty";
    public static final String ENABLE_ISSUE_BINDING = "enable.binding.issue";
    public static final String ENABLE_VALIDATE_BINDING = "enable.binding.validate";
    public static final String SUBJECT_CONFIRMATION_METHOD = "subject.confirmation.method";
    public static final String RELYING_PARTY_ADDRESS = "address.relyingParty";
    public static final String STS_ADDRESS = "address.sts";
    public static final String KEYSTORE_PATH = "path.keystore";
    public static final String REPO_PATH = "path.repo";
    public static final String STS_POLICY_PATH = "path.policy.sts";
    public static final String RELYING_PARTY_POLICY_PATH = "path.policy.relyingParty";
    public static final String RELYING_PARTY_MESSAGE = "relyingParty.message";
    public static final String UT_USERNAME = "ut.username";
    public static final String UT_PASSWORD = "ut.password";
    public static final String CLAIM_DIALECT = "claim.dialect";
    public static final String CLAIM_URIS = "claim.uris";
    public static final String ENCRYPTION_USER = "encryption.user";
    public static final String USER_CERTIFICATE_ALIAS = "user.cert.alias";
    public static final String USER_CERTIFICATE_PASSWORD = "user.cert.password";
    public static final String PASSWORD_CALLBACK_CLASS = "password.callback.class";
    public static final String KEYSTORE_PASSWORD = "keystore.password";
    public static final String ENABLE_RENEW = "enable.renew";
    //property file values
    public static final String SUBJECT_CONFIRMATION_BEARER = "b";
    public static final String SUBJECT_CONFIRMATION_HOLDER_OF_KEY = "h";
    public static final String SAML_TOKEN_TYPE_10 = "1.0";
    public static final String SAML_TOKEN_TYPE_11 = "1.1";
    public static final String SAML_TOKEN_TYPE_20 = "2.0";
}
