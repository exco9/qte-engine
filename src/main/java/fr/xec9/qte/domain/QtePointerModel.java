package fr.xec9.qte.domain;

public final class QtePointerModel {
    public static final double AIM_RADIUS = 0.18;
    public static final double TRACKING_RADIUS = 0.22;
    private static final double MOVEMENT_SCALE = 0.006;

    private QtePointerModel() {}

    public static Point move(Point current, double deltaX, double deltaY) {
        return move(current, deltaX, deltaY, 0.5, false);
    }

    public static Point move(
        Point current,
        double deltaX,
        double deltaY,
        double sensitivity,
        boolean invertY
    ) {
        if (!Double.isFinite(deltaX) || !Double.isFinite(deltaY)) {
            return current;
        }
        double configured = Double.isFinite(sensitivity) ? clamp01(sensitivity) : 0.5;
        double base = configured * 0.6 + 0.2;
        double multiplier = base * base * base * 8.0;
        return new Point(
            clamp(current.x() + deltaX * MOVEMENT_SCALE * multiplier),
            clamp(current.y() + deltaY * MOVEMENT_SCALE * multiplier * (invertY ? -1 : 1))
        );
    }

    public static Point target(QteType type, long sessionSeed, long elapsedTicks, int durationTicks) {
        return target(type, sessionSeed, elapsedTicks, durationTicks,
            QteDefinition.DEFAULT_TRACKING_SPEED, null, null);
    }

    public static Point target(
        QteType type,
        long sessionSeed,
        long elapsedTicks,
        int durationTicks,
        double trackingSpeed,
        Double aimX,
        Double aimY
    ) {
        if (type == QteType.TRACKING) {
            double seedPhase = unit(mix(sessionSeed ^ 0x9E3779B97F4A7C15L)) * Math.PI * 2;
            double phase = Math.max(0, elapsedTicks) / (double) Math.max(1, durationTicks)
                * Math.PI * 3.0 * trackingSpeed;
            return new Point(
                Math.sin(phase + seedPhase) * 0.90,
                Math.cos(phase * 0.73 + seedPhase * 1.31) * 0.88
            );
        }
        if (type == QteType.AIM && aimX != null && aimY != null) {
            return new Point(aimX, aimY);
        }
        double x = unit(mix(sessionSeed ^ 0x243F6A8885A308D3L)) * 1.84 - 0.92;
        double y = unit(mix(sessionSeed ^ 0x13198A2E03707344L)) * 1.84 - 0.92;
        return new Point(x, y);
    }

    public static double distance(Point first, Point second) {
        return Math.hypot(first.x() - second.x(), first.y() - second.y());
    }

    private static double clamp(double value) {
        return Math.max(-1, Math.min(1, value));
    }

    private static double clamp01(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static double unit(long value) {
        return (value >>> 11) * 0x1.0p-53;
    }

    public record Point(double x, double y) {}
}
