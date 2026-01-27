CREATE TABLE IF NOT EXISTS db.e2ee_devices (
    device_id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS db.e2ee_user_devices (
    user_id VARCHAR(100) NOT NULL REFERENCES db.users(id) ON DELETE CASCADE,
    device_id VARCHAR(36) NOT NULL REFERENCES db.e2ee_devices(device_id) ON DELETE CASCADE,
    identity_key_public JSONB NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, device_id)
);


CREATE TABLE IF NOT EXISTS db.e2ee_sender_key_envelopes (
    id BIGSERIAL PRIMARY KEY,
    chat_id VARCHAR(100) NOT NULL REFERENCES db.groups(id) ON DELETE CASCADE,
    from_user_id VARCHAR(100) NOT NULL REFERENCES db.users(id) ON DELETE CASCADE,
    from_device_id VARCHAR(36) NOT NULL REFERENCES db.e2ee_devices(device_id) ON DELETE CASCADE,
    to_user_id VARCHAR(100) NOT NULL REFERENCES db.users(id) ON DELETE CASCADE,
    to_device_id VARCHAR(36) NOT NULL REFERENCES db.e2ee_devices(device_id) ON DELETE CASCADE,
    wrapped JSONB NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    consumed_at TIMESTAMPTZ NULL,
    UNIQUE (chat_id, to_user_id, to_device_id)
);


CREATE TABLE IF NOT EXISTS db.e2ee_sender_key_requests (
    id BIGSERIAL PRIMARY KEY,
    chat_id VARCHAR(100) NOT NULL REFERENCES db.groups(id) ON DELETE CASCADE,
    requester_user_id VARCHAR(100) NOT NULL REFERENCES db.users(id) ON DELETE CASCADE,
    requester_device_id VARCHAR(36) NOT NULL REFERENCES db.e2ee_devices(device_id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fulfilled_at TIMESTAMPTZ NULL,
    UNIQUE (chat_id, requester_user_id, requester_device_id, fulfilled_at)
);


ALTER TABLE db.chat_messages
ALTER COLUMN content TYPE TEXT; -- STORES CYPHERTEXT WRAP INSTEAD OF MESSAGES, EXCEEDS >2000 CHAR LIMIT