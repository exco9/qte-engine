package fr.aicha.freshsmooth.qte;

import fr.aicha.freshsmooth.qte.command.QteCommands;
import fr.aicha.freshsmooth.qte.network.QtePayloads;
import fr.aicha.freshsmooth.qte.server.QteSessions;
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
    }

    private void registerCommands(RegisterCommandsEvent event) {
        QteCommands.register(event.getDispatcher());
    }

    private void serverTick(ServerTickEvent.Post event) {
        QteSessions.tick(event.getServer());
    }
}
