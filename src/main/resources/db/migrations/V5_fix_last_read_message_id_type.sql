ALTER TABLE db.user_groups
    DROP CONSTRAINT IF EXISTS user_groups_last_read_message_id_fkey;

ALTER TABLE db.user_groups
    ALTER COLUMN last_read_message_id TYPE BIGINT;

ALTER TABLE db.user_groups
    ADD CONSTRAINT user_groups_last_read_message_id_fkey
        FOREIGN KEY (last_read_message_id)
            REFERENCES db.chat_messages(id)
            ON DELETE SET NULL;