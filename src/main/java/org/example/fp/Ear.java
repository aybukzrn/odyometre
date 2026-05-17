package org.example.fp;

import java.util.Locale;
import java.util.Optional;

public enum Ear {
    RIGHT,
    LEFT;

    public static Optional<Ear> fromLabel(String label) {
        if (label == null || label.isBlank()) {
            return Optional.empty();
        }

        String normalized = label.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("right") || normalized.equals("sag") || normalized.equals("sağ") || normalized.equals("sağ kulak")) {
            return Optional.of(RIGHT);
        }
        if (normalized.equals("left") || normalized.equals("sol") || normalized.equals("sol kulak")) {
            return Optional.of(LEFT);
        }

        return Optional.empty();
    }
}
