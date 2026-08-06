package fr.aicha.freshsmooth.qte.client;

import fr.aicha.freshsmooth.qte.domain.QteType;
import java.util.Locale;

final class QteHudModel {
    static final int HEIGHT = 84;
    private static final int MIN_WIDTH = 216;
    private static final int MAX_WIDTH = 320;
    private static final int HORIZONTAL_MARGIN = 16;
    private static final int BOTTOM_MARGIN = 52;

    private QteHudModel() {}

    static Layout layout(int guiWidth, int guiHeight) {
        int width = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, guiWidth - HORIZONTAL_MARGIN * 2));
        return new Layout((guiWidth - width) / 2, guiHeight - HEIGHT - BOTTOM_MARGIN, width, HEIGHT);
    }

    static int chatBottom(int guiWidth, int guiHeight) {
        return Math.max(0, layout(guiWidth, guiHeight).y() - 4);
    }

    static Mechanic mechanic(QteType type) {
        return switch (type) {
            case TIMING, DIALOGUE_TIMING -> Mechanic.TIMING;
            case ANALOG_PRECISION, BALANCE -> Mechanic.PRECISION;
            case AIM -> Mechanic.AIM;
            case TRACKING -> Mechanic.TRACKING;
            case INPUT_SEQUENCE, MEMORY, RHYTHM -> Mechanic.SEQUENCE;
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
        return label;
    }

    static double clampProgress(double value) {
        return Math.max(0, Math.min(1, value));
    }

    static Urgency urgency(double remainingFraction) {
        if (remainingFraction <= 0) {
            return Urgency.EXPIRED;
        }
        return remainingFraction <= 0.25 ? Urgency.URGENT : Urgency.NORMAL;
    }

    static Band timingSuccessBand(int width) {
        int center = (int) Math.round(width * 0.70);
        int naturalLeft = (int) Math.round(width * 0.58);
        int naturalRight = (int) Math.round(width * 0.82);
        int bandWidth = Math.min(width, Math.max(8, naturalRight - naturalLeft));
        int left = Math.max(0, Math.min(width - bandWidth, center - bandWidth / 2));
        return new Band(left, left + bandWidth);
    }

    enum Mechanic {
        SINGLE,
        TIMING,
        PRECISION,
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

    record Band(int left, int right) {
        int width() {
            return right - left;
        }

        int center() {
            return left + width() / 2;
        }
    }
}
