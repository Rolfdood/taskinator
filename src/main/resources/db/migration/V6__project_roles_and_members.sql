CREATE TABLE IF NOT EXISTS project_roles (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_project_roles_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT uq_project_roles_project_name UNIQUE (project_id, name)
);

CREATE TABLE IF NOT EXISTS project_role_permissions (
    role_id UUID NOT NULL,
    permission VARCHAR(50) NOT NULL,
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES project_roles(id) ON DELETE CASCADE,
    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission)
);

CREATE TABLE IF NOT EXISTS project_members (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    project_id UUID NOT NULL,
    role_id UUID NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_project_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_members_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_members_role FOREIGN KEY (role_id) REFERENCES project_roles(id) ON DELETE CASCADE,
    CONSTRAINT uq_project_members UNIQUE (user_id, project_id)
);

ALTER TABLE tasks ADD COLUMN assigned_to UUID;
ALTER TABLE tasks ADD CONSTRAINT fk_tasks_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL;
