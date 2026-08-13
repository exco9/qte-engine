package fr.xec9.qte.client;

import fr.xec9.qte.QteEngine;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = QteEngine.MOD_ID, dist = Dist.CLIENT)
public final class QteEngineClient {
    public QteEngineClient(IEventBus modBus) {
        modBus.addListener(QteClient::registerGuiLayers);
    }
}
