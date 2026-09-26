package fr.xec9.qte.domain;

import java.util.UUID;

/** Decides whether a server terminal packet should remove the client's current HUD. */
public final class QteTerminalPolicy {
    private QteTerminalPolicy() {}
    public static boolean shouldClear(UUID activeId, UUID completedId, boolean terminal, boolean preserveLinger) {
        return activeId != null && activeId.equals(completedId) && !(preserveLinger && terminal);
    }
}
