package fr.xec9.qte.domain;

import java.util.regex.Pattern;

public final class QteKeyMigration {
    private static final Pattern LEGACY_LETTER = Pattern.compile("key\\.keyboard\\.([a-z])");

    private QteKeyMigration() {}

    public static String migrate(String key, boolean legacyFormat) {
        if (!legacyFormat) {
            return key;
        }
        var matcher = LEGACY_LETTER.matcher(key);
        return matcher.matches() ? "key.localized." + matcher.group(1) : key;
    }
}
