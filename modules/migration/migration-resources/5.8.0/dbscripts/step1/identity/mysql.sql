ALTER TABLE IDN_SAML2_ASSERTION_STORE ADD COLUMN ASSERTION BLOB;

DROP PROCEDURE IF EXISTS skip_index_if_exists;

CREATE PROCEDURE skip_index_if_exists(indexName varchar(64),tableName varchar(64), tableColumns varchar(255)) BEGIN  IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name = tableName AND index_name = indexName) = 0) THEN SET @s = CONCAT('CREATE INDEX ' , indexName , ' ON ' , tableName, tableColumns); PREPARE stmt FROM @s; EXECUTE stmt; END IF; END;

DROP PROCEDURE IF EXISTS create_index_if_not_column_is_partly_indexed;

CREATE PROCEDURE create_index_if_not_column_is_partly_indexed(partlyIndexedColumn varchar(64), indexName varchar(64), tableName varchar(64), columns varchar(64)) BEGIN  DECLARE indexColumnCount BIGINT;  DECLARE subPartValue BIGINT;  SELECT SUB_PART INTO subPartValue  FROM information_schema.statistics  WHERE TABLE_SCHEMA = DATABASE()    and table_name = tableName    AND index_name = indexName    AND COLUMN_NAME = partlyIndexedColumn;  SELECT COUNT(*) AS index_exists INTO indexColumnCount  FROM information_schema.statistics  WHERE TABLE_SCHEMA = DATABASE()    and table_name = tableName    AND index_name = indexName    AND COLUMN_NAME = partlyIndexedColumn;  IF (subPartValue IS NULL)  THEN    START TRANSACTION; IF(indexColumnCount > 0) THEN SET @dropQuery = CONCAT('DROP INDEX ', indexName, ' ON ', tableName);      PREPARE dropStatement FROM @dropQuery;      EXECUTE dropStatement;    END IF;    SET @createQuery = CONCAT('CREATE INDEX ', indexName, ' ON ', tableName, columns);    PREPARE createStatement FROM @createQuery;    EXECUTE createStatement;    COMMIT;  END IF;END;

DROP PROCEDURE IF EXISTS add_column_if_not_exists_with_default_val;

CREATE PROCEDURE add_column_if_not_exists_with_default_val(table_name VARCHAR (64), column_name VARCHAR (64),data_type VARCHAR (64), default_val VARCHAR (64)) BEGIN IF EXISTS( SELECT NULL       FROM INFORMATION_SCHEMA.COLUMNS       WHERE table_name = table_name        AND column_name = column_name) THEN  SET @query = CONCAT('ALTER TABLE ', table_name, ' ADD COLUMN ', column_name, ' ', data_type, ' NOT NULL default ', default_val);  PREPARE statement FROM @query;  EXECUTE statement; END IF;END;

CALL add_column_if_not_exists_with_default_val('IDN_OAUTH2_AUTHORIZATION_CODE', 'IDP_ID', 'int', '-1');

CALL add_column_if_not_exists_with_default_val('IDN_OAUTH2_ACCESS_TOKEN', 'IDP_ID', 'INT', '-1');

CALL add_column_if_not_exists_with_default_val('IDN_OAUTH2_ACCESS_TOKEN_AUDIT', 'IDP_ID', 'INT', '-1');

ALTER TABLE IDN_OAUTH2_ACCESS_TOKEN DROP INDEX CON_APP_KEY;

ALTER TABLE IDN_OAUTH2_ACCESS_TOKEN ADD CONSTRAINT CON_APP_KEY UNIQUE (CONSUMER_KEY_ID,AUTHZ_USER,TENANT_ID,USER_DOMAIN,USER_TYPE,TOKEN_SCOPE_HASH,TOKEN_STATE,TOKEN_STATE_ID,IDP_ID);

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

CREATE INDEX IDX_USER_ID ON IDN_AUTH_USER_SESSION_MAPPING (USER_ID);

CREATE INDEX IDX_SESSION_ID ON IDN_AUTH_USER_SESSION_MAPPING (SESSION_ID);

call create_index_if_not_column_is_partly_indexed('ATTR_NAME', 'IDX_IDN_SCIM_GROUP_TI_RN_AN', 'IDN_SCIM_GROUP', '(TENANT_ID, ROLE_NAME, ATTR_NAME(500))');

CALL skip_index_if_exists('IDX_OCA_UM_TID_UD_APN','IDN_OAUTH_CONSUMER_APPS','(USERNAME,TENANT_ID,USER_DOMAIN, APP_NAME)');

CALL skip_index_if_exists('IDX_SPI_APP','SP_INBOUND_AUTH','(APP_ID)');

CALL skip_index_if_exists('IDX_IOP_TID_CK','IDN_OIDC_PROPERTY','(TENANT_ID,CONSUMER_KEY)');

