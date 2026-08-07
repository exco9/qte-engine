package fr.aicha.freshsmooth.qte.domain;

public record QteInput(Kind kind, String key, double value, double secondaryValue) {
    public enum Kind {
        PRESS,
        RELEASE,
        AXIS,
        POINTER
    }

    public static QteInput press(String key) {
        return new QteInput(Kind.PRESS, key, 0, 0);
    }

    public static QteInput release(String key) {
        return new QteInput(Kind.RELEASE, key, 0, 0);
    }

    public static QteInput axis(double distanceFromTarget) {
        return new QteInput(Kind.AXIS, "", distanceFromTarget, 0);
    }

    public static QteInput pointer(double x, double y) {
        return new QteInput(Kind.POINTER, "", x, y);
    }
}
