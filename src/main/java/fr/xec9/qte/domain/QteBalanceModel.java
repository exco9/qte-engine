package fr.xec9.qte.domain;

public final class QteBalanceModel {
    public static final double SUCCESS_HALF_WIDTH = 0.065;
    private static final double ROTATIONS = 1.25;

    private QteBalanceModel() {}

    public static double targetPhase(long sessionSeed) {
        return 0.18 + unit(mix(sessionSeed ^ 0x6A09E667F3BCC909L)) * 0.64;
    }

    public static double needlePhase(double elapsedTicks, int durationTicks) {
        double progress = Math.max(0, elapsedTicks) / (double) Math.max(1, durationTicks);
        return wrap(progress * ROTATIONS);
    }

    public static double angularDistance(double first, double second) {
        double distance = Math.abs(wrap(first) - wrap(second));
        return Math.min(distance, 1 - distance);
    }

    private static double wrap(double value) {
        double wrapped = value % 1.0;
        return wrapped < 0 ? wrapped + 1 : wrapped;
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static double unit(long value) {
        return (value >>> 11) * 0x1.0p-53;
    }
}
