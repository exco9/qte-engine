package fr.xec9.qte.domain;

import java.util.List;
import java.util.Locale;

public final class QteCommandSchema {
    public static final String INPUTS_ARGUMENT = "inputs";
    public static final String SUCCESS_RESULT_ARGUMENT = "success_result";
    public static final String FAILURE_RESULT_ARGUMENT = "failure_result";
    public static final String HIDE_HUD_ARGUMENT = "hide_hud";
    private static final List<String> COMMON_INPUTS = List.of(
        "a", "d", "down", "e", "left", "left_shift", "mouse.left", "mouse.middle", "mouse.right",
        "right", "s", "space", "up", "w", "z"
    );

    private QteCommandSchema() {}

    public static List<String> inputSuggestions(String remaining) {
        String normalized = remaining == null ? "" : remaining.trim().toLowerCase(Locale.ROOT);
        int separator = normalized.lastIndexOf(',');
        String prefix = separator < 0 ? "" : normalized.substring(0, separator + 1);
        String token = normalized.substring(separator + 1).trim();
        return COMMON_INPUTS.stream()
            .filter(candidate -> candidate.startsWith(token))
            .map(candidate -> prefix + candidate)
            .toList();
    }

}
