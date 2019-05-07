CREATE OR REPLACE PROCEDURE add_column_if_not_exists_with_default_val (IN table_name VARCHAR (255), IN column_name VARCHAR (255), IN
data_type VARCHAR(255), IN default_val VARCHAR (255))
     DYNAMIC RESULT SETS 0
     MODIFIES SQL DATA
     LANGUAGE SQL
BEGIN
    DECLARE SQL_STATEMENT VARCHAR(800);
    IF (NOT EXISTS(
    SELECT 1 FROM SYSCAT.COLUMNS WHERE TABNAME = table_name AND COLNAME = column_name))
    THEN
        SET SQL_STATEMENT = 'ALTER TABLE ';
        SET SQL_STATEMENT = SQL_STATEMENT || table_name;
        SET SQL_STATEMENT = SQL_STATEMENT || ' ADD COLUMN ';
        SET SQL_STATEMENT = SQL_STATEMENT || column_name || ' ';
        SET SQL_STATEMENT = SQL_STATEMENT || data_type;
        SET SQL_STATEMENT = SQL_STATEMENT || ' NOT NULL DEFAULT ';
        SET SQL_STATEMENT = SQL_STATEMENT || default_val;
        EXECUTE IMMEDIATE SQL_STATEMENT;
    END IF;
END
/

CALL add_column_if_not_exists_with_default_val('IDN_OAUTH2_AUTHORIZATION_CODE', 'IDP_ID', 'INTEGER', '-1')
/

CALL add_column_if_not_exists_with_default_val('IDN_OAUTH2_ACCESS_TOKEN', 'IDP_ID', 'INTEGER', '-1')
/

CALL add_column_if_not_exists_with_default_val('IDN_OAUTH2_ACCESS_TOKEN_AUDIT', 'IDP_ID', 'INTEGER', '-1')
/

ALTER TABLE IDN_SAML2_ASSERTION_STORE ADD COLUMN ASSERTION BLOB
/

ALTER TABLE IDN_OAUTH2_ACCESS_TOKEN DROP CONSTRAINT CON_APP_KEY
/

ALTER TABLE IDN_OAUTH2_ACCESS_TOKEN ADD CONSTRAINT CON_APP_KEY UNIQUE (CONSUMER_KEY_ID,AUTHZ_USER,TENANT_ID,USER_DOMAIN,USER_TYPE,TOKEN_SCOPE_HASH,TOKEN_STATE,TOKEN_STATE_ID,IDP_ID)
/

CREATE TABLE IDN_AUTH_USER (
	USER_ID VARCHAR(255) NOT NULL,
	USER_NAME VARCHAR(255) NOT NULL,
	TENANT_ID INTEGER NOT NULL,
	DOMAIN_NAME VARCHAR(255) NOT NULL,
	IDP_ID INTEGER NOT NULL,
	PRIMARY KEY (USER_ID),
	CONSTRAINT USER_STORE_CONSTRAINT UNIQUE (USER_NAME, TENANT_ID, DOMAIN_NAME, IDP_ID)
)
/

CREATE TABLE IDN_AUTH_USER_SESSION_MAPPING (
	USER_ID VARCHAR(255) NOT NULL,
	SESSION_ID VARCHAR(255) NOT NULL,
	CONSTRAINT USER_SESSION_STORE_CONSTRAINT UNIQUE (USER_ID, SESSION_ID)
)
/

CREATE INDEX IDX_USER_ID ON IDN_AUTH_USER_SESSION_MAPPING (USER_ID)
/

CREATE INDEX IDX_SESSION_ID ON IDN_AUTH_USER_SESSION_MAPPING (SESSION_ID)
/

CREATE INDEX IDX_OCA_UM_TID_UD_APN ON IDN_OAUTH_CONSUMER_APPS(USERNAME,TENANT_ID,USER_DOMAIN, APP_NAME)
/

CREATE INDEX IDX_SPI_APP ON SP_INBOUND_AUTH(APP_ID)
/

CREATE INDEX IDX_IOP_TID_CK ON IDN_OIDC_PROPERTY(TENANT_ID,CONSUMER_KEY)
/
