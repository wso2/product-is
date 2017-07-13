ALTER TABLE IDN_OAUTH_CONSUMER_APPS ADD PKCE_MANDATORY CHAR(1) DEFAULT '0';
ALTER TABLE IDN_OAUTH_CONSUMER_APPS ADD PKCE_SUPPORT_PLAIN CHAR(1) DEFAULT '0';

ALTER TABLE IDN_OAUTH2_AUTHORIZATION_CODE ADD PKCE_CODE_CHALLENGE VARCHAR(255);
ALTER TABLE IDN_OAUTH2_AUTHORIZATION_CODE ADD PKCE_CODE_CHALLENGE_METHOD VARCHAR(128);

ALTER TABLE WF_BPS_PROFILE ALTER COLUMN HOST_URL_MANAGER VARCHAR(255);
ALTER TABLE WF_BPS_PROFILE ALTER COLUMN HOST_URL_WORKER VARCHAR(255);

TRUNCATE TABLE IDN_AUTH_SESSION_STORE;
