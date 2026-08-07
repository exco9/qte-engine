package fr.xec9.qte.network;

import fr.xec9.qte.QteEngine;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FinishQtePayload(UUID sessionId, boolean success) implements CustomPacketPayload {
    public static final Type<FinishQtePayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(QteEngine.MOD_ID, "finish")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FinishQtePayload> STREAM_CODEC =
        CustomPacketPayload.codec(FinishQtePayload::write, FinishQtePayload::new);

    public FinishQtePayload(RegistryFriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(sessionId);
        buffer.writeBoolean(success);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
