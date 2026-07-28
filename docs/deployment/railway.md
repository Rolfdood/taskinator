# Railway Deployment Runbook

This service deploys to Railway as a Dockerized Spring Boot API and uses
Supabase Postgres as the production database.

## Railway Service

Use the repository root as the Railway service source. Railway reads
`railway.json`, builds with the root `Dockerfile`, and checks
`/actuator/health` before marking the deployment healthy.

Required Railway variables:

```text
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://<supabase-session-pooler-host>:5432/postgres?sslmode=require&currentSchema=taskinator
DB_USERNAME=taskinator_app.<supabase-project-ref>
DB_PASSWORD=<taskinator_app password>
FLYWAY_USERNAME=taskinator_migrator.<supabase-project-ref>
FLYWAY_PASSWORD=<taskinator_migrator password>
DB_APP_ROLE=taskinator_app
DB_SCHEMA=taskinator
JWT_SECRET=<64+ character random secret>
APP_CORS_ALLOWED_ORIGINS=https://<frontend-domain>
DB_MAX_POOL_SIZE=5
DB_MIN_IDLE=1
```

Use the Supabase session pooler hostname for `DB_URL`. Railway outbound IPv6 is
not required for the session pooler path, and the URL must include
`sslmode=require`.

## Supabase Setup

Run this once in the Supabase SQL editor before the first Railway deploy. Use
strong generated passwords and store them only in Railway variables.

```sql
CREATE ROLE taskinator_migrator LOGIN PASSWORD '<generated migrator password>';
CREATE ROLE taskinator_app LOGIN PASSWORD '<generated app password>';

CREATE SCHEMA IF NOT EXISTS taskinator AUTHORIZATION taskinator_migrator;

GRANT USAGE ON SCHEMA taskinator TO taskinator_app;

ALTER ROLE taskinator_migrator SET search_path = taskinator, public;
ALTER ROLE taskinator_app SET search_path = taskinator, public;

REVOKE EXECUTE ON FUNCTION public.rls_auto_enable() FROM PUBLIC;
REVOKE EXECUTE ON FUNCTION public.rls_auto_enable() FROM anon;
REVOKE EXECUTE ON FUNCTION public.rls_auto_enable() FROM authenticated;
```

Flyway runs with `taskinator_migrator`; the application datasource runs with
`taskinator_app`. The Flyway callback `afterMigrate__grant_app_role.sql` grants
runtime table permissions after each migration.

## Verification

Before promoting a deployment:

```text
GET https://<railway-domain>/actuator/health -> 200 with {"status":"UP"}
GET https://<railway-domain>/api/v1/projects without JWT -> 401
OPTIONS https://<railway-domain>/api/v1/auth/login from APP_CORS_ALLOWED_ORIGINS -> Access-Control-Allow-Origin is returned
OPTIONS https://<railway-domain>/api/v1/auth/login from an unlisted origin -> 403
```

After the first deploy, use Supabase advisors and table inspection:

```text
Security advisor: no lints
Performance advisor: no lints
Tables exist in taskinator schema
No application tables exist in public schema
```
