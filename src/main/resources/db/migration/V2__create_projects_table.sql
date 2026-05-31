CREATE TABLE IF NOT EXISTS projects (
                          id          BIGSERIAL PRIMARY KEY,
                          name        VARCHAR(255) NOT NULL,
                          description TEXT,
                          user_id     BIGINT NOT NULL,
                          created_at  TIMESTAMP DEFAULT NOW(),

                          CONSTRAINT fk_projects_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users (id)
                                  ON DELETE CASCADE
);

CREATE INDEX idx_projects_user_id ON projects (user_id);