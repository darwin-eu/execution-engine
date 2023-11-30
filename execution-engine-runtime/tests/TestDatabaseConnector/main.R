# Print environment variables and setup db connection if possible
library(DatabaseConnector)

ENV_VARS <- c("DATA_SOURCE_NAME",
              "DBMS_USERNAME",
              "DBMS_PASSWORD",
              "DBMS_TYPE",
              "CONNECTION_STRING",
              "CDM_SCHEMA",
              "WRITE_SCHEMA",
              "RESULT_SCHEMA",
              "COHORT_TARGET_TABLE",
              "BQ_KEYFILE",
              "ANALYSIS_ID",
              "DBMS_CATALOG",
              "DBMS_SERVER",
              "DBMS_NAME",
              "DBMS_PORT",
              "CDM_VERSION")

envVars <- lapply(ENV_VARS, Sys.getenv)
names(envVars) <- ENV_VARS

print("Environment variables that are available")
for (i in seq_along(envVars)) {
  var   <- names(envVars)[i]
  value <- envVars[i]
  if (var != "DBMS_PASSWORD") {
    print(paste(var, ":", value))
  }
}

# try to setup db connection
dbmsType   <- envVars[["DBMS_TYPE"]]
connString <- envVars[["CONNECTION_STRING"]]
dbmsUser   <- envVars[["DBMS_USERNAME"]]
dbmsPwd    <- envVars[["DBMS_PASSWORD"]]
cdmSchema <- envVars[["CDM_SCHEMA"]]

# write results to the /results folder

if (dbmsType != "" && connString != "" && dbmsUser != "" && cdmSchema != "") {
  print("Setting up db connection")
  conn <- DatabaseConnector::connect(dbms = dbmsType,
                                     connectionString = connString,
                                     user = dbmsUser,
                                     password = dbmsPwd,
                                     pathToDriver = "/opt/hades/jdbc_drivers")
  personCount <- dbGetQuery(conn, paste0("SELECT COUNT(*) AS n FROM ", cdmSchema, ".person"))[[1]]
  readr::write_lines(paste("Number of persons:", personCount), "/results/output.txt")
  disconnect(conn)
  print("test complete")
}


