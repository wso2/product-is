DROP TABLE IF EXISTS IDN_OAUTH2_SCOPE_BINDING;
CREATE TABLE IF NOT EXISTS IDN_OAUTH2_SCOPE_BINDING (
            SCOPE_ID INTEGER NOT NULL,
            SCOPE_BINDING VARCHAR(255),
            FOREIGN KEY (SCOPE_ID) REFERENCES IDN_OAUTH2_SCOPE(SCOPE_ID) ON DELETE CASCADE)
);

DO $$ DECLARE con_name varchar(200); BEGIN SELECT 'ALTER TABLE IDN_OAUTH2_RESOURCE_SCOPE DROP CONSTRAINT ' || tc .constraint_name || ';' INTO con_name FROM information_schema.table_constraints AS tc JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name WHERE constraint_type = 'FOREIGN KEY' AND tc.table_name = 'IDN_OAUTH2_RESOURCE_SCOPE' AND ccu.table_name='IDN_OAUTH2_SCOPE' LIMIT 1; EXECUTE con_name; END $$;

ALTER TABLE IDN_OAUTH2_RESOURCE_SCOPE ADD FOREIGN KEY (SCOPE_ID) REFERENCES IDN_OAUTH2_SCOPE(SCOPE_ID) ON DELETE CASCADE;