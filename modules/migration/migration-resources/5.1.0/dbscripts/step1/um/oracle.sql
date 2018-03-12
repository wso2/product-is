declare
  con_name varchar2(100);
  command varchar2(200);
  databasename VARCHAR2(100);
BEGIN
  databasename := 'SAMPLE';

  begin
    select a.constraint_name into con_name FROM all_cons_columns a JOIN all_constraints c ON a.owner = c.owner AND a.constraint_name = c.constraint_name JOIN all_constraints c_pk ON c.r_owner = c_pk.owner AND c.r_constraint_name = c_pk.constraint_name WHERE c.constraint_type = 'R' AND a.table_name = 'UM_ROLE_PERMISSION' AND UPPER(a.OWNER)=UPPER(databasename) AND c_pk.table_name='UM_PERMISSION' AND ROWNUM<2;

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
    select a.constraint_name into con_name FROM all_cons_columns a JOIN all_constraints c ON a.owner = c.owner AND a.constraint_name = c.constraint_name JOIN all_constraints c_pk ON c.r_owner = c_pk.owner AND c.r_constraint_name = c_pk.constraint_name WHERE c.constraint_type = 'R' AND a.table_name = 'UM_USER_PERMISSION' AND UPPER(a.OWNER)=UPPER(databasename) AND c_pk.table_name='UM_PERMISSION' AND ROWNUM<2;

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
    select a.constraint_name into con_name FROM all_cons_columns a JOIN all_constraints c ON a.owner = c.owner AND a.constraint_name = c.constraint_name JOIN all_constraints c_pk ON c.r_owner = c_pk.owner AND c.r_constraint_name = c_pk.constraint_name WHERE c.constraint_type = 'R' AND a.table_name = 'UM_HYBRID_USER_ROLE' AND UPPER(a.OWNER)=UPPER(databasename) AND c_pk.table_name='UM_HYBRID_ROLE' AND ROWNUM<2;

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

update UM_PERMISSION set UM_RESOURCE_ID = REPLACE(UM_RESOURCE_ID, '-at-', '-AT-') where UM_TENANT_ID <> -1234
/

DELETE FROM UM_CLAIM
WHERE UM_CLAIM_URI = 'http://wso2.org/claims/passwordTimestamp'
/

INSERT INTO UM_CLAIM (
  UM_DIALECT_ID,
  UM_CLAIM_URI,
  UM_DISPLAY_TAG,
  UM_DESCRIPTION,
  UM_MAPPED_ATTRIBUTE,
  UM_TENANT_ID,
  UM_READ_ONLY)
VALUES ((SELECT UM_ID
         FROM UM_DIALECT
         WHERE UM_DIALECT_URI = 'http://wso2.org/claims' AND UM_TENANT_ID = -1234),
        'http://wso2.org/claims/username', 'Username', 'Username', 'uid', -1234, 1)
/

INSERT INTO UM_CLAIM (
  UM_DIALECT_ID,
  UM_CLAIM_URI,
  UM_DISPLAY_TAG,
  UM_DESCRIPTION,
  UM_MAPPED_ATTRIBUTE,
  UM_TENANT_ID,
  UM_READ_ONLY)
  SELECT
    DIALECT.UM_ID,
    'http://wso2.org/username',
    'Username',
    'Username',
    'uid',
    DIALECT.UM_TENANT_ID,
    1
  FROM UM_DIALECT DIALECT
    JOIN UM_TENANT TENANT ON DIALECT.UM_TENANT_ID = TENANT.UM_ID
  WHERE DIALECT.UM_DIALECT_URI = 'http://wso2.org/claims'
/

INSERT INTO UM_CLAIM (
  UM_DIALECT_ID,
  UM_CLAIM_URI,
  UM_DISPLAY_TAG,
  UM_DESCRIPTION,
  UM_MAPPED_ATTRIBUTE,
  UM_TENANT_ID,
  UM_READ_ONLY)
VALUES ((SELECT UM_ID
         FROM UM_DIALECT
         WHERE UM_DIALECT_URI = 'http://wso2.org/claims' AND UM_TENANT_ID = -1234),
        'http://wso2.org/claims/identity/failedLoginAttempts', 'Failed Login Attempts', 'Failed Login Attempts',
        'failedLoginAttempts', -1234, 1)
/

INSERT INTO UM_CLAIM (
  UM_DIALECT_ID,
  UM_CLAIM_URI,
  UM_DISPLAY_TAG,
  UM_DESCRIPTION,
  UM_MAPPED_ATTRIBUTE,
  UM_TENANT_ID,
  UM_READ_ONLY)
  SELECT
    DIALECT.UM_ID,
    'http://wso2.org/claims/identity/failedLoginAttempts',
    'Failed Login Attempts',
    'Failed Login Attempts',
    'failedLoginAttempts',
    DIALECT.UM_TENANT_ID,
    1
  FROM UM_DIALECT DIALECT
    JOIN UM_TENANT TENANT ON DIALECT.UM_TENANT_ID = TENANT.UM_ID
  WHERE DIALECT.UM_DIALECT_URI = 'http://wso2.org/claims'
/

INSERT INTO UM_CLAIM (
  UM_DIALECT_ID,
  UM_CLAIM_URI,
  UM_DISPLAY_TAG,
  UM_DESCRIPTION,
  UM_MAPPED_ATTRIBUTE,
  UM_TENANT_ID,
  UM_READ_ONLY)
VALUES ((SELECT UM_ID
         FROM UM_DIALECT
         WHERE UM_DIALECT_URI = 'http://wso2.org/claims' AND UM_TENANT_ID = -1234),
        'http://wso2.org/claims/identity/unlockTime', 'Unlock Time', 'Unlock Time', 'unlockTime', -1234, 1)
/

INSERT INTO UM_CLAIM (
  UM_DIALECT_ID,
  UM_CLAIM_URI,
  UM_DISPLAY_TAG,
  UM_DESCRIPTION,
  UM_MAPPED_ATTRIBUTE,
  UM_TENANT_ID,
  UM_READ_ONLY)
  SELECT
    DIALECT.UM_ID,
    'http://wso2.org/claims/identity/unlockTime',
    'Unlock Time',
    'Unlock Time',
    'unlockTime',
    DIALECT.UM_TENANT_ID,
    1
  FROM UM_DIALECT DIALECT
    JOIN UM_TENANT TENANT ON DIALECT.UM_TENANT_ID = TENANT.UM_ID
  WHERE DIALECT.UM_DIALECT_URI = 'http://wso2.org/claims'
/

INSERT INTO UM_CLAIM (
  UM_DIALECT_ID,
  UM_CLAIM_URI,
  UM_DISPLAY_TAG,
  UM_DESCRIPTION,
  UM_MAPPED_ATTRIBUTE,
  UM_TENANT_ID,
  UM_READ_ONLY)
VALUES ((SELECT UM_ID
         FROM UM_DIALECT
         WHERE UM_DIALECT_URI = 'http://wso2.org/claims' AND UM_TENANT_ID = -1234),
        'http://wso2.org/claims/displayName', 'Display Name', 'Display Name', 'displayName', -1234, 1)
/

INSERT INTO UM_CLAIM (
  UM_DIALECT_ID,
  UM_CLAIM_URI,
  UM_DISPLAY_TAG,
  UM_DESCRIPTION,
  UM_MAPPED_ATTRIBUTE,
  UM_TENANT_ID,
  UM_READ_ONLY)
  SELECT
    DIALECT.UM_ID,
    'http://wso2.org/claims/displayName',
    'Display Name',
    'Display Name',
    'displayName',
    DIALECT.UM_TENANT_ID,
    1
  FROM UM_DIALECT DIALECT
    JOIN UM_TENANT TENANT ON DIALECT.UM_TENANT_ID = TENANT.UM_ID
  WHERE DIALECT.UM_DIALECT_URI = 'http://wso2.org/claims'
/