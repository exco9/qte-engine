package fr.aicha.freshsmooth.qte.domain;

import java.util.EnumSet;
import java.util.Set;

public final class QteJudge {
    private static final Set<QteType> SINGLE = EnumSet.of(
        QteType.REACTION_CHOICE,
        QteType.OBSERVATION
    );
    private static final Set<QteType> TIMED = EnumSet.of(QteType.TIMING, QteType.DIALOGUE_TIMING);
    private static final Set<QteType> SEQUENCE = EnumSet.of(
        QteType.INPUT_SEQUENCE,
        QteType.MEMORY
    );
    private static final Set<QteType> PRECISION = EnumSet.of(
        QteType.ANALOG_PRECISION,
        QteType.BALANCE
    );

    private final QteDefinition definition;
    private QteStatus status = QteStatus.ACTIVE;
    private int index;
    private int presses;
    private long heldSince = -1;
    private long elapsedTicks;
    private double axisDistance = 1;
    private QtePointerModel.Point pointer = new QtePointerModel.Point(0, 0);
    private boolean trackingHeld;
    private int trackingTicks;
    private long lastTrackingTick = -1;

    public QteJudge(QteDefinition definition) {
        this.definition = definition;
    }

    public QteStatus accept(QteInput input, long elapsedTicks) {
        if (input.kind() == QteInput.Kind.POINTER) {
            pointer = new QtePointerModel.Point(clampSigned(input.value()), clampSigned(input.secondaryValue()));
        }
        tick(elapsedTicks);
        if (status.terminal()) {
            return status;
        }

        switch (strategy(definition.type())) {
            case SINGLE -> acceptSingle(input);
            case TIMING -> acceptTimed(input);
            case SEQUENCE -> acceptSequence(input);
            case PRECISION -> acceptPrecision(input);
            case AIM -> acceptAim(input);
            case TRACKING -> acceptTracking(input);
            case HOLD -> acceptHold(input);
            case MASH -> acceptMash(input);
            case RHYTHM -> acceptRhythm(input);
        }
        return status;
    }

    public QteStatus tick(long elapsedTicks) {
        if (status.terminal()) {
            return status;
        }
        this.elapsedTicks = Math.max(0, elapsedTicks);
        if (definition.type() == QteType.TRACKING && this.elapsedTicks != lastTrackingTick) {
            lastTrackingTick = this.elapsedTicks;
            QtePointerModel.Point target = QtePointerModel.target(
                QteType.TRACKING,
                definition.id(),
                this.elapsedTicks,
                definition.durationTicks()
            );
            if (trackingHeld && QtePointerModel.distance(pointer, target) <= QtePointerModel.TRACKING_RADIUS) {
                trackingTicks++;
                if (trackingTicks >= trackingTargetTicks()) {
                    status = QteStatus.SUCCESS;
                }
            }
        }
        if (definition.type() == QteType.HOLD && heldSince >= 0
            && this.elapsedTicks - heldSince >= holdTarget()) {
            status = QteStatus.SUCCESS;
        } else if (this.elapsedTicks > definition.durationTicks()) {
            status = QteStatus.TIMEOUT;
        }
        return status;
    }

    public QteStatus status() {
        return status;
    }

    public double progress() {
        if (status == QteStatus.SUCCESS) {
            return 1;
        }
        QteType type = definition.type();
        if (SEQUENCE.contains(type) || type == QteType.RHYTHM) {
            return clamp((double) index / definition.keys().size());
        }
        if (type == QteType.MASH) {
            return clamp((double) presses / mashTarget());
        }
        if (type == QteType.HOLD) {
            return heldSince < 0 ? 0 : clamp((double) (elapsedTicks - heldSince) / holdTarget());
        }
        if (PRECISION.contains(type)) {
            return clamp(1 - Math.abs(axisDistance));
        }
        if (type == QteType.AIM) {
            return clamp(1 - pointerDistance() / 1.5);
        }
        if (type == QteType.TRACKING) {
            return clamp((double) trackingTicks / trackingTargetTicks());
        }
        return clamp((double) elapsedTicks / definition.durationTicks());
    }

    public int currentIndex() {
        return index;
    }

    public int mashTarget() {
        return Math.max(5, (int) Math.round(definition.durationSeconds() * 4));
    }

    public int trackingTargetTicks() {
        return Math.max(4, (int) Math.ceil(definition.durationTicks() * 0.45));
    }

    public int trackingTicks() {
        return trackingTicks;
    }

    public static Strategy strategy(QteType type) {
        return switch (type) {
            case REACTION_CHOICE, OBSERVATION -> Strategy.SINGLE;
            case TIMING, DIALOGUE_TIMING -> Strategy.TIMING;
            case INPUT_SEQUENCE, MEMORY -> Strategy.SEQUENCE;
            case ANALOG_PRECISION, BALANCE -> Strategy.PRECISION;
            case AIM -> Strategy.AIM;
            case TRACKING -> Strategy.TRACKING;
            case HOLD -> Strategy.HOLD;
            case MASH -> Strategy.MASH;
            case RHYTHM -> Strategy.RHYTHM;
        };
    }

    public double pointerDistance() {
        QtePointerModel.Point target = QtePointerModel.target(
            definition.type(),
            definition.id(),
            elapsedTicks,
            definition.durationTicks()
        );
        return QtePointerModel.distance(pointer, target);
    }

    private void acceptSingle(QteInput input) {
        if (input.kind() == QteInput.Kind.PRESS) {
            status = expectedKey().equals(input.key()) ? QteStatus.SUCCESS : QteStatus.FAILURE;
        }
    }

    private void acceptTimed(QteInput input) {
        if (input.kind() != QteInput.Kind.PRESS) {
            return;
        }
        double normalizedTime = (double) elapsedTicks / definition.durationTicks();
        status = expectedKey().equals(input.key()) && Math.abs(normalizedTime - 0.70) <= 0.12
            ? QteStatus.SUCCESS
            : QteStatus.FAILURE;
    }

    private void acceptSequence(QteInput input) {
        if (input.kind() != QteInput.Kind.PRESS) {
            return;
        }
        if (!definition.keys().get(index).equals(input.key())) {
            status = QteStatus.FAILURE;
            return;
        }
        index++;
        if (index == definition.keys().size()) {
            status = QteStatus.SUCCESS;
        }
    }

    private void acceptPrecision(QteInput input) {
        if (input.kind() != QteInput.Kind.AXIS) {
            return;
        }
        axisDistance = Math.abs(input.value());
        if (axisDistance <= 0.15) {
            status = QteStatus.SUCCESS;
        }
    }

    private void acceptAim(QteInput input) {
        if (input.kind() != QteInput.Kind.PRESS) {
            return;
        }
        status = expectedKey().equals(input.key()) && pointerDistance() <= QtePointerModel.AIM_RADIUS
            ? QteStatus.SUCCESS
            : QteStatus.FAILURE;
    }

    private void acceptTracking(QteInput input) {
        if (input.kind() == QteInput.Kind.PRESS) {
            if (!expectedKey().equals(input.key())) {
                status = QteStatus.FAILURE;
            } else {
                trackingHeld = true;
            }
        } else if (input.kind() == QteInput.Kind.RELEASE && expectedKey().equals(input.key())) {
            trackingHeld = false;
        }
    }

    private void acceptHold(QteInput input) {
        if (!expectedKey().equals(input.key())) {
            if (input.kind() == QteInput.Kind.PRESS) {
                status = QteStatus.FAILURE;
            }
            return;
        }
        if (input.kind() == QteInput.Kind.PRESS && heldSince < 0) {
            heldSince = elapsedTicks;
        } else if (input.kind() == QteInput.Kind.RELEASE && heldSince >= 0) {
            status = elapsedTicks - heldSince >= holdTarget() ? QteStatus.SUCCESS : QteStatus.FAILURE;
        }
    }

    private void acceptMash(QteInput input) {
        if (input.kind() != QteInput.Kind.PRESS) {
            return;
        }
        if (!expectedKey().equals(input.key())) {
            status = QteStatus.FAILURE;
            return;
        }
        presses++;
        if (presses >= mashTarget()) {
            status = QteStatus.SUCCESS;
        }
    }

    private void acceptRhythm(QteInput input) {
        if (input.kind() != QteInput.Kind.PRESS) {
            return;
        }
        double target = (double) (index + 1) / (definition.keys().size() + 1);
        double actual = (double) elapsedTicks / definition.durationTicks();
        if (!definition.keys().get(index).equals(input.key()) || Math.abs(actual - target) > 0.12) {
            status = QteStatus.FAILURE;
            return;
        }
        index++;
        if (index == definition.keys().size()) {
            status = QteStatus.SUCCESS;
        }
    }

    private String expectedKey() {
        return definition.keys().getFirst();
    }

    private int holdTarget() {
        return Math.max(1, (int) Math.ceil(definition.durationTicks() * 0.60));
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private static double clampSigned(double value) {
        if (!Double.isFinite(value)) {
            return 0;
        }
        return Math.max(-1, Math.min(1, value));
    }

    public enum Strategy {
        SINGLE,
        TIMING,
        SEQUENCE,
        PRECISION,
        AIM,
        TRACKING,
        HOLD,
        MASH,
        RHYTHM
    }
}
