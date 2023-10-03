EXECUTION ENGINE WITH AUTHENTICATION
=====================================

Here you can find a compose file to run the execution engine on a dedicated machine and access over HTTP(S) using
Keycloak as an identity provider.

If you already have an Open ID Connect provider in your organization you don't need Keycloak you can configure it
directly in the UI and API configurations.

To get things running follow the same steps as in the README in the root folder to run execution engine on port 80 (
HTTP)

If you want to run on port 443 (HTTPS) you must add your certificate, check the commented out configurations in
compose.yaml and
nginx.conf for a suggestion on how to enable this.

Keycloak comes with a lot of functionality, if you want to use this to integrate with your existing (non-OpenID Connect)
authentication solution it is recommended to let someone who (at least half) knows what they are doing set this up.
If you only want to add some users yourself in Keycloak this is not too hard and should be doable by clicking around the
UI and when necessary consulting the Keycloak docs online.

