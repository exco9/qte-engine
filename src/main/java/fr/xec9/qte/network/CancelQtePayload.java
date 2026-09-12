package fr.xec9.qte.network;

import fr.xec9.qte.QteEngine;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CancelQtePayload(UUID sessionId) implements CustomPacketPayload {
    public static final Type<CancelQtePayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(QteEngine.MOD_ID, "cancel")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, CancelQtePayload> STREAM_CODEC =
        CustomPacketPayload.codec(CancelQtePayload::write, CancelQtePayload::new);

    public CancelQtePayload(RegistryFriendlyByteBuf buffer) {
        this(buffer.readUUID());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(sessionId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
