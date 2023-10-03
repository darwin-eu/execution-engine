DARWIN EU CC Execution Engine Installation
==========================================

**This repo contains everything you need to get the execution engine running locally**

## Requirements

- [Docker](https://docs.docker.com/engine/install/)

## HOW TO RUN

In order to download the docker images you must first authenticate with our Azure Container Registry like so:

`docker login -u anonymous-pull -p xfM4KmU3nYSiivRmXZljfO3dfqJWNlSU2c3A3t4huD+ACRCicjwZ executionengine.azurecr.io`

(These credentials expire on 2023-10-02, we should move the images to a public registry and get rid of this step)

Then:

- Change the default encryption key and database credentials in the compose.yaml file
- Run `docker compose up -d`. This will run it as a daemon, and it will be on when Docker is on, if you don't want that
  remove the `-d`.
- It will take a couple of minutes before the app comes 'alive'.
- The execution engine should now be up and running on http://localhost:4200/
- To turn things off run `docker compose stop`

This set up is practical if you are running execution engine locally or somewhere where you are ok accessing it through
http or ssh.

If you want to run the execution engine and connect via https it is recommended to use something like
nginx as a reverse proxy you can see the proxied folder for a compose file that can do this.

## AUTHENTICATION

By default, authentication is disabled for execution engine you can enable it and connect it to your own OpenID
Connect (OIDC) provider (e.g. Azure Active Directory, Google etc.) by configuring the required endpoints in the
compose.yml.

If you want only authenticated access to the execution but don't have an OpenID Connect provider in your org the authed
folder provides a compose file that will spin up [Keycloak](https://www.keycloak.org/) that you can use for user
management and/or integration with other identity management solutions.

## UPDATING TO THE LATEST VERSION

Run `make do-update` or run the four commands in the Makefile.

## Troubleshooting

## Questions?

Get in touch with the DARWIN EU Coordination Center

