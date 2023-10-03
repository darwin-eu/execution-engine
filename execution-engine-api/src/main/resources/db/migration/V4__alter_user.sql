ALTER TABLE users
    ADD COLUMN oidc_id TEXT;

ALTER TABLE users
    DROP COLUMN password;

ALTER TABLE users
    DROP CONSTRAINT users_username_unique;
