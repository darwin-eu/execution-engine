DARWIN Execution Engine API
=======================================

**The Execution Engine is a component used to execute SQL or R code on a CDM database using a UI**

Note: This code started out as a merger of the Arachne Node and Arachne Execution Engine developed by Odysseus which has subsequently
been reshaped with a rather large hammer.

### REQUIREMENTS

- Docker

- For running directly this app directly on your machine: Java 17, Postgres and Maven

### HOW TO USE

- Easiest way to run the execution engine is by using the docker compose file the [Execution Engine
  Compose repo](https://github.com/darwin-eu-dev/execution-engine-compose)

- Otherwise, configuring your database settings in the application.yml in the
  ./src/main/resources folder under spring.datasource or pass them as arguments, then run `mvn spring-boot:run`.

### ENVIRONMENT VARIABLES

The execution engine will supply several environment variable to environment that runs the R code. You can use these
variables which in your R code, they are:

DATA_SOURCE_NAME\
DBMS_USERNAME\
DBMS_PASSWORD\
DBMS_TYPE\
CONNECTION_STRING\
DBMS_CATALOG\
DBMS_SERVER\
DBMS_NAME\
DBMS_PORT\
DBMS_SCHEMA\
CDM_VERSION\
TARGET_SCHEMA\
RESULT_SCHEMA\
COHORT_TARGET_TABLE\
ANALYSIS_ID

It will also set several values by default, these are:

PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin\
LANG=en_US.UTF-8\
LC_ALL=en_US.UTF-8

### DATABASES

Besides running your R code the engine will make some validation queries on your CDM, also there is the possibility to
run an SQL script directly on the DB. The following databases drivers are provided by default:

PostgreSQL\
SQL Server\
Redshift\
Snowflake\
Oracle (OJDBC8)

Other databases could also be supported, but you need to build the code including their respective drivers.
