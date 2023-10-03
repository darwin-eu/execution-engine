CREATE TABLE data_source
(
    id                  BIGSERIAL NOT NULL PRIMARY KEY,
    name                TEXT      NOT NULL,
    description         TEXT,
    type                TEXT      NOT NULL,
    connection_string   TEXT,
    username            TEXT      NOT NULL,
    password            TEXT,
    cdm_schema          TEXT      NOT NULL,
    cohort_target_table TEXT,
    result_schema       TEXT,
    target_schema       TEXT,
    use_kerberos        BOOLEAN,
    krb_fqdn            TEXT,
    krb_realm           TEXT,
    krb_user            TEXT,
    krb_keytab          TEXT,
    krb_auth_method     TEXT,
    krb_admin_fqdn      TEXT,
    krb_password        TEXT
);

CREATE TABLE users
(
    id       BIGSERIAL NOT NULL PRIMARY KEY,
    username TEXT      NOT NULL,
    password TEXT      NOT NULL
);

CREATE TABLE analyses
(
    id                    BIGSERIAL               NOT NULL PRIMARY KEY,
    data_source_id        BIGINT                  NOT NULL
        CONSTRAINT analyses_data_source_id_fk
            REFERENCES data_source,
    analysis_folder       TEXT,
    executable_file_name  TEXT,
    result_status         TEXT,
    study_title           TEXT,
    study_id              TEXT,
    execution_environment TEXT,
    created               TIMESTAMP DEFAULT NOW() NOT NULL,
    user_id               BIGINT
        CONSTRAINT analyses_user_id_fk
            REFERENCES users,
    comment               TEXT,
    finished              TIMESTAMP
);



CREATE TABLE analysis_file
(
    id          BIGSERIAL NOT NULL PRIMARY KEY,
    type        TEXT,
    link        TEXT,
    analysis_id BIGINT
        CONSTRAINT analysis_file_analysis_id_fk
            REFERENCES analyses
);

CREATE INDEX analysis_file_analysis_id_idx
    ON analysis_file (analysis_id);

CREATE TABLE analysis_state_journal
(
    id          BIGSERIAL               NOT NULL PRIMARY KEY,
    state       TEXT,
    date        TIMESTAMP DEFAULT NOW() NOT NULL,
    reason      TEXT,
    analysis_id BIGINT
        CONSTRAINT analysis_state_journal_analysis_id_fk
            REFERENCES analyses
);

CREATE INDEX analysis_state_journal_analysis_id_idx
    ON analysis_state_journal (analysis_id);

CREATE TABLE log
(
    id          BIGSERIAL               NOT NULL PRIMARY KEY,
    line        TEXT,
    analysis_id BIGINT                  NOT NULL
        CONSTRAINT log_analyses_id_fk
            REFERENCES analyses,
    level       TEXT,
    date        TIMESTAMP DEFAULT NOW() NOT NULL
);

CREATE INDEX log_analysis_id_level_idx
    ON log (analysis_id, level);

