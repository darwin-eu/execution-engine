FROM --platform=linux/amd64 rocker/rstudio:4.2
MAINTAINER Adam Black <a.black@darwin-eu.org>


# Install java and rJava
RUN apt-get -y update && apt-get install -y \
   default-jdk \
   r-cran-rjava \
   sudo \
   && apt-get clean \
   && rm -rf /var/lib/apt/lists/ \
   && sudo R CMD javareconf

RUN echo 'options(repos = c(CRAN = "https://packagemanager.posit.co/cran/__linux__/jammy/2024-01-31"))' >>"${R_HOME}/etc/Rprofile.site"
RUN install2.r --error rJava && rm -rf /tmp/download_packages/ /tmp/*.rds
RUN install2.r --error DatabaseConnector && rm -rf /tmp/download_packages/ /tmp/*.rds
ENV DATABASECONNECTOR_JAR_FOLDER="/opt/hades/jdbc_drivers"
RUN R -e "DatabaseConnector::downloadJdbcDrivers('all');"

RUN install2.r --error Andromeda && rm -rf /tmp/download_packages/ /tmp/*.rds

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

# Install Darwin packages
RUN install2.r --error \
        omopgenerics \
        CDMConnector \
        IncidencePrevalence \
        PatientProfiles \
        TreatmentPatterns \
        DrugExposureDiagnostics \
        DrugUtilisation \
&& rm -rf /tmp/download_packages/ /tmp/*.rds

# install OHDSI HADES R packages from CRAN and GitHub, temporarily adding a GitHub Personal Access Token (PAT) to the Renviron file
# To build this container image, you will need to create a file called GITHUBPAT.txt and populate it with a GitHub Personal Access Token.
RUN --mount=type=secret,id=build_github_pat \
	echo "GITHUB_PAT=$(cat /run/secrets/build_github_pat)" >> /usr/local/lib/R/etc/Renviron \
        && R -e "remotes::install_github('ohdsi/CirceR')" \
        && R -e "remotes::install_github('ohdsi/Capr')" \
        && cp /tmp/Renviron /usr/local/lib/R/etc/Renviron

RUN echo "DATABASECONNECTOR_JAR_FOLDER=/opt/hades/jdbc_drivers" >> /usr/local/lib/R/etc/Renviron
RUN echo "RENV_PATHS_CELLAR=/opt/renv_cellar" >> /usr/local/lib/R/etc/Renviron

# SQL Server odbc
RUN curl https://packages.microsoft.com/keys/microsoft.asc | sudo tee /etc/apt/trusted.gpg.d/microsoft.asc
RUN curl https://packages.microsoft.com/config/ubuntu/22.04/prod.list | sudo tee /etc/apt/sources.list.d/mssql-release.list
RUN apt clean && apt-get update && ACCEPT_EULA=Y apt-get install -y msodbcsql17

# Snowflake odbc
RUN curl --output snowflake-odbc-3.1.1.x86_64.deb https://sfc-repo.snowflakecomputing.com/odbc/linux/3.1.1/snowflake-odbc-3.1.1.x86_64.deb
RUN sudo dpkg -i snowflake-odbc-3.1.1.x86_64.deb

RUN install2.r --error here log4r testthat \
&& rm -rf /tmp/download_packages/ /tmp/*.rds

RUN echo "EUNOMIA_DATA_FOLDER=/opt/eunomia_data" >> /usr/local/lib/R/etc/Renviron
RUN R -e 'CDMConnector::download_eunomia_data()'

# Install vim
RUN apt-get -y update && apt-get install -y vim && apt-get clean && rm -rf /var/lib/apt/lists/

# need to replace one line in the snowflake.ini file
RUN sed -i 's/libodbcinst.so.1/libodbcinst.so.2/g' /usr/lib/snowflake/odbc/lib/simba.snowflake.ini

# Install onboarding R packages

#RUN R -e "remotes::install_github('ohdsi/Achilles', 'v1.7.2')"
RUN R -e "remotes::install_version('Achilles', version = '1.7.2', repos = 'https://repository-dev.darwin-eu.org/ohdsi/latest')"
# RUN R -e "remotes::install_github('darwin-eu/CdmOnboarding', 'v2.1.0')"
# RUN R -e "remotes::install_github('darwin-eu/DashboardExport', 'v1.1.0')"
# RUN R -e "remotes::install_github('ohdsi/DataQualityDashboard', 'v2.4.0')"
# RUN R -e "remotes::install_github('darwin-eu/CohortDiagnostics')"

# disable rstudio login by default
ARG DISABLE_AUTH=true

RUN mkdir /results
WORKDIR /code

CMD bash

# Ubuntu 22.04 (Jammy)
