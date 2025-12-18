ALTER TABLE db.user_groups
DROP CONSTRAINT user_groups_user_id_fkey;

ALTER TABLE db.users
ALTER COLUMN id TYPE VARCHAR(100);

ALTER TABLE db.chat_messages
ALTER COLUMN sender_id TYPE VARCHAR(100);

ALTER TABLE db.user_groups
ALTER COLUMN user_id TYPE VARCHAR(100);

ALTER TABLE db.user_groups
ADD CONSTRAINT user_groups_user_id_fkey
FOREIGN KEY (user_id) REFERENCES db.users(id);