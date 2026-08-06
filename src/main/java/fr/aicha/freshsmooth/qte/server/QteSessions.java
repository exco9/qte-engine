package fr.aicha.freshsmooth.qte.server;

import fr.aicha.freshsmooth.qte.domain.QteDefinition;
import fr.aicha.freshsmooth.qte.domain.QteStatus;
import fr.aicha.freshsmooth.qte.network.FinishQtePayload;
import fr.aicha.freshsmooth.qte.network.QteInputPayload;
import fr.aicha.freshsmooth.qte.network.StartQtePayload;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class QteSessions {
    private static final Map<UUID, QteServerSession> ACTIVE = new HashMap<>();

    private QteSessions() {}

    public static void start(ServerPlayer player, QteDefinition definition) {
        UUID sessionId = UUID.randomUUID();
        ACTIVE.put(
            player.getUUID(),
            new QteServerSession(sessionId, definition, player.serverLevel().getGameTime())
        );
        PacketDistributor.sendToPlayer(player, StartQtePayload.from(sessionId, definition));
    }

    public static void acceptInput(ServerPlayer player, QteInputPayload payload) {
        QteServerSession session = ACTIVE.get(player.getUUID());
        if (session != null) {
            long now = player.serverLevel().getGameTime();
            if (session.accept(payload.sessionId(), payload.input(), now)) {
                complete(player, session, session.outcome(now));
            }
        }
    }

    public static void finish(ServerPlayer player, FinishQtePayload payload) {
        QteServerSession session = ACTIVE.get(player.getUUID());
        if (session == null || !session.matches(payload.sessionId())) {
            return;
        }
        complete(player, session, session.finish(payload.sessionId(), player.serverLevel().getGameTime()));
    }

    private static void complete(
        ServerPlayer player,
        QteServerSession session,
        Optional<QteStatus> outcome
    ) {
        if (outcome.isEmpty() || !ACTIVE.remove(player.getUUID(), session)) {
            return;
        }
        executeOutcome(player, session, outcome.get());
    }

    private static void executeOutcome(ServerPlayer player, QteServerSession session, QteStatus outcome) {
        String configuredCommand = outcome == QteStatus.SUCCESS
            ? session.definition().resultCommand()
            : session.definition().failureCommand();
        if (configuredCommand == null || configuredCommand.isBlank()) {
            return;
        }
        String command = configuredCommand.replace("%player%", player.getGameProfile().getName());
        player.getServer().getCommands().performPrefixedCommand(
            player.getServer().createCommandSourceStack()
                .withEntity(player)
                .withPosition(player.position())
                .withRotation(player.getRotationVector())
                .withSuppressedOutput(),
            command
        );
    }

    public static void tick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        Iterator<Map.Entry<UUID, QteServerSession>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, QteServerSession> entry = iterator.next();
            QteServerSession session = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                iterator.remove();
                continue;
            }
            Optional<QteStatus> outcome = session.outcome(now);
            if (outcome.isPresent()) {
                iterator.remove();
                executeOutcome(player, session, outcome.get());
            } else if (session.expired(now)) {
                iterator.remove();
            }
        }
    }
}
