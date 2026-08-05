package fr.aicha.freshsmooth.qte.domain;

import java.util.Arrays;
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
    boolean exclusiveInput,
    String texture
) {
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern RESOURCE = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    public QteDefinition {
        if (id == null || !ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid QTE id: " + id);
        }
        Objects.requireNonNull(type, "type");
        keys = List.copyOf(keys);
        if (keys.isEmpty()) {
            throw new IllegalArgumentException("Key must contain at least one input");
        }
        if (durationTicks < 2 || durationTicks > 6_000) {
            throw new IllegalArgumentException("Duration must be between 0.1 and 300 seconds");
        }
        if (resultCommand == null || resultCommand.isBlank()) {
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
        return create(id, type, key, durationSeconds, resultCommand, false, texture);
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
        if (!Double.isFinite(durationSeconds) || durationSeconds < 0.1 || durationSeconds > 300) {
            throw new IllegalArgumentException("Duration must be between 0.1 and 300 seconds");
        }
        List<String> inputs = Arrays.stream(key == null ? new String[0] : key.split(","))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .map(QteDefinition::normalizeInput)
            .toList();
        String command = resultCommand == null ? null : resultCommand.trim();
        if (command != null && command.startsWith("/")) {
            command = command.substring(1).trim();
        }
        String normalizedTexture = texture == null || texture.isBlank()
            ? null
            : texture.trim().toLowerCase(Locale.ROOT);
        return new QteDefinition(
            id == null ? null : id.trim().toLowerCase(Locale.ROOT),
            type,
            inputs,
            (int) Math.round(durationSeconds * 20.0),
            command,
            exclusiveInput,
            normalizedTexture
        );
    }

    public double durationSeconds() {
        return durationTicks / 20.0;
    }

    private static String normalizeInput(String raw) {
        String value = raw.toLowerCase(Locale.ROOT);
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
