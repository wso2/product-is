DROP PROCEDURE IF EXISTS skip_index_if_exists;

CREATE PROCEDURE skip_index_if_exists(indexName varchar(64),tableName varchar(64), tableColumns varchar(64)) BEGIN  IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name = tableName AND index_name = indexName) = 0) THEN SET @s = CONCAT('CREATE INDEX ' , indexName , ' ON ' , tableName, tableColumns); PREPARE stmt FROM @s; EXECUTE stmt; END IF; END;

CALL skip_index_if_exists('IDX_USER_RID', 'IDN_UMA_RESOURCE', '(RESOURCE_ID, RESOURCE_OWNER_NAME, USER_DOMAIN, ' ||
 'CLIENT_ID)');
CREATE INDEX IDX_USER_RID ON IDN_UMA_RESOURCE (RESOURCE_ID, RESOURCE_OWNER_NAME, USER_DOMAIN, CLIENT_ID);

DROP PROCEDURE IF EXISTS skip_index_if_exists;
