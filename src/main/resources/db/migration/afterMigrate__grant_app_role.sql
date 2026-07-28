DO $$
DECLARE
    app_role_name text := '${app_role}';
    app_schema_name text := '${app_schema}';
BEGIN
    IF app_role_name IS NULL
        OR btrim(app_role_name) = ''
        OR NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = app_role_name) THEN
        RAISE NOTICE 'Skipping runtime grants because role "%" does not exist', app_role_name;
        RETURN;
    END IF;

    EXECUTE format('GRANT USAGE ON SCHEMA %I TO %I', app_schema_name, app_role_name);
    EXECUTE format(
        'GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA %I TO %I',
        app_schema_name,
        app_role_name
    );
    EXECUTE format(
        'GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA %I TO %I',
        app_schema_name,
        app_role_name
    );
    EXECUTE format(
        'ALTER DEFAULT PRIVILEGES IN SCHEMA %I GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO %I',
        app_schema_name,
        app_role_name
    );
    EXECUTE format(
        'ALTER DEFAULT PRIVILEGES IN SCHEMA %I GRANT USAGE, SELECT ON SEQUENCES TO %I',
        app_schema_name,
        app_role_name
    );
END $$;
