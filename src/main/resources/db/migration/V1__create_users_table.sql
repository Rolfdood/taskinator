CREATE TABLE IF NOT EXISTS users(
                       id          BIGSERIAL PRIMARY KEY,
                       email       VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       first_name  VARCHAR(255) NOT NULL,
                       middle_name VARCHAR(255),
                       last_name   VARCHAR(255) NOT NULL,
                       suffix      VARCHAR(255),
                       created_at  TIMESTAMP DEFAULT NOW()
);