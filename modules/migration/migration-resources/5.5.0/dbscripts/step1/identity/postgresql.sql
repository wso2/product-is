ALTER TABLE IDN_OAUTH2_ACCESS_TOKEN ALTER COLUMN REFRESH_TOKEN TYPE VARCHAR(2048);
ALTER TABLE IDN_OAUTH2_ACCESS_TOKEN ALTER COLUMN ACCESS_TOKEN TYPE VARCHAR(2048);
ALTER TABLE IDN_OAUTH2_AUTHORIZATION_CODE ALTER COLUMN AUTHORIZATION_CODE TYPE VARCHAR(2048);
ALTER TABLE IDN_OAUTH_CONSUMER_APPS ALTER COLUMN CONSUMER_SECRET TYPE VARCHAR(2048);
