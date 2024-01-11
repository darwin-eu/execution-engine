print(Sys.getenv())


getenv <- function(env) {
  x <- Sys.getenv(env)
  if (x == "") stop(paste("Environment variable", env, "is not set!"))
  x
}

dbConnectUsingEnvironmentVars <- function() {
  port <- Sys.getenv("DBMS_PORT")
  if (port == "") port <- NULL

  stopifnot(getenv("DBMS_TYPE") %in% c("postgresql", "redshift"))

  drv <- switch (getenv("DBMS_TYPE"),
    "postgresql" = RPostgres::Postgres(),
    "redshift" = RPostgres::Redshift()
  )

  DBI::dbConnect(
    drv = drv,
    dbname = getenv("DBMS_NAME"),
    host = getenv("DBMS_SERVER"),
    user = getenv("DBMS_USERNAME"),
    password = getenv("DBMS_PASSWORD"),
    port = port)
}

library(CDMConnector)

print("Connection to the database using environment variables")

con <- dbConnectUsingEnvironmentVars()

if (Sys.getenv("CDM_SCHEMA") == "") {
  stop("CDM schema is empty!")
}

if (Sys.getenv("WRITE_SCHEMA") == "") {
  stop("write schema is empty!")
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


