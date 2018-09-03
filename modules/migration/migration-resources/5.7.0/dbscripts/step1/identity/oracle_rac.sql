CREATE TABLE IDN_OAUTH2_ACCESS_TOKEN_AUDIT (
            TOKEN_ID VARCHAR (255),
            ACCESS_TOKEN VARCHAR2(2048),
            REFRESH_TOKEN VARCHAR2(2048),
            CONSUMER_KEY_ID INTEGER,
            AUTHZ_USER VARCHAR2 (100),
            TENANT_ID INTEGER,
            USER_DOMAIN VARCHAR2(50),
            USER_TYPE VARCHAR (25),
            GRANT_TYPE VARCHAR (50),
            TIME_CREATED TIMESTAMP NULL,
            REFRESH_TOKEN_TIME_CREATED TIMESTAMP NULL,
            VALIDITY_PERIOD NUMBER(19),
            REFRESH_TOKEN_VALIDITY_PERIOD NUMBER(19),
            TOKEN_SCOPE_HASH VARCHAR2(32),
            TOKEN_STATE VARCHAR2(25),
            TOKEN_STATE_ID VARCHAR (128) ,
            SUBJECT_IDENTIFIER VARCHAR(255),
            ACCESS_TOKEN_HASH VARCHAR2(512),
            REFRESH_TOKEN_HASH VARCHAR2(512),
            INVALIDATED_TIME TIMESTAMP NULL)
/
CREATE TABLE SP_TEMPLATE (
  ID         INTEGER      NOT NULL,
  TENANT_ID  INTEGER      NOT NULL,
  NAME       VARCHAR(255) NOT NULL,
  DESCRIPTION  VARCHAR(1023),
  CONTENT BLOB DEFAULT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT SP_TEMPLATE_CONSTRAINT UNIQUE (TENANT_ID, NAME))
)
/
CREATE SEQUENCE SP_TEMPLATE_SEQ START WITH 1 INCREMENT BY 1 NOCACHE
/
CREATE OR REPLACE TRIGGER SP_TEMPLATE_TRIG
  BEFORE INSERT
  ON SP_TEMPLATE
  REFERENCING NEW AS NEW
  FOR EACH ROW
  BEGIN
    SELECT SP_TEMPLATE_SEQ.nextval
    INTO :NEW.ID
    FROM dual;
  END;
/
CREATE TABLE IDN_AUTH_WAIT_STATUS (
  ID              INTEGER       NOT NULL,
  TENANT_ID       INTEGER       NOT NULL,
  LONG_WAIT_KEY   VARCHAR(255)  NOT NULL,
  WAIT_STATUS     CHAR(1)       DEFAULT '1',
  TIME_CREATED    TIMESTAMP,
  EXPIRE_TIME     TIMESTAMP,
  PRIMARY KEY (ID),
  CONSTRAINT IDN_AUTH_WAIT_STATUS_KEY UNIQUE (LONG_WAIT_KEY)
)
/

CREATE SEQUENCE IDN_AUTH_WAIT_STATUS_SEQ START WITH 1 INCREMENT BY 1 NOCACHE
/

CREATE OR REPLACE TRIGGER IDN_AUTH_WAIT_STATUS_TRIG
  BEFORE INSERT
  ON IDN_AUTH_WAIT_STATUS
  REFERENCING NEW AS NEW
  FOR EACH ROW
  BEGIN
    SELECT IDN_AUTH_WAIT_STATUS_SEQ.nextval INTO :NEW.ID FROM dual;
  END;
/

CREATE TABLE IDN_SAML2_ARTIFACT_STORE (
  ID INTEGER,
  SOURCE_ID VARCHAR(255) NOT NULL,
  MESSAGE_HANDLER VARCHAR(255) NOT NULL,
  AUTHN_REQ_DTO BLOB NOT NULL,
  SESSION_ID VARCHAR(255) NOT NULL,
  INIT_TIMESTAMP TIMESTAMP NOT NULL,
  EXP_TIMESTAMP TIMESTAMP NOT NULL,
  ASSERTION_ID VARCHAR(255),
  PRIMARY KEY (ID))
/

CREATE SEQUENCE IDN_SAML2_ARTIFACT_STORE_SEQ START WITH 1 INCREMENT BY 1 NOCACHE
/

CREATE TABLE IDN_OIDC_SCOPE (
            ID INTEGER,
            NAME VARCHAR(255),
            TENANT_ID INTEGER DEFAULT -1,
            PRIMARY KEY(ID))
/

CREATE SEQUENCE IDN_OIDC_SCOPE_SEQUENCE START WITH 1 INCREMENT BY 1 NOCACHE
/

CREATE OR REPLACE TRIGGER IDN_OIDC_SCOPE_TRIGGER
		    BEFORE INSERT
            ON IDN_OIDC_SCOPE
            REFERENCING NEW AS NEW
            FOR EACH ROW
            BEGIN
                SELECT IDN_OIDC_SCOPE_SEQUENCE.nextval INTO :NEW.ID FROM dual;
            END;
/

CREATE TABLE IDN_OIDC_SCOPE_CLAIM_MAPPING (
  ID INTEGER,
  SCOPE_ID INTEGER,
  EXTERNAL_CLAIM_ID INTEGER,
  PRIMARY KEY (ID),
  FOREIGN KEY (SCOPE_ID) REFERENCES IDN_CLAIM(ID) ON DELETE CASCADE,
  FOREIGN KEY (EXTERNAL_CLAIM_ID) REFERENCES IDN_OIDC_SCOPE(ID) ON DELETE CASCADE)
/

CREATE SEQUENCE IDN_OIDC_SCOPE_CLAIM_MAPPING_SEQ START WITH 1 INCREMENT BY 1 NOCACHE
/

CREATE OR REPLACE TRIGGER IDN_OIDC_SCOPE_CLAIM_MAPPING_TRIG
            BEFORE INSERT
            ON IDN_OIDC_SCOPE_CLAIM_MAPPING
            REFERENCING NEW AS NEW
            FOR EACH ROW
               BEGIN
                   SELECT IDN_OIDC_SCOPE_CLAIM_MAPPING_SEQ.nextval INTO :NEW.ID FROM dual;
               END;
/
CREATE INDEX IDX_AT_SI_ECI ON IDN_OIDC_SCOPE_CLAIM_MAPPING(SCOPE_ID, EXTERNAL_CLAIM_ID)
/

ALTER TABLE IDN_OAUTH_CONSUMER_APPS modify (USER_ACCESS_TOKEN_EXPIRE_TIME DEFAULT 3600)
/
ALTER TABLE IDN_OAUTH_CONSUMER_APPS modify (APP_ACCESS_TOKEN_EXPIRE_TIME DEFAULT 3600)
/
ALTER TABLE IDN_OAUTH_CONSUMER_APPS modify (REFRESH_TOKEN_EXPIRE_TIME DEFAULT 84600)
/
ALTER TABLE IDN_OAUTH_CONSUMER_APPS modify (ID_TOKEN_EXPIRE_TIME DEFAULT 3600)
/
