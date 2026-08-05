package fr.aicha.freshsmooth.qte.domain;

public enum QteStatus {
    ACTIVE,
    SUCCESS,
    FAILURE,
    TIMEOUT;

    public boolean terminal() {
        return this != ACTIVE;
    }
}
