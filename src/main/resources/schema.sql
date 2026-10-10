-- Runs on every startup (spring.sql.init.mode=always in application.properties).
-- "IF NOT EXISTS" makes it safe: the table is created the first time, then left alone,
-- so your data survives restarts.

CREATE TABLE IF NOT EXISTS users (
    id       BIGINT       NOT NULL AUTO_INCREMENT,   -- matches: Long id + @GeneratedValue(IDENTITY)
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(150) NOT NULL,
    password VARCHAR(255),
    role     VARCHAR(30),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
);

-- Used by JdbcVsR2dbcTest
CREATE TABLE IF NOT EXISTS person (
    id   INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- Seed rows; INSERT IGNORE skips ids that already exist, so restarts are safe
INSERT IGNORE INTO person (id, name) VALUES
    (1, 'Koushik'),
    (2, 'Ravi'),
    (3, 'Anita');
