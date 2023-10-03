WRITING CODE THAT CAN INTERACT WITH THE EXECUTION ENGINE
========================================================

If you want to be able to run your R code in the execution engine there are some prerequisites that must be met.

1. Your code should be uploaded in zip format
2. Include a file called execution-config.yml, this file should look like this
   ```yaml
   studyId: "2"
   studyTitle: "cohort"
   engine: "executionengine.azurecr.io/darwin-runtime:latest"
   entrypoint: "folder/codeToRun.R"
   ```
   Here the engine must reference a docker image that can either be pulled from the internet or loaded locally.
3. If you are creating your own custom image it must include a `/code` folder, and it should be able to execute
   the `Rscript` command
4. Any results that you want to be included in the download at the end must be written to a `/results` folder
5. During runtime, you have access to the following environment variables:
   DATA_SOURCE_NAME\
   DBMS_USERNAME\
   DBMS_PASSWORD\
   DBMS_TYPE\
   CONNECTION_STRING\
   DBMS_SCHEMA\
   TARGET_SCHEMA\
   RESULT_SCHEMA\
   COHORT_TARGET_TABLE\
   BQ_KEYFILE\
   ANALYSIS_ID

   At present DBMS_TYPE may could be any of the following values
   postgresql\
   sql server\
   pdw\
   redshift\
   oracle\
   impala\
   bigquery\
   netezza\
   hive\
   spark\
   snowflake\
   synapse\
