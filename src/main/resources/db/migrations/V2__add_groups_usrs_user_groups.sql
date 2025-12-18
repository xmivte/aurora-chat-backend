CREATE TABLE IF NOT EXISTS db.users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    image TEXT
    );

CREATE TABLE IF NOT EXISTS db.groups (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(100),
    image TEXT
    );

CREATE TABLE IF NOT EXISTS db.user_groups (
    id SERIAL PRIMARY KEY,
    user_id INTEGER  NOT NULL REFERENCES db.users(id) ON DELETE RESTRICT,
    group_id VARCHAR(100) NOT NULL REFERENCES db.groups(id) ON DELETE RESTRICT,
    last_read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_read_message_id INTEGER REFERENCES db.chat_messages(id) ON DELETE SET NULL,
    unread_count int DEFAULT 0
    );
