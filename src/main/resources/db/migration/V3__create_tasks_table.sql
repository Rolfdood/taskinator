CREATE TABLE tasks (
                       id          BIGSERIAL PRIMARY KEY,
                       title       VARCHAR(255) NOT NULL,
                       description TEXT,
                       status      VARCHAR(50) NOT NULL DEFAULT 'TODO',
                       due_date    DATE,
                       project_id  BIGINT NOT NULL,
                       created_at  TIMESTAMP DEFAULT NOW(),

                       CONSTRAINT fk_tasks_project
                           FOREIGN KEY (project_id)
                               REFERENCES projects (id)
                               ON DELETE CASCADE
);

CREATE INDEX idx_tasks_project_id ON tasks (project_id);
CREATE INDEX idx_tasks_status ON tasks (status);