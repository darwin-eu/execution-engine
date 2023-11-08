# print(Sys.getenv())

vars <- c("DBMS_TYPE",
          "DATA_SOURCE_NAME",
          "CDM_VERSION",
          "DBMS_CATALOG",
          "DBMS_SERVER",
          "DBMS_NAME",
          "DBMS_PORT",
          "DBMS_USERNAME",
          "DBMS_PASSWORD",
          "CDM_SCHEMA",
          "WRITE_SCHEMA"
)

for (v in vars) {
  print(paste0(v, ": ", Sys.getenv(v, unset = "MISSING!!!")))
}

cdm_from_environment <- function(write_prefix = "") {

  vars <- c("DBMS_TYPE",
            "DATA_SOURCE_NAME",
            "CDM_VERSION",
            "DBMS_CATALOG",
            "DBMS_SERVER",
            "DBMS_NAME",
            "DBMS_PORT",
            "DBMS_USERNAME",
            "DBMS_PASSWORD",
            "CDM_SCHEMA",
            "WRITE_SCHEMA")

  supported_dbms <- c("redshift", "postgresql", "sql server", "duckdb", "snowflake")

  if (!(Sys.getenv("DBMS_TYPE") %in% supported_dbms)) {
    cli::cli_abort("DBMS_TYPE must be one of {paste(supported_dbms, collapse = ',')}")
  }

  if (Sys.getenv("DBMS_TYPE") == "duckdb") {
    checkmate::check_choice(Sys.getenv("DBMS_NAME"), choices = CDMConnector::example_datasets())

    con <- DBI::dbConnect(duckdb::duckdb(), CDMConnector::eunomia_dir(Sys.getenv("DBMS_NAME")))
    cdm <- cdm_from_con(con, cdm_schema = "main", write_schema = "main")
    return(cdm)

  } else {
    # "DBMS_CATALOG" is not required
    for (v in vars) {
      if (Sys.getenv(v) == "" && v != "DBMS_CATALOG") {
        cli::cli_abort("Environment variable {v} is required but not set!")
      }
    }
  }

  stringr::str_count(Sys.getenv("CDM_SCHEMA"), "\\.")

  if (Sys.getenv("DBMS_TYPE") %in% c("postgresql", "redshift")) {

    drv <- switch (Sys.getenv("DBMS_TYPE"),
      "postgresql" = RPostgres::Postgres(),
      "redshift" = RPostgres::Redshift()
    )

    con <- DBI::dbConnect(drv = drv,
                          dbname   = Sys.getenv("DBMS_NAME"),
                          host     = Sys.getenv("DBMS_SERVER"),
                          user     = Sys.getenv("DBMS_USERNAME"),
                          password = Sys.getenv("DBMS_PASSWORD"),
                          port     = Sys.getenv("DBMS_PORT"))

    if (!DBI::dbIsValid(con)) {
      cli::cli_abort("Database connection failed!")
    }

  } else if (Sys.getenv("DBMS_TYPE") == "sql server") {

      con <- DBI::dbConnect(odbc::odbc(),
                            Driver   = "ODBC Driver 17 for SQL Server",
                            Server   = Sys.getenv("DBMS_SERVER"),
                            Database = Sys.getenv("DBMS_NAME"),
                            UID      = Sys.getenv("DBMS_USERNAME"),
                            PWD      = Sys.getenv("DBMS_PASSWORD"),
                            TrustServerCertificate="yes",
                            Port     = Sys.getenv("DBMS_PORT"))


  } else if (Sys.getenv("DBMS_TYPE") == "snowflake") {
    con <- DBI::dbConnect(odbc::odbc(),
                          DRIVER    = "SnowflakeDSIIDriver",
                          SERVER    = Sys.getenv("DBMS_SERVER"),
                          DATABASE  = Sys.getenv("DBMS_NAME"),
                          UID       = Sys.getenv("DBMS_USERNAME"),
                          PWD       = Sys.getenv("DBMS_PASSWORD"),
                          WAREHOUSE = "COMPUTE_WH_XS")

  } else {
    cli::cli_abort("{Sys.getenv('DBMS_TYPE')} is not a supported database type!")
  }

  # split schemas. If write schema has a dot we need to interpret it as catalog.schema
  # cdm schema should not have a dot

  if (stringr::str_detect(Sys.getenv("WRITE_SCHEMA"), "\\.")) {
    write_schema <- stringr::str_split(Sys.getenv("WRITE_SCHEMA"), "\\.")[[1]]
    if (length(write_schema) != 2) {
      cli::cli_abort("write_schema can have at most one period (.)!")
    }

    stopifnot(nchars(write_schema[1]) > 0, nchars(write_schema[2]) > 0)
    write_schema <- c(catalog = write_schema[1], schema = write_schema[2])
  } else {
    write_schema <- c(schema = Sys.getenv("WRITE_SCHEMA"))
  }

  if (write_prefix != "") {
    write_schema <- c(write_schema, prefix = write_prefix)
  }

  if (stringr::str_detect(Sys.getenv("CDM_SCHEMA"), "\\.")) {
    cli::cli_abort("CDM_SCHEMA cannot contain a period (.)! Use DBMS_CATALOG to add a catalog.")
  }

  if (Sys.getenv("DBMS_CATALOG") != "") {
    cdm_schema <- c(catalog = Sys.getenv("DBMS_CATALOG"), schema = Sys.getenv("CDM_SCHEMA"))
  } else {
    cdm_schema <- Sys.getenv("CDM_SCHEMA")
  }

  cdm <- CDMConnector::cdm_from_con(
    con = con,
    cdm_schema = cdm_schema,
    write_schema = write_schema,
    cdm_version = Sys.getenv("CDM_VERSION"),
    cdm_name = Sys.getenv("DATA_SOURCE_NAME"))

  if (length(names(cdm)) == 0) {
    cli::cli_abort("CDM object creation failed!")
  }

  return(cdm)
}


library(CDMConnector)
library(dplyr, warn.conflicts = F)

print(sessionInfo())

print("Connection to the database using environment variables")
cdm <- cdm_from_environment()

print("Querying the CDM with dplyr")

personCount <- cdm$person %>%
  count(name = "n") %>%
  pull(n)

message(paste("Number of persons:", personCount))

readr::write_lines(paste("Number of persons:", personCount), "/results/output.txt")

CDMConnector::cdm_disconnect(cdm)

cli::cat_rule("CDMConnector test complete!")


