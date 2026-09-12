package fr.xec9.qte.api;

import fr.xec9.qte.domain.QteStatus;

/** Terminal outcome exposed to integrations using QTE Engine programmatically. */
public enum QteResultStatus {
    SUCCESS,
    FAILURE,
    TIMEOUT,
    CANCELLED,
    REPLACED;

    public static QteResultStatus fromInternal(QteStatus status) {
        return switch (status) {
            case SUCCESS -> SUCCESS;
            case FAILURE -> FAILURE;
            case TIMEOUT -> TIMEOUT;
            case ACTIVE -> throw new IllegalArgumentException("ACTIVE is not a terminal QTE result");
        };
    }
}
