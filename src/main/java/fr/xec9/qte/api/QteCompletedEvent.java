package fr.xec9.qte.api;

import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** Fired on the NeoForge event bus when a QTE session reaches a terminal result. */
public final class QteCompletedEvent extends Event {
    private final ServerPlayer player;
    private final QteResult result;

    public QteCompletedEvent(ServerPlayer player, QteResult result) {
        this.player = Objects.requireNonNull(player, "player");
        this.result = Objects.requireNonNull(result, "result");
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public QteResult getResult() {
        return result;
    }
}
