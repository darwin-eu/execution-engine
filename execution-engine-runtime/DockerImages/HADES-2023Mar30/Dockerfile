FROM --platform=linux/amd64 rocker/tidyverse:4.2
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

RUN install2.r --error openssl httr xml2 remotes renv \
    && rm -rf /tmp/download_packages/ /tmp/*.rds

RUN echo 'options(repos = c(CRAN = "https://packagemanager.posit.co/cran/2023-11-01"))' >>"${R_HOME}/etc/Rprofile.site"
RUN echo "DATABASECONNECTOR_JAR_FOLDER=/opt/hades/jdbc_drivers" >> /usr/local/lib/R/etc/Renviron
RUN echo "RENV_PATHS_CELLAR=/opt/renv_cellar" >> /usr/local/lib/R/etc/Renviron

WORKDIR /code

COPY renv.lock /code/

# Install Darwin packages
RUN R -e "renv::restore()"

RUN mkdir /results

CMD bash

# Ubuntu 22.04
