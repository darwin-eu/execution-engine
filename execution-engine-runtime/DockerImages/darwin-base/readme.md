# darwin-base docker image

This image contains the minimum required environment to run Darwin studies.
It includes R, database drivers (JDBC and ODBC) as well as the Darwin R packages and their dependencies.

**deployment notes**

docker build -t executionengine.azurecr.io/darwin-base:v0.2 --build-arg GITHUB_PAT=$GITHUB_PAT  .
docker run -it --rm executionengine.azurecr.io/darwin-base:v0.2
docker login -u <user> -p <password> executionengine.azurecr.io
docker push executionengine.azurecr.io/darwin-base:v0.2
docker pull executionengine.azurecr.io/darwin-base:v0.2


