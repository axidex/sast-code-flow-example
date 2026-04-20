CREATE TABLE IF NOT EXISTS users (
    id    INT PRIMARY KEY,
    name  VARCHAR(100) NOT NULL,
    email VARCHAR(200) NOT NULL
);

INSERT INTO users VALUES (1, 'Alice', 'alice@example.com');
INSERT INTO users VALUES (2, 'Bob',   'bob@example.com');
