FROM rocker/r-ver:4.2
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
   unixodbc unixodbc-dev libpq-dev \
   && apt-get clean \
   && rm -rf /var/lib/apt/lists/

RUN install2.r --error odbc RPostgres \
   && rm -rf /tmp/downloaded_packages/ /tmp/*.rds


# Install Darwin packages
RUN install2.r \
        CDMConnector \
        IncidencePrevalence \
        PatientProfiles \
        DrugExposureDiagnostics \
        TreatmentPatterns \
        DrugUtilization \
&& rm -rf /tmp/download_packages/ /tmp/*.rds

RUN R -e "remotes::install_github('ohdsi/CirceR')"
RUN R -e "remotes::install_github('ohdsi/Capr')"

RUN echo "DATABASECONNECTOR_JAR_FOLDER=/opt/hades/jdbc_drivers" >> /usr/local/lib/R/etc/Renviron
RUN echo "RENV_PATHS_CELLAR=/opt/renv_cellar" >> /usr/local/lib/R/etc/Renviron

# Copy some global R settings
#COPY Rprofile.R /tmp/Rprofile.R
#UN cat /tmp/Rprofile.R >> /usr/local/lib/R/etc/Rprofile.site

# Install odbc drivers
RUN apt-get -y update && apt-get install -y curl unixodbc unixodbc-dev vim
RUN curl https://cdn.rstudio.com/drivers/7C152C12/installer/rstudio-drivers-2023.05.0.tar --output /opt/rstudio-drivers.tar
RUN tar -xf /opt/rstudio-drivers.tar -C /opt/
RUN mv /opt/rstudio-drivers-2023.05.0 /opt/rstudio-drivers

RUN echo 'export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/opt/rstudio-drivers/sqlserver/bin/lib/' >> /etc/bash.bashrc

# sudo cp /etc/odbcinst.ini /etc/odbcinst.ini.bak
# cat /opt/rstudio-drivers/odbcinst.ini.sample | sudo tee -a /etc/odbcinst.ini
# cat /etc/odbcinst.ini


RUN mkdir /results
WORKDIR /code

CMD bash

