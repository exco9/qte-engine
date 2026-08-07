package fr.xec9.qte.domain;

public enum QteStatus {
    ACTIVE,
    SUCCESS,
    FAILURE,
    TIMEOUT;

    public boolean terminal() {
        return this != ACTIVE;
    }
}
