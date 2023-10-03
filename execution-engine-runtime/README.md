# Darwin Execution Engine R Runtime Environment


This repository contains the files needed to build the Darwin EU R Execution Engine Docker images.

**The main purpose of this image is to run Darwin EU study code by executing a main.R script, but may also be used to
run
tests and generate a report or test database connectivity.**

A pre-built image of the current Dockerfile can be obtained
with `docker pull executionengine.azurecr.io/darwin-runtime:latest`

Changes committed to the main branch will result in both a new 'latest' version and a tagged version becoming
available in our Azure container registry. A consistent versioning and update policy is still to be determined

 ---

In the future we may consider creating separate images which allow for the following

1. Run all the packages required for onboarding
2. Launch a shiny app (i.e. view the results of a completed study)
3. Launch RStudio server (for debugging and code development)

Currently under active development by Adam, Ger, and Rowan
