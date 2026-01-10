CREATE TABLE IF NOT EXISTS db.file_attachments (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES db.chat_messages(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
);

-- Index for fast lookup by message_id
CREATE INDEX idx_file_attachments_message_id ON db.file_attachments(message_id);

-- Index for fast lookup by expires_at
CREATE INDEX idx_file_attachments_expires_at ON db.file_attachments(expires_at);

-- Messages can have no text (if file is attached)
ALTER TABLE db.chat_messages
    ALTER COLUMN content DROP NOT NULL;

