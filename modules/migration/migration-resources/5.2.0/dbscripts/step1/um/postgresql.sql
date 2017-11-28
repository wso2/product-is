INSERT INTO UM_CLAIM (
            UM_DIALECT_ID, 
            UM_CLAIM_URI,
            UM_DISPLAY_TAG, 
            UM_DESCRIPTION,
            UM_MAPPED_ATTRIBUTE,
            UM_TENANT_ID,
            UM_READ_ONLY,
            UM_SUPPORTED,
            UM_REQUIRED,
            UM_DISPLAY_ORDER,
            UM_CHECKED_ATTRIBUTE)
VALUES ((SELECT UM_ID FROM UM_DIALECT WHERE UM_DIALECT_URI='http://wso2.org/claims' AND UM_TENANT_ID=-1234),
'http://wso2.org/claims/identity/lastLoginTime','Last Login Time','Last Login Time','carLicense',-1234,1,0,0,7,0);

INSERT INTO UM_CLAIM (
           UM_DIALECT_ID,
           UM_CLAIM_URI,
           UM_DISPLAY_TAG,
           UM_DESCRIPTION,
           UM_MAPPED_ATTRIBUTE,
           UM_TENANT_ID,
           UM_READ_ONLY)
SELECT DIALECT.UM_ID,
	   'http://wso2.org/claims/identity/lastLoginTime',
           'Last Login Time',
           'Last Login Time',
           'carLicense',
	   DIALECT.UM_TENANT_ID,
           1
           FROM UM_DIALECT as DIALECT JOIN UM_TENANT as TENANT ON DIALECT.UM_TENANT_ID=TENANT.UM_ID WHERE DIALECT.UM_DIALECT_URI='http://wso2.org/claims';

INSERT INTO UM_CLAIM (
            UM_DIALECT_ID,
            UM_CLAIM_URI,
            UM_DISPLAY_TAG,
            UM_DESCRIPTION,
            UM_MAPPED_ATTRIBUTE,
            UM_TENANT_ID,
            UM_READ_ONLY,
            UM_SUPPORTED,
            UM_REQUIRED,
            UM_DISPLAY_ORDER,
            UM_CHECKED_ATTRIBUTE)
VALUES ((SELECT UM_ID FROM UM_DIALECT WHERE UM_DIALECT_URI='http://wso2.org/claims' AND UM_TENANT_ID=-1234),
'http://wso2.org/claims/identity/lastPasswordUpdateTime','Last Password Update','Last Password Update','businessCategory',-1234,1,0,0,7,0);

INSERT INTO UM_CLAIM (
           UM_DIALECT_ID,
           UM_CLAIM_URI,
           UM_DISPLAY_TAG,
           UM_DESCRIPTION,
           UM_MAPPED_ATTRIBUTE,
           UM_TENANT_ID,
           UM_READ_ONLY)
SELECT DIALECT.UM_ID,
	'http://wso2.org/claims/identity/lastPasswordUpdateTime',
        'Last Password Update',
        'Last Password Update',
        'businessCategory',
        DIALECT.UM_TENANT_ID,
        1
        FROM UM_DIALECT as DIALECT JOIN UM_TENANT as TENANT ON DIALECT.UM_TENANT_ID=TENANT.UM_ID WHERE DIALECT.UM_DIALECT_URI='http://wso2.org/claims';

INSERT INTO UM_CLAIM (
      UM_DIALECT_ID,
      UM_CLAIM_URI,
      UM_DISPLAY_TAG,
      UM_DESCRIPTION,
      UM_MAPPED_ATTRIBUTE,
      UM_TENANT_ID,
      UM_READ_ONLY)
VALUES ((SELECT UM_ID FROM UM_DIALECT WHERE UM_DIALECT_URI='http://wso2.org/claims' AND UM_TENANT_ID=-1234),
'http://wso2.org/claims/identity/accountDisabled','Account Disabled','Account Disabled','ref',-1234,1);

INSERT INTO UM_CLAIM (
           UM_DIALECT_ID,
           UM_CLAIM_URI,
           UM_DISPLAY_TAG,
           UM_DESCRIPTION,
           UM_MAPPED_ATTRIBUTE,
           UM_TENANT_ID,
           UM_READ_ONLY)
SELECT DIALECT.UM_ID,
	   'http://wso2.org/claims/identity/accountDisabled',
           'Account Disabled',
           'Account Disabled',
           'ref',
	         DIALECT.UM_TENANT_ID,
           1
           FROM UM_DIALECT DIALECT JOIN UM_TENANT TENANT ON DIALECT.UM_TENANT_ID=TENANT.UM_ID WHERE DIALECT.UM_DIALECT_URI='http://wso2.org/claims';

CREATE INDEX REG_LOG_IND_BY_REG_LOGTIME ON REG_LOG(REG_LOGGED_TIME, REG_TENANT_ID);
CREATE INDEX REG_RESOURCE_IND_BY_UUID  ON REG_RESOURCE(REG_UUID);
CREATE INDEX REG_RESOURCE_IND_BY_TENANT ON REG_RESOURCE(REG_TENANT_ID, REG_UUID);
CREATE INDEX REG_RESOURCE_IND_BY_TYPE ON REG_RESOURCE(REG_TENANT_ID, REG_MEDIA_TYPE);
