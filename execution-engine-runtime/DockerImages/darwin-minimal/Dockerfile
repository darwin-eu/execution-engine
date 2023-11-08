FROM --platform=linux/amd64 rocker/r-ver:4.2
MAINTAINER Adam Black <adam.black@odysseusinc.com>

# Install java and rJava
RUN apt-get -y update && apt-get install -y \
   default-jdk \
   r-cran-rjava \
   sudo \
   && apt-get clean \
   && rm -rf /var/lib/apt/lists/ \
   && sudo R CMD javareconf

RUN install2.r --error rJava \
&& rm -rf /tmp/download_packages/ /tmp/*.rds

RUN install2.r --error DatabaseConnector \
&& rm -rf /tmp/download_packages/ /tmp/*.rds

ENV DATABASECONNECTOR_JAR_FOLDER="/opt/hades/jdbc_drivers"
RUN R -e "DatabaseConnector::downloadJdbcDrivers('all');"

RUN install2.r --error Andromeda \
&& rm -rf /tmp/download_packages/ /tmp/*.rds

# install utility R packages
RUN apt-get -y update && apt-get install -y \
    libxml2-dev libssl-dev libcurl4-openssl-dev \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/

RUN install2.r --error openssl httr xml2 remotes \
    && rm -rf /tmp/download_packages/ /tmp/*.rds

# Install odbc and RPostgres drivers
RUN apt-get -y update && apt-get install -y --install-suggests \
   unixodbc unixodbc-dev libpq-dev curl \
   && apt-get clean \
   && rm -rf /var/lib/apt/lists/

RUN install2.r --error odbc RPostgres duckdb \
   && rm -rf /tmp/downloaded_packages/ /tmp/*.rds

RUN echo 'options(repos = c(CRAN = "https://packagemanager.posit.co/cran/2023-11-01"))' >>"${R_HOME}/etc/Rprofile.site"

# Install Darwin packages
RUN R -e "remotes::install_github('ohdsi/CirceR')"
RUN R -e "remotes::install_github('ohdsi/Capr')"

RUN install2.r --deps TRUE --error --skipinstalled --ncpus -1 \
        CDMConnector \
        IncidencePrevalence \
        PatientProfiles \
        TreatmentPatterns \
        DrugExposureDiagnostics \
        DrugUtilisation \
&& rm -rf /tmp/download_packages/ /tmp/*.rds

RUN echo "DATABASECONNECTOR_JAR_FOLDER=/opt/hades/jdbc_drivers" >> /usr/local/lib/R/etc/Renviron
RUN echo "RENV_PATHS_CELLAR=/opt/renv_cellar" >> /usr/local/lib/R/etc/Renviron

# SQL Server odbc
RUN curl https://packages.microsoft.com/keys/microsoft.asc | sudo tee /etc/apt/trusted.gpg.d/microsoft.asc
RUN curl https://packages.microsoft.com/config/ubuntu/22.04/prod.list | sudo tee /etc/apt/sources.list.d/mssql-release.list
RUN apt clean && apt-get update && ACCEPT_EULA=Y apt-get install -y msodbcsql17

# Snowflake odbc
RUN curl --output snowflake-odbc-3.1.1.x86_64.deb https://sfc-repo.snowflakecomputing.com/odbc/linux/3.1.1/snowflake-odbc-3.1.1.x86_64.deb
RUN sudo dpkg -i snowflake-odbc-3.1.1.x86_64.deb

RUN install2.r here log4r \
&& rm -rf /tmp/download_packages/ /tmp/*.rds

RUN echo "EUNOMIA_DATA_FOLDER=/opt/eunomia_data" >> /usr/local/lib/R/etc/Renviron
RUN R -e 'CDMConnector::download_eunomia_data()'

RUN mkdir /results
WORKDIR /code

CMD bash

# Ubuntu 22.04
