ALTER TABLE data_source
    DROP COLUMN use_kerberos,
    DROP COLUMN krb_fqdn,
    DROP COLUMN krb_realm,
    DROP COLUMN krb_user,
    DROP COLUMN krb_keytab,
    DROP COLUMN krb_auth_method,
    DROP COLUMN krb_admin_fqdn,
    DROP COLUMN krb_password,
    ADD COLUMN db_catalog TEXT,
    ADD COLUMN db_server TEXT,
    ADD COLUMN db_name TEXT,
    ADD COLUMN db_port TEXT
;

