package fr.aicha.freshsmooth.qte.domain;

import java.util.EnumSet;
import java.util.Set;

public final class QteJudge {
    private static final Set<QteType> SINGLE = EnumSet.of(
        QteType.REACTION,
        QteType.REACTION_CHOICE,
        QteType.OBSERVATION,
        QteType.ATTENTION
    );
    private static final Set<QteType> TIMED = EnumSet.of(QteType.TIMING, QteType.DIALOGUE_TIMING);
    private static final Set<QteType> SEQUENCE = EnumSet.of(
        QteType.INPUT_SEQUENCE,
        QteType.DIRECTION,
        QteType.MEMORY,
        QteType.PATTERN
    );
    private static final Set<QteType> PRECISION = EnumSet.of(
        QteType.ANALOG_PRECISION,
        QteType.AIM,
        QteType.TRACKING,
        QteType.BALANCE
    );

    private final QteDefinition definition;
    private QteStatus status = QteStatus.ACTIVE;
    private int index;
    private int presses;
    private long heldSince = -1;
    private long elapsedTicks;
    private double axisDistance = 1;

    public QteJudge(QteDefinition definition) {
        this.definition = definition;
    }

    public QteStatus accept(QteInput input, long elapsedTicks) {
        tick(elapsedTicks);
        if (status.terminal()) {
            return status;
        }

        QteType type = definition.type();
        if (SINGLE.contains(type)) {
            acceptSingle(input);
        } else if (TIMED.contains(type)) {
            acceptTimed(input);
        } else if (SEQUENCE.contains(type)) {
            acceptSequence(input);
        } else if (PRECISION.contains(type)) {
            acceptPrecision(input);
        } else if (type == QteType.HOLD) {
            acceptHold(input);
        } else if (type == QteType.MASH) {
            acceptMash(input);
        } else if (type == QteType.RHYTHM) {
            acceptRhythm(input);
        }
        return status;
    }

    public QteStatus tick(long elapsedTicks) {
        if (status.terminal()) {
            return status;
        }
        this.elapsedTicks = Math.max(0, elapsedTicks);
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
        return clamp((double) elapsedTicks / definition.durationTicks());
    }

    public int currentIndex() {
        return index;
    }

    public int mashTarget() {
        return Math.max(5, (int) Math.round(definition.durationSeconds() * 4));
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
}
