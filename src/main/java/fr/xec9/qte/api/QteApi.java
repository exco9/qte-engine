package fr.xec9.qte.api;

import fr.xec9.qte.domain.QteDefinition;
import fr.xec9.qte.server.QteSavedData;
import fr.xec9.qte.server.QteSessions;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Stable entry point for mods that want to run QTEs without Minecraft commands. */
public final class QteApi {
    private QteApi() {}

    public static UUID play(ServerPlayer player, String qteId) {
        return play(player, qteId, QteRunOptions.DEFAULT);
    }

    public static UUID play(ServerPlayer player, String qteId, QteRunOptions options) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(qteId, "qteId");
        Objects.requireNonNull(options, "options");
        QteDefinition definition = getDefinition(player.getServer(), qteId)
            .orElseThrow(() -> new IllegalArgumentException("Unknown QTE definition: " + qteId));
        return QteSessions.start(player, definition, options);
    }

    public static Optional<QteDefinition> getDefinition(MinecraftServer server, String qteId) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(qteId, "qteId");
        return QteSavedData.get(server).registry().find(qteId);
    }

    public static Optional<UUID> activeSession(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        return QteSessions.activeSession(player);
    }

    public static boolean isActive(ServerPlayer player) {
        return activeSession(player).isPresent();
    }

    public static boolean cancel(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        return QteSessions.cancel(player);
    }
}
