package fr.aicha.freshsmooth.qte.server;

import fr.aicha.freshsmooth.qte.domain.QteDefinition;
import fr.aicha.freshsmooth.qte.network.FinishQtePayload;
import fr.aicha.freshsmooth.qte.network.StartQtePayload;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class QteSessions {
    private static final Map<UUID, Session> ACTIVE = new HashMap<>();

    private QteSessions() {}

    public static void start(ServerPlayer player, QteDefinition definition) {
        UUID sessionId = UUID.randomUUID();
        ACTIVE.put(player.getUUID(), new Session(sessionId, definition, player.serverLevel().getGameTime()));
        PacketDistributor.sendToPlayer(player, StartQtePayload.from(sessionId, definition));
    }

    public static void finish(ServerPlayer player, FinishQtePayload payload) {
        Session session = ACTIVE.get(player.getUUID());
        if (session == null || !session.id().equals(payload.sessionId())) {
            return;
        }
        ACTIVE.remove(player.getUUID());
        long elapsed = player.serverLevel().getGameTime() - session.startedAt();
        if (!payload.success() || elapsed > session.definition().durationTicks() + 20L) {
            return;
        }

        String command = session.definition().resultCommand().replace("%player%", player.getGameProfile().getName());
        player.getServer().getCommands().performPrefixedCommand(
            player.getServer().createCommandSourceStack().withSuppressedOutput(),
            command
        );
    }

    public static void tick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        Iterator<Map.Entry<UUID, Session>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Session> entry = iterator.next();
            Session session = entry.getValue();
            if (server.getPlayerList().getPlayer(entry.getKey()) == null
                || now - session.startedAt() > session.definition().durationTicks() + 40L) {
                iterator.remove();
            }
        }
    }

    private record Session(UUID id, QteDefinition definition, long startedAt) {}
}
