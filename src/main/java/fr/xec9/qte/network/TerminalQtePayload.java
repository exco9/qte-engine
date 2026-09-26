package fr.xec9.qte.network;

import fr.xec9.qte.QteEngine;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TerminalQtePayload(UUID sessionId) implements CustomPacketPayload {
    public static final Type<TerminalQtePayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(QteEngine.MOD_ID, "terminal")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalQtePayload> STREAM_CODEC =
        CustomPacketPayload.codec(TerminalQtePayload::write, TerminalQtePayload::new);

    public TerminalQtePayload(RegistryFriendlyByteBuf buffer) {
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
