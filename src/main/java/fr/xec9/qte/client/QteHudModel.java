package fr.xec9.qte.client;

import fr.xec9.qte.domain.QteType;
import java.util.Locale;

final class QteHudModel {
    static final int PROMPT_SIZE = 68;
    static final int KEY_SIZE = 32;
    static final int BALANCE_KEY_SIZE = KEY_SIZE;
    static final int ACCENT_COLOR = 0xFFF1F4F4;
    static final int TRACK_COLOR = 0xFF555555;
    static final int SUCCESS_INDICATOR_COLOR = 0xFF000000;
    static final int SUCCESS_PROGRESS_INNER_RADIUS = 20;
    static final int SUCCESS_PROGRESS_OUTER_RADIUS = 22;
    static final int SEQUENCE_SUCCESS_INNER_RADIUS = 13;
    static final int SEQUENCE_SUCCESS_OUTER_RADIUS = 14;
    static final int SEQUENCE_DURATION_INNER_RADIUS = 15;
    static final int SEQUENCE_DURATION_OUTER_RADIUS = 17;
    static final int BALANCE_DURATION_INNER_RADIUS = 29;
    static final int BALANCE_DURATION_OUTER_RADIUS = 31;
    static final int BALANCE_TRACK_COLOR = 0xFF555555;
    static final int BALANCE_TARGET_COLOR = 0xFF70E08C;
    static final int BALANCE_TRACK_INNER_RADIUS = 24;
    static final int BALANCE_TRACK_OUTER_RADIUS = 26;
    static final int BALANCE_TARGET_INNER_RADIUS = 23;
    static final int BALANCE_TARGET_OUTER_RADIUS = 27;
    static final int RING_OUTER_RADIUS = 27;
    static final int RING_INNER_RADIUS = 25;
    private static final int BOTTOM_MARGIN = 48;
    private static final double ENTRY_DURATION_TICKS = 2.5;
    private static final int FEEDBACK_DURATION_TICKS = 8;

    private QteHudModel() {}

    static Layout layout(int guiWidth, int guiHeight) {
        int x = Math.max(0, (guiWidth - PROMPT_SIZE) / 2);
        int y = Math.max(0, guiHeight - PROMPT_SIZE - BOTTOM_MARGIN);
        return new Layout(x, y, PROMPT_SIZE, PROMPT_SIZE);
    }

    static int chatBottom(int guiWidth, int guiHeight) {
        return Math.max(0, layout(guiWidth, guiHeight).y() - 4);
    }

    static double remainingFraction(long elapsedTicks, int durationTicks, float partialTick) {
        if (durationTicks <= 0) {
            return 0;
        }
        return clampProgress((durationTicks - (elapsedTicks + Math.max(0, partialTick))) / durationTicks);
    }

    static double entryProgress(long elapsedTicks, float partialTick) {
        double linear = clampProgress((elapsedTicks + Math.max(0, partialTick)) / ENTRY_DURATION_TICKS);
        return easeOutCubic(linear);
    }

    static double easeOutCubic(double progress) {
        double clamped = clampProgress(progress);
        double inverse = 1 - clamped;
        return 1 - inverse * inverse * inverse;
    }

    static double entryScale(double easedProgress) {
        return 0.85 + 0.15 * clampProgress(easedProgress);
    }

    static int alpha(double progress, int baseAlpha) {
        return (int) Math.round(Math.max(0, Math.min(255, baseAlpha)) * clampProgress(progress));
    }

    static int countdownAlpha(int alpha) {
        return (int) Math.round(Math.max(0, Math.min(255, alpha)) * 190.0 / 255.0);
    }

    static int successIndicatorAlpha(int alpha) {
        return (int) Math.round(Math.max(0, Math.min(255, alpha)) * 0.35);
    }

    static double successScale(double feedbackProgress) {
        return 1 + 0.10 * (1 - easeOutCubic(feedbackProgress));
    }

    static int feedbackAlpha(int lingerTicks) {
        return alpha(1 - (double) Math.max(0, lingerTicks) / FEEDBACK_DURATION_TICKS, 255);
    }

    static int failureShake(int lingerTicks) {
        if (lingerTicks <= 0 || lingerTicks >= FEEDBACK_DURATION_TICKS) {
            return 0;
        }
        int amplitude = (int) Math.round(2 * (1 - (double) lingerTicks / FEEDBACK_DURATION_TICKS));
        return (lingerTicks & 2) == 0 ? amplitude : -amplitude;
    }

    static Mechanic mechanic(QteType type) {
        return switch (type) {
            case BALANCE -> Mechanic.BALANCE;
            case AIM -> Mechanic.AIM;
            case TRACKING -> Mechanic.TRACKING;
            case INPUT_SEQUENCE, REACTION_CHOICE -> Mechanic.SEQUENCE;
            case HOLD -> Mechanic.HOLD;
            case MASH -> Mechanic.MASH;
            default -> Mechanic.SINGLE;
        };
    }

    static String keyLabel(String key) {
        String label = key
            .replace("key.keyboard.", "")
            .replace("key.localized.", "")
            .replace("key.mouse.", "")
            .toUpperCase(Locale.ROOT);
        if (key.startsWith("key.mouse.")) {
            return switch (label) {
                case "LEFT" -> "M1";
                case "RIGHT" -> "M2";
                case "MIDDLE" -> "M3";
                default -> "BUTTON " + label;
            };
        }
        return switch (label) {
            case "LEFT_SHIFT", "RIGHT_SHIFT" -> "SHIFT";
            case "LEFT_CONTROL", "RIGHT_CONTROL" -> "CTRL";
            case "LEFT_ALT", "RIGHT_ALT" -> "ALT";
            default -> label;
        };
    }

    static double clampProgress(double value) {
        return Math.max(0, Math.min(1, value));
    }

    static ScreenPoint screenPoint(double normalizedX, double normalizedY, int width, int height, int margin) {
        int safeMargin = Math.max(0, Math.min(margin, Math.min(width, height) / 2));
        double x = clampSigned(normalizedX);
        double y = clampSigned(normalizedY);
        return new ScreenPoint(
            safeMargin + (int) Math.round((x + 1) * 0.5 * Math.max(0, width - safeMargin * 2)),
            safeMargin + (int) Math.round((y + 1) * 0.5 * Math.max(0, height - safeMargin * 2))
        );
    }

    static int keyLabelXOffset() {
        return 1;
    }

    static int keyLabelYOffset(boolean pressed) {
        return pressed ? 0 : -2;
    }

    static float keyLabelScale(int textWidth, int keySize) {
        int maxTextWidth = Math.max(8, keySize - 9);
        float scaleLimit = keySize < KEY_SIZE ? 1.20F : 1.35F;
        return Math.min(scaleLimit, (float) maxTextWidth / Math.max(1, textWidth));
    }

    static String mousePromptSprite(String label) {
        return switch (label) {
            case "M1" -> "qte_mouse_left";
            case "M2" -> "qte_mouse_right";
            case "M3" -> "qte_mouse_mb3";
            default -> null;
        };
    }

    static int mouseBlinkAlpha(long elapsedMillis, boolean pressed) {
        if (pressed) {
            return 255;
        }
        long phaseMillis = Math.floorMod(elapsedMillis, 2_000L);
        double phase = phaseMillis / 2_000.0;
        return (int) Math.round((1 - Math.cos(phase * Math.PI * 2)) * 127.5 + 1.0E-9);
    }

    static int ringSegments(double fraction) {
        double clamped = clampProgress(fraction);
        return clamped <= 0 ? 0 : Math.max(1, (int) Math.ceil(72 * clamped));
    }

    private static double clampSigned(double value) {
        if (!Double.isFinite(value)) {
            return 0;
        }
        return Math.max(-1, Math.min(1, value));
    }

    static Urgency urgency(double remainingFraction) {
        if (remainingFraction <= 0) {
            return Urgency.EXPIRED;
        }
        return remainingFraction <= 0.25 ? Urgency.URGENT : Urgency.NORMAL;
    }

    enum Mechanic {
        SINGLE,
        BALANCE,
        AIM,
        TRACKING,
        SEQUENCE,
        HOLD,
        MASH
    }

    enum Urgency {
        NORMAL,
        URGENT,
        EXPIRED
    }

    record Layout(int x, int y, int width, int height) {}

    record ScreenPoint(int x, int y) {}

}
