CREATE TABLE container
(
    id           BIGSERIAL NOT NULL PRIMARY KEY,
    container_id TEXT      NOT NULL,
    status       TEXT      NOT NULL,
    analysis_id  BIGINT    NOT NULL
        CONSTRAINT container_analysis_id_fk
            REFERENCES analyses
);

CREATE INDEX container_analysis_id_idx
    ON container (analysis_id);

CREATE INDEX container_container_id_idx
    ON container (container_id);
