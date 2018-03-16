ALTER TABLE IDN_OAUTH2_SCOPE
  MODIFY SCOPE_ID INTEGER NOT NULL
/
ALTER TABLE IDN_OAUTH2_SCOPE
  RENAME COLUMN NAME TO DISPLAY_NAME
/
ALTER TABLE IDN_OAUTH2_SCOPE
  RENAME COLUMN SCOPE_KEY TO NAME
/
ALTER TABLE IDN_OAUTH2_SCOPE
  DROP COLUMN ROLES
/
UPDATE IDN_OAUTH2_SCOPE
SET TENANT_ID = -1
WHERE TENANT_ID = 0
/
ALTER TABLE IDN_OAUTH2_SCOPE
  MODIFY TENANT_ID INTEGER DEFAULT -1
/

DECLARE
  con_name     VARCHAR2(100);
  command      VARCHAR2(200);
  databasename VARCHAR2(100);
BEGIN

  SELECT sys_context('userenv', 'current_schema')
  INTO databasename
  FROM dual;

  BEGIN
    SELECT a.constraint_name
    INTO con_name
    FROM all_cons_columns a
      JOIN all_constraints c ON a.owner = c.owner AND a.constraint_name = c.constraint_name
      JOIN all_constraints c_pk ON c.r_owner = c_pk.owner AND c.r_constraint_name = c_pk.constraint_name
    WHERE
      c.constraint_type = 'R' AND a.table_name = 'IDN_OAUTH2_RESOURCE_SCOPE' AND UPPER(a.OWNER) = UPPER(databasename)
      AND c_pk.table_name = 'IDN_OAUTH2_SCOPE' AND ROWNUM < 2;

    IF TRIM(con_name) IS NOT NULL
    THEN
      command := 'ALTER TABLE IDN_OAUTH2_RESOURCE_SCOPE DROP CONSTRAINT ' || con_name;
      dbms_output.Put_line(command);
      EXECUTE IMMEDIATE command;
    END IF;

    EXCEPTION
    WHEN NO_DATA_FOUND
    THEN
    dbms_output.Put_line('Foreign key not found');
  END;

END;
/

ALTER TABLE IDN_OAUTH2_RESOURCE_SCOPE
  ADD FOREIGN KEY (SCOPE_ID) REFERENCES IDN_OAUTH2_SCOPE (SCOPE_ID) ON DELETE CASCADE
/
