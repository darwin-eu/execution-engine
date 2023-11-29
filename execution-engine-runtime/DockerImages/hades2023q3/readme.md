# Hades March 30, 2023 release

This image is built using the Hades-wide release lockfiles found [here](https://github.com/OHDSI/Hades/blob/69f0db8a49d3c90ce297059de6cb0e9381130ff3/hadesWideReleases/2023Mar30/renv.lock#L1).


**deployment notes**

docker build -t executionengine.azurecr.io/hades:2023q3 .
docker run -it --rm executionengine.azurecr.io/hades:2023q3
docker login -u <user> -p <password> executionengine.azurecr.io
docker push executionengine.azurecr.io/darwin-minimal:v0.2


docker build -t executionengine.azurecr.io/hades:2023Q3 .
