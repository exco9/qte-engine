package fr.xec9.qte.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public enum QteType {
    TIMING,
    HOLD,
    MASH,
    INPUT_SEQUENCE,
    ANALOG_PRECISION,
    AIM,
    TRACKING,
    REACTION_CHOICE,
    DIALOGUE_TIMING,
    OBSERVATION,
    MEMORY,
    RHYTHM,
    BALANCE;

    public static QteType parse(String value) {
        String normalized = value == null ? "" : value.trim()
            .toUpperCase(Locale.ROOT)
            .replace('-', '_')
            .replace(' ', '_');
        normalized = switch (normalized) {
            case "REACTION", "ATTENTION" -> "OBSERVATION";
            case "PATTERN", "DIRECTION" -> "INPUT_SEQUENCE";
            default -> normalized;
        };
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException("Unknown QTE type: " + value);
        }
    }

    public static String names() {
        return Arrays.stream(values())
            .map(type -> type.name().toLowerCase(Locale.ROOT))
            .collect(Collectors.joining(", "));
    }
}
