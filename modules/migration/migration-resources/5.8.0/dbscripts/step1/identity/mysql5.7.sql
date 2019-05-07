ALTER TABLE IDN_SAML2_ASSERTION_STORE ADD COLUMN ASSERTION BLOB;

ALTER TABLE IDN_OAUTH_CONSUMER_APPS MODIFY CALLBACK_URL VARCHAR(2048);

ALTER TABLE IDN_OAUTH1A_REQUEST_TOKEN MODIFY CALLBACK_URL VARCHAR(2048);

ALTER TABLE IDN_OAUTH2_AUTHORIZATION_CODE MODIFY CALLBACK_URL VARCHAR(2048);

DROP PROCEDURE IF EXISTS skip_index_if_exists;

CREATE PROCEDURE skip_index_if_exists(indexName varchar(64),tableName varchar(64), tableColumns varchar(64)) BEGIN  IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name = tableName AND index_name = indexName) = 0) THEN SET @s = CONCAT('CREATE INDEX ' , indexName , ' ON ' , tableName, tableColumns); PREPARE stmt FROM @s; EXECUTE stmt; END IF; END;

DROP PROCEDURE IF EXISTS create_index_if_not_column_is_partly_indexed;

CREATE PROCEDURE create_index_if_not_column_is_partly_indexed(partlyIndexedColumn varchar(64), indexName varchar(64), tableName varchar(64), columns varchar(64)) BEGIN

  DECLARE indexColumnCount BIGINT;
  DECLARE subPartValue BIGINT;

  SELECT SUB_PART INTO subPartValue
  FROM information_schema.statistics
  WHERE TABLE_SCHEMA = DATABASE()
    and table_name = tableName
    AND index_name = indexName
    AND COLUMN_NAME = partlyIndexedColumn;

  SELECT COUNT(*) AS index_exists INTO indexColumnCount
  FROM information_schema.statistics
  WHERE TABLE_SCHEMA = DATABASE()
    and table_name = tableName
    AND index_name = indexName
    AND COLUMN_NAME = partlyIndexedColumn;

  IF (subPartValue IS NULL)
  -- either index does not exists or the column is not partly indexed --
  THEN
    START TRANSACTION;

    --  column is not partly indexed. Drop existing prior to index creation --
    IF(indexColumnCount > 0) THEN
      SET @dropQuery = CONCAT('DROP INDEX ', indexName, ' ON ', tableName);
      PREPARE dropStatement FROM @dropQuery;
      EXECUTE dropStatement;
    END IF;

    SET @createQuery = CONCAT('CREATE INDEX ', indexName, ' ON ', tableName, columns);
    PREPARE createStatement FROM @createQuery;
    EXECUTE createStatement;

    COMMIT;
  END IF;
END;

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

call create_index_if_not_column_is_partly_indexed('ATTR_NAME', 'IDX_IDN_SCIM_GROUP_TI_RN_AN', 'IDN_SCIM_GROUP', '(TENANT_ID, ROLE_NAME, ATTR_NAME(500))');

CALL skip_index_if_exists('IDX_OCA_UM_TID_UD_APN','IDN_OAUTH_CONSUMER_APPS','(USERNAME,TENANT_ID,USER_DOMAIN, APP_NAME)');

CALL skip_index_if_exists('IDX_SPI_APP','SP_INBOUND_AUTH','(APP_ID)');

CALL skip_index_if_exists('IDX_IOP_TID_CK','IDN_OIDC_PROPERTY','(TENANT_ID,CONSUMER_KEY)');

-- IDN_OAUTH2_ACCESS_TOKEN --

CALL skip_index_if_exists('IDX_AT_AU_TID_UD_TS_CKID','IDN_OAUTH2_ACCESS_TOKEN','(AUTHZ_USER, TENANT_ID, USER_DOMAIN, TOKEN_STATE, CONSUMER_KEY_ID');

CALL skip_index_if_exists('IDX_AT_AT','IDN_OAUTH2_ACCESS_TOKEN','(ACCESS_TOKEN');

CALL skip_index_if_exists('IDX_AT_AU_CKID_TS_UT','IDN_OAUTH2_ACCESS_TOKEN','(AUTHZ_USER, CONSUMER_KEY_ID, TOKEN_STATE, USER_TYPE');

CALL skip_index_if_exists('IDX_AT_RTH','IDN_OAUTH2_ACCESS_TOKEN','(REFRESH_TOKEN_HASH');

CALL skip_index_if_exists('IDX_AT_RT','IDN_OAUTH2_ACCESS_TOKEN','(REFRESH_TOKEN');

-- IDN_OAUTH2_AUTHORIZATION_CODE --

CALL skip_index_if_exists('IDX_AC_CKID','IDN_OAUTH2_AUTHORIZATION_CODE','(CONSUMER_KEY_ID');

CALL skip_index_if_exists('IDX_AC_TID','IDN_OAUTH2_AUTHORIZATION_CODE','(TOKEN_ID');

CALL skip_index_if_exists('IDX_AC_AC_CKID','IDN_OAUTH2_AUTHORIZATION_CODE','(AUTHORIZATION_CODE, CONSUMER_KEY_ID');

-- IDN_OAUTH2_SCOPE --

CALL skip_index_if_exists('IDX_SC_TID','IDN_OAUTH2_SCOPE','(TENANT_ID');

CALL skip_index_if_exists('IDX_SC_N_TID','IDN_OAUTH2_SCOPE','(NAME, TENANT_ID');

-- IDN_OAUTH2_SCOPE_BINDING --

CALL skip_index_if_exists('IDX_SB_SCPID','IDN_OAUTH2_SCOPE_BINDING','(SCOPE_ID');

-- IDN_OIDC_REQ_OBJECT_REFERENCE --

CALL skip_index_if_exists('IDX_OROR_TID','IDN_OIDC_REQ_OBJECT_REFERENCE','(TOKEN_ID');

-- IDN_OAUTH2_ACCESS_TOKEN_SCOPE --

CALL skip_index_if_exists('IDX_ATS_TID','IDN_OAUTH2_ACCESS_TOKEN_SCOPE','(TOKEN_ID');

-- IDN_AUTH_USER --

CALL skip_index_if_exists('IDX_AUTH_USER_UN_TID_DN','IDN_AUTH_USER ','(USER_NAME, TENANT_ID, DOMAIN_NAME');

CALL skip_index_if_exists('IDX_AUTH_USER_DN_TOD','IDN_AUTH_USER ','(DOMAIN_NAME, TENANT_ID');

DROP PROCEDURE IF EXISTS skip_index_if_exists;

DROP PROCEDURE IF EXISTS create_index_if_not_column_is_partly_indexed;
