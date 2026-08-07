package fr.xec9.qte.network;

import fr.xec9.qte.QteEngine;
import fr.xec9.qte.domain.QteDefinition;
import fr.xec9.qte.domain.QteType;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record StartQtePayload(
    UUID sessionId,
    String id,
    QteType qteType,
    List<String> keys,
    int durationTicks,
    boolean exclusiveInput,
    boolean hideHud,
    String texture
) implements CustomPacketPayload {
    public static final Type<StartQtePayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(QteEngine.MOD_ID, "start")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, StartQtePayload> STREAM_CODEC =
        CustomPacketPayload.codec(StartQtePayload::write, StartQtePayload::new);

    public StartQtePayload(RegistryFriendlyByteBuf buffer) {
        this(
            buffer.readUUID(),
            buffer.readUtf(64),
            QteType.parse(buffer.readUtf(32)),
            buffer.readList(value -> value.readUtf(64)),
            buffer.readVarInt(),
            buffer.readBoolean(),
            buffer.readBoolean(),
            buffer.readBoolean() ? buffer.readUtf(256) : null
        );
    }

    public static StartQtePayload from(UUID sessionId, QteDefinition definition) {
        return new StartQtePayload(
            sessionId,
            definition.id(),
            definition.type(),
            definition.keys(),
            definition.durationTicks(),
            definition.exclusiveInput(),
            definition.hideHud(),
            definition.texture()
        );
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(sessionId);
        buffer.writeUtf(id, 64);
        buffer.writeUtf(qteType.name(), 32);
        buffer.writeCollection(keys, (target, value) -> target.writeUtf(value, 64));
        buffer.writeVarInt(durationTicks);
        buffer.writeBoolean(exclusiveInput);
        buffer.writeBoolean(hideHud);
        buffer.writeBoolean(texture != null);
        if (texture != null) {
            buffer.writeUtf(texture, 256);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
