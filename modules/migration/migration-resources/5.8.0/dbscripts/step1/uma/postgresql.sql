CREATE OR REPLACE FUNCTION skip_index_if_exists(indexName varchar(64),tableName varchar(64), tableColumns varchar(64))
  RETURNS void AS $$
  declare s varchar(1000);
  begin
	  if to_regclass(indexName) IS NULL  then
	    s :=  CONCAT('CREATE INDEX ' , indexName , ' ON ' , tableName, tableColumns);
	    execute s;
	  end if;
  END;
$$ LANGUAGE plpgsql;

SELECT skip_index_if_exists('IDX_USER_RID', 'IDN_UMA_RESOURCE', '(RESOURCE_ID, RESOURCE_OWNER_NAME, USER_DOMAIN, CLIENT_ID)');

DROP FUNCTION skip_index_if_exists;
