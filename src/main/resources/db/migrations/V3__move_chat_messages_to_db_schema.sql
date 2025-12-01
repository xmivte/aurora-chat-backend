CREATE TABLE IF NOT EXISTS db.chat_messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT,
    group_id VARCHAR(100),
    content VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent BOOLEAN DEFAULT FALSE
);

DROP TABLE IF EXISTS public.chat_messages;