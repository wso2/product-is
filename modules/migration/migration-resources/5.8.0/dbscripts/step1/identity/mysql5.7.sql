ALTER TABLE IDN_SAML2_ASSERTION_STORE ADD COLUMN ASSERTION BLOB;

ALTER TABLE IDN_OAUTH_CONSUMER_APPS MODIFY CALLBACK_URL VARCHAR(2048);

ALTER TABLE IDN_OAUTH1A_REQUEST_TOKEN MODIFY CALLBACK_URL VARCHAR(2048);

ALTER TABLE IDN_OAUTH2_AUTHORIZATION_CODE MODIFY CALLBACK_URL VARCHAR(2048);

DROP PROCEDURE IF EXISTS skip_index_if_exists;

CREATE PROCEDURE skip_index_if_exists(indexName varchar(64),tableName varchar(64), tableColumns varchar(64)) BEGIN  IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name = tableName AND index_name = indexName) = 0) THEN SET @s = CONCAT('CREATE INDEX ' , indexName , ' ON ' , tableName, tableColumns); PREPARE stmt FROM @s; EXECUTE stmt; END IF; END;

CREATE TABLE IF NOT EXISTS IDN_AUTH_USER (
	USER_ID VARCHAR(255) NOT NULL,
	USER_NAME VARCHAR(255) NOT NULL,
	TENANT_ID INTEGER NOT NULL,
	DOMAIN_NAME VARCHAR(255) NOT NULL,
	IDP_ID INTEGER NOT NULL,
	PRIMARY KEY (USER_ID),
	CONSTRAINT USER_STORE_CONSTRAINT UNIQUE (USER_NAME, TENANT_ID, DOMAIN_NAME, IDP_ID));

CREATE TABLE IF NOT EXISTS IDN_AUTH_USER_SESSION_MAPPING (
	USER_ID VARCHAR(255) NOT NULL,
	SESSION_ID VARCHAR(255) NOT NULL,
	CONSTRAINT USER_SESSION_STORE_CONSTRAINT UNIQUE (USER_ID, SESSION_ID));

CALL skip_index_if_exists('IDX_USER_ID', 'IDN_AUTH_USER_SESSION_MAPPING', '(USER_ID)');

CALL skip_index_if_exists('IDX_SESSION_ID', 'IDN_AUTH_USER_SESSION_MAPPING', '(SESSION_ID)');

CALL skip_index_if_exists('IDX_OCA_UM_TID_UD_APN','IDN_OAUTH_CONSUMER_APPS','(USERNAME,TENANT_ID,USER_DOMAIN, APP_NAME)');

CALL skip_index_if_exists('IDX_SPI_APP','SP_INBOUND_AUTH','(APP_ID)');

CALL skip_index_if_exists('IDX_IOP_TID_CK','IDN_OIDC_PROPERTY','(TENANT_ID,CONSUMER_KEY)');
