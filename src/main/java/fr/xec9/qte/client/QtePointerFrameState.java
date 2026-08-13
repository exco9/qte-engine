package fr.xec9.qte.client;

import fr.xec9.qte.domain.QtePointerModel;

final class QtePointerFrameState {
    private QtePointerModel.Point pointer = new QtePointerModel.Point(0, 0);
    private boolean pendingSample;

    void move(double deltaX, double deltaY, double sensitivity, boolean invertY) {
        pointer = QtePointerModel.move(pointer, deltaX, deltaY, sensitivity, invertY);
        pendingSample = true;
    }

    QtePointerModel.Point pointer() {
        return pointer;
    }

    boolean hasPendingSample() {
        return pendingSample;
    }

    QtePointerModel.Point consumePendingSample() {
        if (!pendingSample) {
            return null;
        }
        pendingSample = false;
        return pointer;
    }
}
