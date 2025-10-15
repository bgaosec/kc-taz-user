package com.example.keycloak.events;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;

/**
 * Factory that wires {@link AdminEventLoggingListenerProvider} into Keycloak.
 */
public class AdminEventLoggingListenerProviderFactory implements EventListenerProviderFactory {

    public static final String PROVIDER_ID = "admin-event-logger";
    private static final Logger LOGGER = Logger.getLogger(AdminEventLoggingListenerProviderFactory.class);

    private Path logFile;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new AdminEventLoggingListenerProvider(new AdminEventWriter(logFile));
    }

    @Override
    public void init(Config.Scope config) {
        String logFileLocation = config.get("logFile");
        if (logFileLocation == null || logFileLocation.isBlank()) {
            String logDir = System.getProperty("jboss.server.log.dir", System.getProperty("quarkus.log.dir", "."));
            logFileLocation = Paths.get(logDir, "keycloak-admin-events.log").toString();
        }

        logFile = Paths.get(logFileLocation);
        LOGGER.infov("Admin event logger initialized with file {0}", logFile.toAbsolutePath());
    }

    @Override
    public void postInit(org.keycloak.models.KeycloakSessionFactory factory) {
        // Nothing to do after initialization.
    }

    @Override
    public void close() {
        // Nothing to close.
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
