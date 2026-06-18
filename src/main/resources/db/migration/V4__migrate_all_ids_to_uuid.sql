-- ==========================================
-- STEP 1: Drop FK constraints (children first)
-- ==========================================
ALTER TABLE tasks DROP CONSTRAINT fk_tasks_project;
ALTER TABLE projects DROP CONSTRAINT fk_projects_user;

-- ==========================================
-- STEP 2: Drop indexes on old FK columns
-- ==========================================
DROP INDEX idx_projects_user_id;
DROP INDEX idx_tasks_project_id;

-- ==========================================
-- STEP 3: Add new UUID columns to all tables
-- ==========================================
ALTER TABLE users    ADD COLUMN new_id UUID DEFAULT gen_random_uuid() NOT NULL;
ALTER TABLE projects ADD COLUMN new_id      UUID DEFAULT gen_random_uuid() NOT NULL;
ALTER TABLE projects ADD COLUMN new_user_id UUID;
ALTER TABLE tasks    ADD COLUMN new_id         UUID DEFAULT gen_random_uuid() NOT NULL;
ALTER TABLE tasks    ADD COLUMN new_project_id UUID;

-- ==========================================
-- STEP 4: Backfill FK UUID columns by joining
-- ==========================================
UPDATE projects p
SET new_user_id = u.new_id
FROM users u
WHERE p.user_id = u.id;

UPDATE tasks t
SET new_project_id = p.new_id
FROM projects p
WHERE t.project_id = p.id;

-- ==========================================
-- STEP 5: Drop old PKs and legacy columns
-- ==========================================

-- users
ALTER TABLE users DROP CONSTRAINT users_pkey;
ALTER TABLE users DROP COLUMN id;
ALTER TABLE users RENAME COLUMN new_id TO id;
ALTER TABLE users ADD PRIMARY KEY (id);
ALTER TABLE users ALTER COLUMN id SET DEFAULT gen_random_uuid();

-- projects
ALTER TABLE projects DROP CONSTRAINT projects_pkey;
ALTER TABLE projects DROP COLUMN id;
ALTER TABLE projects DROP COLUMN user_id;
ALTER TABLE projects RENAME COLUMN new_id TO id;
ALTER TABLE projects RENAME COLUMN new_user_id TO user_id;
ALTER TABLE projects ADD PRIMARY KEY (id);
ALTER TABLE projects ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE projects ALTER COLUMN user_id SET NOT NULL;

-- tasks
ALTER TABLE tasks DROP CONSTRAINT tasks_pkey;
ALTER TABLE tasks DROP COLUMN id;
ALTER TABLE tasks DROP COLUMN project_id;
ALTER TABLE tasks RENAME COLUMN new_id TO id;
ALTER TABLE tasks RENAME COLUMN new_project_id TO project_id;
ALTER TABLE tasks ADD PRIMARY KEY (id);
ALTER TABLE tasks ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE tasks ALTER COLUMN project_id SET NOT NULL;

-- ==========================================
-- STEP 6: Recreate FK constraints
-- ==========================================
ALTER TABLE projects ADD CONSTRAINT fk_projects_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE tasks ADD CONSTRAINT fk_tasks_project
    FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE;

-- ==========================================
-- STEP 7: Recreate indexes
-- ==========================================
CREATE INDEX idx_projects_user_id ON projects (user_id);
CREATE INDEX idx_tasks_project_id ON tasks (project_id);