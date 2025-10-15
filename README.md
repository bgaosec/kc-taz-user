# Keycloak Admin Event Logger

This project provides a custom [Keycloak Event Listener SPI](https://www.keycloak.org/docs/latest/server_development/#_events) that records every admin event into a dedicated log file. The listener focuses exclusively on admin operations (such as realm configuration changes, client updates, or user management actions) and ignores end-user events.

## Features

* Writes a structured, single-line entry for every admin event processed by Keycloak.
* Creates the target log file and parent directories on demand.
* Configurable log file location via Keycloak SPI configuration.

## Building the provider

```bash
mvn clean package
```

This produces `target/admin-event-logger-1.0.0-SNAPSHOT.jar`, which must be copied into the Keycloak providers directory (for the Quarkus distribution this is `providers/` under the Keycloak installation directory).

## Configuring Keycloak

Enable the provider by adding the SPI configuration to your Keycloak installation (for example in `conf/keycloak.conf`):

```
spi-events-listener-admin-event-logger-enabled=true
spi-events-listener-admin-event-logger-log-file=/opt/keycloak/data/log/keycloak-admin-events.log
```

The `log-file` property is optional; if it is not provided the listener defaults to `${jboss.server.log.dir}/keycloak-admin-events.log` (falling back to the current directory when the property is absent).

After updating the configuration, restart Keycloak. Every admin event will now be appended to the chosen log file.
