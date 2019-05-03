CREATE OR REPLACE PROCEDURE skip_index_if_exists (query IN VARCHAR2)
IS
BEGIN
declare
  already_exists  exception;
  columns_indexed exception;
  pragma exception_init( already_exists, -955 );
  pragma exception_init(columns_indexed, -1408);
begin
  execute immediate query;
  dbms_output.put_line( 'created' );
exception
  when already_exists or columns_indexed then
  dbms_output.put_line( 'skipped' );
end;
END;

BEGIN
skip_index_if_exists('CREATE INDEX IDX_USER_RID ON IDN_UMA_RESOURCE (RESOURCE_ID, RESOURCE_OWNER_NAME, USER_DOMAIN, CLIENT_ID)')
END;
