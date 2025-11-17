CREATE SCHEMA db;

CREATE USER db_user WITH PASSWORD 'password';
ALTER ROLE db_user SET search_path = db;
GRANT ALL ON SCHEMA db TO db_user;