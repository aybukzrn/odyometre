package org.example.fp;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class ResponseParser {
    private ResponseParser() {
    }

    public static Optional<PatientResponse> parse(String message) {
        if (message == null || message.isBlank()) {
            return Optional.empty();
        }

        String normalized = message.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "RESPONSE", "HEARD", "YES" -> Optional.of(PatientResponse.HEARD);
            case "NO_RESPONSE", "NOT_HEARD", "NO" -> Optional.of(PatientResponse.NOT_HEARD);
            default -> Optional.empty();
        };
    }

    public static List<PatientResponse> parseValidResponses(Collection<String> messages) {
        return messages.stream()
                .map(ResponseParser::parse)
                .flatMap(Optional::stream)
                .toList();
    }
}
