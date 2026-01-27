CREATE TABLE IF NOT EXISTS db.servers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    user_email VARCHAR(100) NOT NULL REFERENCES db.users(email),
    background_color_hex VARCHAR(15)
);

CREATE TABLE IF NOT EXISTS db.server_groups (
    id BIGSERIAL PRIMARY KEY,
    server_id INTEGER NOT NULL REFERENCES db.servers(id),
    group_id VARCHAR(100) NOT NULL REFERENCES db.groups(id)
);

CREATE TABLE IF NOT EXISTS db.server_group_users (
    id BIGSERIAL PRIMARY KEY,
    server_group_id INTEGER NOT NULL REFERENCES db.server_groups(id),
    user_email VARCHAR(100) NOT NULL REFERENCES db.users(email)
);