package fr.aicha.freshsmooth.qte.domain;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record QteDefinition(
    String id,
    QteType type,
    List<String> keys,
    int durationTicks,
    String resultCommand,
    String failureCommand,
    boolean exclusiveInput,
    boolean hideHud,
    String texture
) {
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern RESOURCE = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    private static final EnumSet<QteType> MULTI_INPUT = EnumSet.of(
        QteType.INPUT_SEQUENCE,
        QteType.REACTION_CHOICE,
        QteType.MEMORY,
        QteType.RHYTHM
    );

    public QteDefinition {
        if (id == null || !ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid QTE id: " + id);
        }
        Objects.requireNonNull(type, "type");
        keys = List.copyOf(keys);
        if (keys.isEmpty()) {
            throw new IllegalArgumentException("Key must contain at least one input");
        }
        if (MULTI_INPUT.contains(type) && keys.size() < 2) {
            throw new IllegalArgumentException(type.name().toLowerCase(Locale.ROOT) + " requires at least two inputs");
        }
        if (!MULTI_INPUT.contains(type) && keys.size() > 1) {
            throw new IllegalArgumentException(type.name().toLowerCase(Locale.ROOT) + " accepts exactly one input");
        }
        if (durationTicks < 2 || durationTicks > 6_000) {
            throw new IllegalArgumentException("Duration must be between 0.1 and 300 seconds");
        }
        resultCommand = normalizeCommand(resultCommand);
        failureCommand = normalizeCommand(failureCommand);
        if (resultCommand == null) {
            throw new IllegalArgumentException("Result command cannot be blank");
        }
        if (texture != null && !RESOURCE.matcher(texture).matches()) {
            throw new IllegalArgumentException("Invalid texture resource: " + texture);
        }
    }

    public static QteDefinition create(
        String id,
        QteType type,
        String key,
        double durationSeconds,
        String resultCommand,
        String texture
    ) {
        return create(id, type, key, durationSeconds, resultCommand, null, false, false, texture);
    }

    public static QteDefinition create(
        String id,
        QteType type,
        String key,
        double durationSeconds,
        String resultCommand,
        boolean exclusiveInput,
        String texture
    ) {
        return create(id, type, key, durationSeconds, resultCommand, null, exclusiveInput, false, texture);
    }

    public static QteDefinition create(
        String id,
        QteType type,
        String key,
        double durationSeconds,
        String resultCommand,
        String failureCommand,
        boolean exclusiveInput,
        boolean hideHud,
        String texture
    ) {
        if (!Double.isFinite(durationSeconds) || durationSeconds < 0.1 || durationSeconds > 300) {
            throw new IllegalArgumentException("Duration must be between 0.1 and 300 seconds");
        }
        List<String> inputs = Arrays.stream(key == null ? new String[0] : key.split(","))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .map(QteDefinition::normalizeInput)
            .toList();
        String normalizedTexture = texture == null || texture.isBlank()
            ? null
            : texture.trim().toLowerCase(Locale.ROOT);
        return new QteDefinition(
            id == null ? null : id.trim().toLowerCase(Locale.ROOT),
            type,
            inputs,
            (int) Math.round(durationSeconds * 20.0),
            resultCommand,
            failureCommand,
            exclusiveInput,
            hideHud,
            normalizedTexture
        );
    }

    public double durationSeconds() {
        return durationTicks / 20.0;
    }

    private static String normalizeCommand(String command) {
        if (command == null || command.isBlank()) {
            return null;
        }
        String normalized = command.trim();
        return normalized.startsWith("/") ? normalized.substring(1).trim() : normalized;
    }

    private static String normalizeInput(String raw) {
        String value = raw.toLowerCase(Locale.ROOT);
        value = switch (value) {
            case "m1", "mouse1", "mouse_1", "left_click" -> "mouse.left";
            case "m2", "mouse2", "mouse_2", "right_click" -> "mouse.right";
            case "m3", "mouse3", "mouse_3", "middle_click" -> "mouse.middle";
            default -> value;
        };
        if (value.startsWith("key.")) {
            return value;
        }
        if (value.startsWith("mouse.")) {
            return "key." + value;
        }
        if (value.matches("[a-z]")) {
            return "key.localized." + value;
        }
        if (value.length() == 1 || value.matches("[a-z0-9_]+")) {
            return "key.keyboard." + value;
        }
        throw new IllegalArgumentException("Invalid input: " + raw);
    }
}
