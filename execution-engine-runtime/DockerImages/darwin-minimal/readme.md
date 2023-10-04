# darwin-minimal docker image

This image contains the minimum required environment to run Darwin studies.

It includes R, database drivers (JDBC and ODBC) as well as the Darwin R packages and their dependencies.

**deployment notes**

docker login
docker build -t darwin-minimal:v0.1 .
docker push adamohdsi/darwin-minimal:v0.1 


