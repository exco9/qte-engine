package fr.xec9.qte.domain;

public final class InputCapturePolicy {
    private InputCapturePolicy() {}

    public static boolean blocksGameInput(boolean qteActive, boolean exclusiveInput) {
        return blocksGameInput(qteActive, exclusiveInput, false);
    }

    public static boolean blocksGameInput(boolean qteActive, boolean exclusiveInput, boolean pauseMenuOpen) {
        return qteActive && exclusiveInput && !pauseMenuOpen;
    }

    public static boolean blocksKeyPress(boolean qteActive, boolean exclusiveInput, int key) {
        return key != 256 && blocksGameInput(qteActive, exclusiveInput);
    }
}
