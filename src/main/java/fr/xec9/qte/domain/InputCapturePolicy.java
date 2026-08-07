package fr.xec9.qte.domain;

public final class InputCapturePolicy {
    private InputCapturePolicy() {}

    public static boolean blocksGameInput(boolean qteActive, boolean exclusiveInput) {
        return qteActive && exclusiveInput;
    }
}
