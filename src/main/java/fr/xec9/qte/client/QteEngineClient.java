package fr.xec9.qte.client;

import fr.xec9.qte.QteEngine;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = QteEngine.MOD_ID, dist = Dist.CLIENT)
public final class QteEngineClient {
    public QteEngineClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(QteClient::registerGuiLayers);
        container.registerConfig(ModConfig.Type.CLIENT, QteClientConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
