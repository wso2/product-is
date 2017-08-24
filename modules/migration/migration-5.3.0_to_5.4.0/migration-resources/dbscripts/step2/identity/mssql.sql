     scopeKeySetNullQueryMap.put(DatabaseTypes.mysql.toString(), "ALTER TABLE IDN_OAUTH2_SCOPE MODIFY "
                                                                    + "SCOPE_KEY VARCHAR (100) NULL");
        scopeKeySetNullQueryMap.put(DatabaseTypes.oracle.toString(), "ALTER TABLE IDN_OAUTH2_SCOPE MODIFY "
                                                                     + "SCOPE_KEY VARCHAR2 (100) NULL");
        scopeKeySetNullQueryMap.put(DatabaseTypes.mssql.toString(), "ALTER TABLE IDN_OAUTH2_SCOPE ALTER "
                                                                    + "COLUMN SCOPE_KEY VARCHAR (100) NULL");
        scopeKeySetNullQueryMap.put(DatabaseTypes.db2.toString(), "ALTER TABLE IDN_OAUTH2_SCOPE ALTER "
                                                                  + "COLUMN SCOPE_KEY SET NULL");
        scopeKeySetNullQueryMap.put(DatabaseTypes.h2.toString(), "ALTER TABLE IDN_OAUTH2_SCOPE ALTER "
                                                                 + "COLUMN SCOPE_KEY VARCHAR (100) NULL");
        scopeKeySetNullQueryMap.put(DatabaseTypes.postgresql.toString(), "ALTER TABLE IDN_OAUTH2_SCOPE "
                                                                         + "ALTER COLUMN SCOPE_KEY SET NULL");