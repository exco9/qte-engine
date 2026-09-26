package fr.xec9.qte;

import fr.xec9.qte.command.QteCommands;
import fr.xec9.qte.network.QtePayloads;
import fr.xec9.qte.server.QteSessions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(QteEngine.MOD_ID)
public final class QteEngine {
    public static final String MOD_ID = "qte_engine";

    public QteEngine(IEventBus modBus) {
        modBus.addListener(QtePayloads::register);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::serverTick);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) QteSessions.playerLoggedOut(player);
        });
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppingEvent event) -> QteSessions.serverStopping(event.getServer()));
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStartedEvent event) -> QteSessions.serverStarted(event.getServer()));
    }

    private void registerCommands(RegisterCommandsEvent event) {
        QteCommands.register(event.getDispatcher());
    }

    private void serverTick(ServerTickEvent.Post event) {
        QteSessions.tick(event.getServer());
    }
}
