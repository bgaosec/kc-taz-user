package com.securiport.keycloak.events;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

/**
 * Event listener that reacts to admin events and records them using the {@link AdminEventWriter}.
 */
public class AdminEventLoggingListenerProvider implements EventListenerProvider {

    private static final Logger LOGGER = Logger.getLogger(AdminEventLoggingListenerProvider.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final AdminEventWriter writer;

    public AdminEventLoggingListenerProvider(AdminEventWriter writer) {
        this.writer = writer;
    }

    @Override
    public void onEvent(Event event) {
        // This listener is focused on admin events only.
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        if (adminEvent == null) {
            return;
        }

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("timestamp=")
                .append(DATE_FORMATTER.format(Instant.ofEpochMilli(adminEvent.getTime()).atOffset(ZoneOffset.UTC)))
                .append(" realm=")
                .append(valueOrUnknown(adminEvent.getRealmId()))
                .append(" operation=")
                .append(adminEvent.getOperationType())
                .append(" resourceType=")
                .append(adminEvent.getResourceType())
                .append(" resourcePath=")
                .append(valueOrUnknown(adminEvent.getResourcePath()));

        if (adminEvent.getAuthDetails() != null) {
            logBuilder.append(" actorRealm=")
                    .append(valueOrUnknown(adminEvent.getAuthDetails().getRealmId()))
                    .append(" actorClient=")
                    .append(valueOrUnknown(adminEvent.getAuthDetails().getClientId()))
                    .append(" actorUser=")
                    .append(valueOrUnknown(adminEvent.getAuthDetails().getUserId()))
                    .append(" actorIpAddress=")
                    .append(valueOrUnknown(adminEvent.getAuthDetails().getIpAddress()));
        }

        if (includeRepresentation && adminEvent.getRepresentation() != null) {
            logBuilder.append(" representation=")
                    .append(adminEvent.getRepresentation().replaceAll("\s+", " "));
        }

        try {
            writer.write(logBuilder.toString());
        } catch (Exception ex) {
            LOGGER.error("Failed to persist admin event log entry", ex);
        }
    }

    @Override
    public void close() {
        // Nothing to close.
    }

    private static String valueOrUnknown(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
