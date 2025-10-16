package com.securiport.keycloak.events;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Helper component that handles writing the admin event log entries to a dedicated log file.
 */
public class AdminEventWriter {

    private final Path logFile;
    private final Object lock = new Object();

    public AdminEventWriter(Path logFile) {
        this.logFile = Objects.requireNonNull(logFile, "logFile");
    }

    public void write(String message) throws IOException {
        Objects.requireNonNull(message, "message");
        synchronized (lock) {
            if (logFile.getParent() != null) {
                Files.createDirectories(logFile.getParent());
            }
            Files.writeString(
                    logFile,
                    message + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        }
    }

    public Path getLogFile() {
        return logFile;
    }
}
