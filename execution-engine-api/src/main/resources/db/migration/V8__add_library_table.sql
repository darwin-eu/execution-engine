CREATE TABLE library_item
(
    id          BIGSERIAL               NOT NULL PRIMARY KEY,
    name        TEXT                    NOT NULL,
    description TEXT,
    link        TEXT                    NOT NULL,
    entrypoint  TEXT,
    engine      TEXT,
    params      JSONB,
    created     TIMESTAMP DEFAULT NOW() NOT NULL

);
