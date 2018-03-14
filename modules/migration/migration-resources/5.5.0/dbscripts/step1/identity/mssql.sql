ALTER TABLE IDN_OAUTH2_ACCESS_TOKEN ALTER COLUMN ACCESS_TOKEN VARCHAR(2048);
ALTER TABLE IDN_OAUTH2_ACCESS_TOKEN ALTER COLUMN REFRESH_TOKEN VARCHAR(2048);
ALTER TABLE IDN_OAUTH2_AUTHORIZATION_CODE ALTER COLUMN AUTHORIZATION_CODE VARCHAR(2048);
ALTER TABLE IDN_OAUTH_CONSUMER_APPS ALTER COLUMN CONSUMER_SECRET VARCHAR(2048);
ALTER TABLE IDN_OAUTH2_ACCESS_TOKEN ADD COLUMN ACCESS_TOKEN_HASH VARCHAR (512);
ALTER TABLE IDN_OAUTH2_ACCESS_TOKEN ADD COLUMN REFRESH_TOKEN_HASH VARCHAR (512);
ALTER TABLE IDN_OAUTH2_AUTHORIZATION_CODE ADD COLUMN AUTHORIZATION_CODE_HASH VARCHAR (512);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[IDN_OAUTH2_SCOPE_VALIDATORS]') AND TYPE IN (N'U'))
CREATE TABLE IDN_OAUTH2_SCOPE_VALIDATORS (
	APP_ID INTEGER NOT NULL,
	SCOPE_VALIDATOR VARCHAR (128) NOT NULL,
	PRIMARY KEY (APP_ID,SCOPE_VALIDATOR),
	FOREIGN KEY (APP_ID) REFERENCES IDN_OAUTH_CONSUMER_APPS(ID) ON DELETE CASCADE
);
IF NOT EXISTS(SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[SP_AUTH_SCRIPT]') AND TYPE IN (N'U'))
CREATE TABLE SP_AUTH_SCRIPT (
  ID         INTEGER IDENTITY NOT NULL,
  TENANT_ID  INTEGER          NOT NULL,
  APP_ID     INTEGER          NOT NULL,
  TYPE       VARCHAR(255)     NOT NULL,
  CONTENT    VARBINARY(MAX)    DEFAULT NULL,
  IS_ENABLED BIT DEFAULT 'FALSE',
  PRIMARY KEY (ID)
);
IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[IDN_OIDC_JTI]') AND TYPE IN (N'U'))
CREATE TABLE IDN_OIDC_JTI  (
  JWT_ID VARCHAR(255) NOT NULL,
  EXP_TIME DATETIME NOT NULL,
  TIME_CREATED DATETIME NOT NULL,
  PRIMARY KEY (JWT_ID)
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[IDN_OIDC_PROPERTY]') AND TYPE IN (N'U'))
CREATE TABLE IDN_OIDC_PROPERTY (
  ID INTEGER NOT NULL IDENTITY,
  TENANT_ID  INTEGER ,
  CONSUMER_KEY  VARCHAR(255) ,
  PROPERTY_KEY  VARCHAR(255) NOT NULL ,
  PROPERTY_VALUE  VARCHAR(2047) ,
  PRIMARY KEY (ID),
  FOREIGN KEY (CONSUMER_KEY) REFERENCES IDN_OAUTH_CONSUMER_APPS(CONSUMER_KEY) ON DELETE CASCADE
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[IDN_OIDC_REQ_OBJECT_REFERENCE]') AND TYPE IN (N'U'))
CREATE TABLE IDN_OIDC_REQ_OBJECT_REFERENCE (
  ID INTEGER NOT NULL IDENTITY,
  CONSUMER_KEY_ID INTEGER ,
  CODE_ID VARCHAR(255) ,
  TOKEN_ID VARCHAR(255) ,
  SESSION_DATA_KEY VARCHAR(255),
  PRIMARY KEY (ID),
  FOREIGN KEY (CONSUMER_KEY_ID) REFERENCES IDN_OAUTH_CONSUMER_APPS(ID) ON DELETE CASCADE ,
  FOREIGN KEY (TOKEN_ID) REFERENCES IDN_OAUTH2_ACCESS_TOKEN(TOKEN_ID),
  FOREIGN KEY (CODE_ID) REFERENCES IDN_OAUTH2_AUTHORIZATION_CODE(CODE_ID)
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[IDN_OIDC_REQ_OBJECT_CLAIMS]') AND TYPE IN (N'U'))
CREATE TABLE IDN_OIDC_REQ_OBJECT_CLAIMS (
  ID INTEGER NOT NULL IDENTITY,
  REQ_OBJECT_ID INTEGER,
  CLAIM_ATTRIBUTE VARCHAR(255) ,
  ESSENTIAL BIT ,
  VALUE VARCHAR(255) ,
  IS_USERINFO BIT,
  PRIMARY KEY (ID),
  FOREIGN KEY (REQ_OBJECT_ID) REFERENCES IDN_OIDC_REQ_OBJECT_REFERENCE (ID) ON DELETE CASCADE
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[IDN_OIDC_REQ_OBJ_CLAIM_VALUES]') AND TYPE IN (N'U'))
CREATE TABLE IDN_OIDC_REQ_OBJ_CLAIM_VALUES (
  ID INTEGER NOT NULL IDENTITY,
  REQ_OBJECT_CLAIMS_ID INTEGER ,
  CLAIM_VALUES VARCHAR(255) ,
  PRIMARY KEY (ID),
  FOREIGN KEY (REQ_OBJECT_CLAIMS_ID) REFERENCES  IDN_OIDC_REQ_OBJECT_CLAIMS(ID) ON DELETE CASCADE
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[IDN_CERTIFICATE]') AND TYPE IN (N'U'))
CREATE TABLE IDN_CERTIFICATE (
             ID INTEGER IDENTITY,
             NAME VARCHAR(100),
             CERTIFICATE_IN_PEM VARBINARY(MAX),
             TENANT_ID INTEGER DEFAULT 0,
             PRIMARY KEY(ID),
             CONSTRAINT CERTIFICATE_UNIQUE_KEY UNIQUE (NAME, TENANT_ID)
);
