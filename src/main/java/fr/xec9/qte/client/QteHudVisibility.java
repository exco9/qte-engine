package fr.xec9.qte.client;

final class QteHudVisibility {
    private final boolean requested;
    private boolean captured;
    private boolean previous;

    QteHudVisibility(boolean requested) {
        this.requested = requested;
    }

    boolean begin(boolean current) {
        if (!requested) {
            return current;
        }
        previous = current;
        captured = true;
        return true;
    }

    boolean enforce(boolean current) {
        return requested && captured ? true : current;
    }

    boolean restore(boolean current) {
        if (!captured) {
            return current;
        }
        captured = false;
        return previous;
    }
}
