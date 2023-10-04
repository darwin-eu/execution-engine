FROM adamohdsi/darwin-minimal:v0.1
MAINTAINER Adam Black <black@ohdsi.org>

# Install onboarding R packages
RUN R -e "remotes::install_github('ohdsi/Achilles', 'v1.7.2')"
RUN R -e "remotes::install_github('darwin-eu/CdmOnboarding', 'v2.1.0')"
RUN R -e "remotes::install_github('darwin-eu/DashboardExport', 'v1.1.0')"
RUN R -e "remotes::install_github('ohdsi/DataQualityDashboard', 'v2.4.0')"

