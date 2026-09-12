package fr.xec9.qte.server;

import com.mojang.logging.LogUtils;
import fr.xec9.qte.api.*;
import fr.xec9.qte.domain.QteDefinition;
import fr.xec9.qte.domain.QteStatus;
import fr.xec9.qte.network.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

/** Logical server-thread implementation of the public QteApi. */
public final class QteSessions {
    private static final Map<UUID, ActiveRun> ACTIVE = new HashMap<>();
    private static final Set<UUID> DISCONNECTING = new HashSet<>();
    private static boolean stopping;
    private QteSessions() {}

    public static UUID start(ServerPlayer player, QteDefinition definition) {
        return start(player, definition, QteRunOptions.DEFAULT);
    }

    public static UUID start(ServerPlayer player, QteDefinition definition, QteRunOptions options) {
        return start(player, definition, options, result -> {});
    }

    public static UUID start(ServerPlayer player, QteDefinition definition, QteRunOptions options,
                             Consumer<QteResult> completion) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(completion, "completion");
        requireServerThread(player.getServer());
        UUID id = UUID.randomUUID();
        ActiveRun run = new ActiveRun(player, new QteServerSession(id, definition, options,
            player.getServer().overworld().getGameTime(), completion));
        if (stopping || DISCONNECTING.contains(player.getUUID()) || player.hasDisconnected()) {
            terminate(run, QteResultStatus.CANCELLED);
            return id;
        }
        // Install first; an observer of REPLACED may itself start an even newer run.
        ActiveRun previous = ACTIVE.put(player.getUUID(), run);
        if (previous != null) terminate(previous, QteResultStatus.REPLACED);
        if (ACTIVE.get(player.getUUID()) == run) {
            try {
                PacketDistributor.sendToPlayer(player, StartQtePayload.from(id, definition));
            } catch (RuntimeException error) {
                if (ACTIVE.remove(player.getUUID(), run)) terminate(run, QteResultStatus.CANCELLED);
                throw error;
            }
        }
        return id;
    }

    public static Optional<UUID> activeSession(ServerPlayer player) {
        requireServerThread(player.getServer());
        ActiveRun run = ACTIVE.get(player.getUUID());
        return run == null ? Optional.empty() : Optional.of(run.session.id());
    }

    public static boolean cancel(ServerPlayer player) {
        return activeSession(player).map(id -> cancel(player, id)).orElse(false);
    }

    public static boolean cancel(ServerPlayer player, UUID sessionId) {
        requireServerThread(player.getServer());
        ActiveRun run = ACTIVE.get(player.getUUID());
        if (run == null || !run.session.matches(sessionId) || !ACTIVE.remove(player.getUUID(), run)) return false;
        terminate(run, QteResultStatus.CANCELLED);
        return true;
    }

    public static void acceptInput(ServerPlayer player, QteInputPayload payload) {
        ActiveRun run = ACTIVE.get(player.getUUID());
        if (run != null) {
            long now = player.getServer().overworld().getGameTime();
            if (run.session.accept(payload.sessionId(), payload.input(), now)) complete(run, run.session.outcome(now));
        }
    }

    public static void finish(ServerPlayer player, FinishQtePayload payload) {
        ActiveRun run = ACTIVE.get(player.getUUID());
        if (run != null && run.session.matches(payload.sessionId()))
            complete(run, run.session.finish(payload.sessionId(), player.getServer().overworld().getGameTime()));
    }

    private static void complete(ActiveRun run, Optional<QteStatus> outcome) {
        if (outcome.isPresent() && ACTIVE.remove(run.player.getUUID(), run))
            terminate(run, QteResultStatus.fromInternal(outcome.get()));
    }

    private static void terminate(ActiveRun run, QteResultStatus status) {
        QteServerSession session = run.session;
        ServerPlayer player = run.player;
        QteResult result = new QteResult(session.id(), player.getUUID(), session.definition().id(), status);
        try {
            session.deliver(result, () -> {
                // Release client input before observers can advance dialogue or start another run.
                safely(session, () -> {
                    if (!player.hasDisconnected()) {
                        if (status == QteResultStatus.CANCELLED || status == QteResultStatus.REPLACED)
                            PacketDistributor.sendToPlayer(player, new CancelQtePayload(session.id()));
                        else PacketDistributor.sendToPlayer(player, new TerminalQtePayload(session.id()));
                    }
                });
                safely(session, () -> NeoForge.EVENT_BUS.post(new QteCompletedEvent(player, result)));
                if (status != QteResultStatus.CANCELLED && status != QteResultStatus.REPLACED
                        && session.options().executeConfiguredCommands())
                    safely(session, () -> executeOutcome(player, session, status));
            });
        } catch (RuntimeException error) {
            LogUtils.getLogger().error("QTE completion callback failed for {}", session.id(), error);
        }
    }

    private static void safely(QteServerSession session, Runnable action) {
        try { action.run(); }
        catch (RuntimeException error) { LogUtils.getLogger().error("QTE result handling failed for {}", session.id(), error); }
    }

    private static void executeOutcome(ServerPlayer player, QteServerSession session, QteResultStatus outcome) {
        String configuredCommand = outcome == QteResultStatus.SUCCESS
            ? session.definition().resultCommand() : session.definition().failureCommand();
        if (configuredCommand == null || configuredCommand.isBlank()) return;
        String command = configuredCommand.replace("%player%", player.getGameProfile().getName());
        player.getServer().getCommands().performPrefixedCommand(
            player.getServer().createCommandSourceStack().withEntity(player)
                .withPosition(player.position()).withRotation(player.getRotationVector()).withSuppressedOutput(), command);
    }

    public static void tick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        for (var entry : Map.copyOf(ACTIVE).entrySet()) {
            ActiveRun run = entry.getValue();
            if (ACTIVE.get(entry.getKey()) != run) continue;
            ServerPlayer currentPlayer = server.getPlayerList().getPlayer(entry.getKey());
            if (currentPlayer != run.player) {
                if (ACTIVE.remove(entry.getKey(), run)) terminate(run, QteResultStatus.CANCELLED);
            } else complete(run, run.session.outcome(now));
        }
    }

    public static void playerLoggedOut(ServerPlayer player) {
        DISCONNECTING.add(player.getUUID());
        try {
            ActiveRun run = ACTIVE.remove(player.getUUID());
            if (run != null) terminate(run, QteResultStatus.CANCELLED);
        } finally { DISCONNECTING.remove(player.getUUID()); }
    }

    public static void serverStopping(MinecraftServer server) {
        stopping = true;
        var runs = List.copyOf(ACTIVE.values());
        ACTIVE.clear();
        for (ActiveRun run : runs) terminate(run, QteResultStatus.CANCELLED);
    }

    public static void serverStarted(MinecraftServer server) { stopping = false; }

    public static void requireServerThread(MinecraftServer server) {
        if (server == null || !server.isSameThread()) throw new IllegalStateException("QTE API requires the server thread");
    }

    private record ActiveRun(ServerPlayer player, QteServerSession session) {}
}
