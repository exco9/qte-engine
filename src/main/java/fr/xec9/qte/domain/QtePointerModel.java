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

    public static Point target(QteType type, String id, long elapsedTicks, int durationTicks) {
        if (type == QteType.TRACKING) {
            double phase = Math.max(0, elapsedTicks) / (double) Math.max(1, durationTicks) * Math.PI * 3.0;
            return new Point(Math.sin(phase) * 0.62, Math.cos(phase * 0.73) * 0.42);
        }
        int hash = id == null ? 0 : id.hashCode();
        double x = ((hash & 0xFFFF) / 65535.0) * 1.10 - 0.55;
        double y = (((hash >>> 16) & 0xFFFF) / 65535.0) * 0.80 - 0.40;
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

    public record Point(double x, double y) {}
}
