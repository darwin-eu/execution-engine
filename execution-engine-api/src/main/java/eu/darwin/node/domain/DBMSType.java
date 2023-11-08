package eu.darwin.node.domain;

import lombok.Getter;

public enum DBMSType {
    POSTGRESQL("PostgreSQL", "postgresql"),
    MS_SQL_SERVER("MS SQL Server", "sql server"),
    DUCKDB("Duckdb", "duckdb"),
    REDSHIFT("Redshift", "redshift"),
    ORACLE("Oracle", "oracle"),
    SPARK("Spark", "spark"),
    SNOWFLAKE("Snowflake", "snowflake");

    private String label;
    @Getter
    private String ohdsiDB;

    DBMSType(String label, String ohdsiDB) {
        this.label = label;
        this.ohdsiDB = ohdsiDB;
    }

    public String getValue() {
        return this.toString();
    }
}
