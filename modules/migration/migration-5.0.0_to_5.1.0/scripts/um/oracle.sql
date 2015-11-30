declare
  con_name varchar2(100);
  command varchar2(200);
  databasename VARCHAR2(100);
BEGIN
  databasename := 'SAMPLE';

  begin
    select a.constraint_name into con_name FROM all_cons_columns a JOIN all_constraints c ON a.owner = c.owner AND a.constraint_name = c.constraint_name JOIN all_constraints c_pk ON c.r_owner = c_pk.owner AND c.r_constraint_name = c_pk.constraint_name WHERE c.constraint_type = 'R' AND a.table_name = 'UM_ROLE_PERMISSION' AND a.OWNER=databasename AND c_pk.table_name='UM_PERMISSION' AND ROWNUM<2;

    if TRIM(con_name) is not null
    then
      command := 'ALTER TABLE UM_ROLE_PERMISSION DROP CONSTRAINT ' || con_name;
      dbms_output.Put_line(command);
      execute immediate command;
    end if;

    exception
    when NO_DATA_FOUND
    then
    dbms_output.Put_line('Foreign key not found');
  end;

  begin
    select a.constraint_name into con_name FROM all_cons_columns a JOIN all_constraints c ON a.owner = c.owner AND a.constraint_name = c.constraint_name JOIN all_constraints c_pk ON c.r_owner = c_pk.owner AND c.r_constraint_name = c_pk.constraint_name WHERE c.constraint_type = 'R' AND a.table_name = 'UM_USER_PERMISSION' AND a.OWNER=databasename AND c_pk.table_name='UM_PERMISSION' AND ROWNUM<2;

    if TRIM(con_name) is not null
    then
      command := 'ALTER TABLE UM_USER_PERMISSION DROP CONSTRAINT ' || con_name;
      dbms_output.Put_line(command);
      execute immediate command;
    end if;

    exception
    when NO_DATA_FOUND
    then
    dbms_output.Put_line('Foreign key not found');
  end;

  begin
    select a.constraint_name into con_name FROM all_cons_columns a JOIN all_constraints c ON a.owner = c.owner AND a.constraint_name = c.constraint_name JOIN all_constraints c_pk ON c.r_owner = c_pk.owner AND c.r_constraint_name = c_pk.constraint_name WHERE c.constraint_type = 'R' AND a.table_name = 'UM_HYBRID_USER_ROLE' AND a.OWNER=databasename AND c_pk.table_name='UM_HYBRID_ROLE' AND ROWNUM<2;

    if TRIM(con_name) is not null
    then
      command := 'ALTER TABLE UM_HYBRID_USER_ROLE DROP CONSTRAINT ' || con_name;
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

ALTER TABLE UM_ROLE_PERMISSION ADD FOREIGN KEY (UM_PERMISSION_ID, UM_TENANT_ID) REFERENCES UM_PERMISSION(UM_ID, UM_TENANT_ID) ON DELETE CASCADE
/
ALTER TABLE UM_USER_PERMISSION ADD FOREIGN KEY (UM_PERMISSION_ID, UM_TENANT_ID) REFERENCES UM_PERMISSION(UM_ID, UM_TENANT_ID) ON DELETE CASCADE
/
ALTER TABLE UM_HYBRID_USER_ROLE ADD FOREIGN KEY (UM_ROLE_ID, UM_TENANT_ID) REFERENCES UM_HYBRID_ROLE(UM_ID, UM_TENANT_ID) ON DELETE CASCADE
/