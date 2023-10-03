print(Sys.getenv())

dbConnectUsingEnvironmentVars <- function() {

  getenv <- function(env) {
    x <- Sys.getenv(env)
    if (x == "") stop(paste("Environment variable", env, "is not set!"))
    x
  }

  port <- Sys.getenv("DBMS_PORT")
  if (port == "") port <- NULL

  if (getenv("DBMS_TYPE") %in% c("postgresql", "redshift")) {

    drv <- switch (getenv("DBMS_TYPE"),
      "postgresql" = RPostgres::Postgres(),
      "redshift" = RPostgres::Redshift()
    )

    return(DBI::dbConnect(drv = drv,
                          dbname = getenv("DBMS_NAME"),
                          host = getenv("DBMS_SERVER"),
                          user = getenv("DBMS_USERNAME"),
                          password = getenv("DBMS_PASSWORD"),
                          port = port))
  }

  if (getenv("DBMS_TYPE") == "sql server") {

    return(DBI::dbConnect(odbc::odbc(),
                          Driver   = "ODBC Driver 18 for SQL Server",
                          Server   = getenv("DBMS_SERVER"),
                          Database = getenv("DBMS_NAME"),
                          UID      = getenv("DBMS_USERNAME"),
                          PWD      = getenv("DBMS_PASSWORD"),
                          TrustServerCertificate="yes",
                          Port     = port))
  }
  #
  # if (getenv("DBMS_TYPE") == "oracle") {
  #   return(DBI::dbConnect(odbc::odbc(), "OracleODBC-19"))
  # }
  #
  #
  # if (dbms == "snowflake") {
  #   return(DBI::dbConnect(odbc::odbc(),
  #                         SERVER = getenv("SNOWFLAKE_SERVER"),
  #                         UID = getenv("SNOWFLAKE_USER"),
  #                         PWD = getenv("SNOWFLAKE_PASSWORD"),
  #                         DATABASE = getenv("SNOWFLAKE_DATABASE"),
  #                         WAREHOUSE = getenv("SNOWFLAKE_WAREHOUSE"),
  #                         DRIVER = getenv("SNOWFLAKE_DRIVER")))
  # }
  #
  # if (dbms == "spark") {
  #   return(DBI::dbConnect(odbc::odbc(), "Databricks", bigint = "numeric"))
  # }

  stop("database type not supported yet")
}

library(CDMConnector)

print("Connection to the database using environment variables")

con <- dbConnectUsingEnvironmentVars()

if (Sys.getenv("DBMS_SCHEMA") == "") {
  stop("CDM schema is empty!")
}

if (Sys.getenv("TARGET_SCHEMA") == "") {
  stop("Target (write) schema is empty!")
}

strsplit(Sys.getenv("DBMS_SCHEMA"),"\\.")[[1]]

print("creating CDM object")
cdm <- cdmFromCon(con,
                  cdmSchema = strsplit(Sys.getenv("DBMS_SCHEMA"),"\\.")[[1]],
                  writeSchema = strsplit(Sys.getenv("TARGET_SCHEMA"),"\\.")[[1]],
                  cdmName = Sys.getenv("DATA_SOURCE_NAME"))

library(dplyr, warn.conflicts = F)

print("Querying the CDM with dplyr")

personCount <- cdm$person %>%
  count(name = "n") %>%
  pull(n)

readr::write_lines(paste("Number of persons:", personCount), "/results/output.txt")

print("test complete!")


