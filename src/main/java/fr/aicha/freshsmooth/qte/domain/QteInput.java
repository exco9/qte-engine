package fr.aicha.freshsmooth.qte.domain;

public record QteInput(Kind kind, String key, double value) {
    public enum Kind {
        PRESS,
        RELEASE,
        AXIS
    }

    public static QteInput press(String key) {
        return new QteInput(Kind.PRESS, key, 0);
    }

    public static QteInput release(String key) {
        return new QteInput(Kind.RELEASE, key, 0);
    }

    public static QteInput axis(double distanceFromTarget) {
        return new QteInput(Kind.AXIS, "", distanceFromTarget);
    }
}
