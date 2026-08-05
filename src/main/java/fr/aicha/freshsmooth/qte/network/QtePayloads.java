package fr.aicha.freshsmooth.qte.network;

import fr.aicha.freshsmooth.qte.client.QteClient;
import fr.aicha.freshsmooth.qte.server.QteSessions;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class QtePayloads {
    private QtePayloads() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("2");
        registrar.playToClient(
            StartQtePayload.TYPE,
            StartQtePayload.STREAM_CODEC,
            (payload, context) -> QteClient.handleStart(payload)
        );
        registrar.playToServer(
            FinishQtePayload.TYPE,
            FinishQtePayload.STREAM_CODEC,
            (payload, context) -> QteSessions.finish((ServerPlayer) context.player(), payload)
        );
    }
}
