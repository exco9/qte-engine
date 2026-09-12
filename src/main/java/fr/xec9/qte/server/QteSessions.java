package fr.xec9.qte.server;

import fr.xec9.qte.api.QteCompletedEvent;
import fr.xec9.qte.api.QteResult;
import fr.xec9.qte.api.QteResultStatus;
import fr.xec9.qte.api.QteRunOptions;
import fr.xec9.qte.domain.QteDefinition;
import fr.xec9.qte.domain.QteStatus;
import fr.xec9.qte.network.CancelQtePayload;
import fr.xec9.qte.network.FinishQtePayload;
import fr.xec9.qte.network.QteInputPayload;
import fr.xec9.qte.network.StartQtePayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public final class QteSessions {
    private static final Map<UUID, QteServerSession> ACTIVE = new HashMap<>();

    private QteSessions() {}

    public static UUID start(ServerPlayer player, QteDefinition definition) {
        return start(player, definition, QteRunOptions.DEFAULT);
    }

    public static UUID start(ServerPlayer player, QteDefinition definition, QteRunOptions options) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(options, "options");

        UUID sessionId = UUID.randomUUID();
        QteServerSession session = new QteServerSession(
            sessionId,
            definition,
            options,
            player.serverLevel().getGameTime()
        );
        QteServerSession replaced = ACTIVE.put(player.getUUID(), session);
        PacketDistributor.sendToPlayer(player, StartQtePayload.from(sessionId, definition));
        if (replaced != null) {
            publishResult(player, replaced, QteResultStatus.REPLACED);
        }
        return sessionId;
    }

    public static Optional<UUID> activeSession(ServerPlayer player) {
        QteServerSession session = ACTIVE.get(player.getUUID());
        return session == null ? Optional.empty() : Optional.of(session.id());
    }

    public static boolean cancel(ServerPlayer player) {
        QteServerSession session = ACTIVE.remove(player.getUUID());
        if (session == null) {
            return false;
        }
        PacketDistributor.sendToPlayer(player, new CancelQtePayload(session.id()));
        publishResult(player, session, QteResultStatus.CANCELLED);
        return true;
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
        finishRemoved(player, session, outcome.get());
    }

    private static void finishRemoved(ServerPlayer player, QteServerSession session, QteStatus outcome) {
        publishResult(player, session, QteResultStatus.fromInternal(outcome));
        if (session.options().executeConfiguredCommands()) {
            executeOutcome(player, session, outcome);
        }
    }

    private static void publishResult(
        ServerPlayer player,
        QteServerSession session,
        QteResultStatus status
    ) {
        NeoForge.EVENT_BUS.post(new QteCompletedEvent(
            player,
            new QteResult(session.id(), player.getUUID(), session.definition().id(), status)
        ));
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
        List<PendingCompletion> completed = new ArrayList<>();
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
                completed.add(new PendingCompletion(player, session, outcome.get()));
            } else if (session.expired(now)) {
                iterator.remove();
                completed.add(new PendingCompletion(player, session, QteStatus.TIMEOUT));
            }
        }
        completed.forEach(result -> finishRemoved(result.player(), result.session(), result.outcome()));
    }

    private record PendingCompletion(
        ServerPlayer player,
        QteServerSession session,
        QteStatus outcome
    ) {}
}
