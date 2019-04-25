ALTER TABLE IDN_SAML2_ASSERTION_STORE ADD ASSERTION BLOB
/

-- IDN_OAUTH_CONSUMER_APPS --
CREATE INDEX IDX_OCA_UM_TID_UD_APN ON IDN_OAUTH_CONSUMER_APPS(USERNAME,TENANT_ID,USER_DOMAIN, APP_NAME)
/

-- IDX_SPI_APP --
CREATE INDEX IDX_SPI_APP ON SP_INBOUND_AUTH(APP_ID)
/

-- IDN_OIDC_PROPERTY --
CREATE INDEX IDX_IOP_TID_CK ON IDN_OIDC_PROPERTY(TENANT_ID,CONSUMER_KEY)
/