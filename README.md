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
spi-events-listener-admin-event-logger-log-file=/opt/keycloak/data/logs/keycloak-admin-events.log
```

The `log-file` property is optional; if it is not provided the listener defaults to `${jboss.server.log.dir}/keycloak-admin-events.log` (falling back to the current directory when the property is absent).

After updating the configuration, restart Keycloak. Every admin event will now be appended to the chosen log file.

## Deploying with Docker Compose

To use the listener in a containerised Keycloak deployment:

1. **Build the JAR**  
   Run `mvn clean package` so that `target/admin-event-logger-1.0.0-SNAPSHOT.jar` is available on the host.

2. **Expose the provider and configuration to the container**  
   Create a directory structure such as:
   ```
   keycloak/
     ├── providers/
     │   └── admin-event-logger-1.0.0-SNAPSHOT.jar
     └── conf/
         └── keycloak.conf
   ```
   The `keycloak.conf` file should include the SPI settings shown above and optionally any other server configuration you need. The log file will be created in `/opt/keycloak/data/logs`, which is part of the standard data directory—make sure this directory is backed by a bind mount so the log is persisted on the host.

3. **Wire everything in via `docker-compose.yml`**  
   Mount the provider directory and configuration file, and enable the listener via environment variables (or command-line arguments):
   ```yaml
   services:
     keycloak:
       image: quay.io/keycloak/keycloak:24.0.2
       command:
         - start
         - --optimized
       environment:
         KEYCLOAK_ADMIN: admin
         KEYCLOAK_ADMIN_PASSWORD: admin
         KC_EVENTS_LISTENERS: "jboss-logging,admin-event-logger"
         KC_SPI_EVENTS_LISTENER_ADMIN_EVENT_LOGGER_ENABLED: "true"
         KC_SPI_EVENTS_LISTENER_ADMIN_EVENT_LOGGER_LOG_FILE: "/opt/keycloak/data/logs/keycloak-admin-events.log"
       volumes:
         - ./keycloak/providers:/opt/keycloak/providers:ro
         - ./keycloak/conf/keycloak.conf:/opt/keycloak/conf/keycloak.conf:ro
         - ./keycloak/data:/opt/keycloak/data
       ports:
         - "8080:8080"
   ```
   Instead of mounting the JAR you can bake it into a derived image with `FROM quay.io/keycloak/keycloak:24.0.2` and `COPY target/*.jar /opt/keycloak/providers/`.

4. **Enable admin events per realm**  
   Inside the Keycloak admin console, open the target realm → *Events* → *Admin Events* and check **Save Events** so that admin events are emitted to listeners.

5. **Restart the stack**  
   Run `docker compose up -d --build` (or the command you normally use). The listener writes to `/opt/keycloak/data/logs/keycloak-admin-events.log`, which on the host corresponds to `./keycloak/data/logs/keycloak-admin-events.log`.

### Troubleshooting permissions

Keycloak runs as user ID `1000` inside the official container. If your host bind mount is owned by `root`, the server will fail with `AccessDeniedException` when it tries to create `/opt/keycloak/data/logs`. Before starting the stack, ensure the mounted directory is writable:

```bash
mkdir -p keycloak/data/logs
sudo chown -R 1000:1000 keycloak/data
```

On SELinux-enabled hosts, add the `:z` or `:Z` flag to the `volumes` entry (for example `./keycloak/data:/opt/keycloak/data:Z`) so the container receives the correct context.
