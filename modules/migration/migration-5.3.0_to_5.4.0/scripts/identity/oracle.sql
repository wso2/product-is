/
CREATE UNIQUE INDEX SCOPE_INDEX ON IDN_OAUTH2_SCOPE (NAME, TENANT_ID);
/
CREATE TABLE IDN_OAUTH2_SCOPE_BINDING (
            SCOPE_ID INTEGER NOT NULL,
            SCOPE_BINDING VARCHAR2(255),
            FOREIGN KEY (SCOPE_ID) REFERENCES IDN_OAUTH2_SCOPE(SCOPE_ID) ON DELETE CASCADE)
/


declare
  con_name varchar2(100);
  command varchar2(200);
  databasename VARCHAR2(100);
BEGIN
  databasename := 'SAMPLE';

  begin
    select a.constraint_name into con_name FROM all_cons_columns a JOIN all_constraints c ON a.owner = c.owner AND a.constraint_name = c.constraint_name JOIN all_constraints c_pk ON c.r_owner = c_pk.owner AND c.r_constraint_name = c_pk.constraint_name WHERE c.constraint_type = 'R' AND a.table_name = 'IDN_OAUTH2_RESOURCE_SCOPE' AND UPPER(a.OWNER)=UPPER(databasename) AND c_pk.table_name='IDN_OAUTH2_SCOPE' AND ROWNUM<2;

    if TRIM(con_name) is not null
    then
      command := 'ALTER TABLE IDN_OAUTH2_RESOURCE_SCOPE DROP CONSTRAINT ' || con_name;
      dbms_output.Put_line(command);
      execute immediate command;
    end if;

    exception
    when NO_DATA_FOUND
    then
    dbms_output.Put_line('Foreign key not found');
  end;

END;
/

ALTER TABLE IDN_OAUTH2_RESOURCE_SCOPE ADD FOREIGN KEY (SCOPE_ID) REFERENCES IDN_OAUTH2_SCOPE(SCOPE_ID) ON DELETE CASCADE
/
