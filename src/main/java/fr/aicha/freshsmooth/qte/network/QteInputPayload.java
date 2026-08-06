package fr.aicha.freshsmooth.qte.network;

import fr.aicha.freshsmooth.qte.QteEngine;
import fr.aicha.freshsmooth.qte.domain.QteInput;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record QteInputPayload(
    UUID sessionId,
    QteInput.Kind kind,
    String key,
    double value,
    double secondaryValue
) implements CustomPacketPayload {
    public static final Type<QteInputPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(QteEngine.MOD_ID, "input")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, QteInputPayload> STREAM_CODEC =
        CustomPacketPayload.codec(QteInputPayload::write, QteInputPayload::new);

    public QteInputPayload(RegistryFriendlyByteBuf buffer) {
        this(
            buffer.readUUID(),
            buffer.readEnum(QteInput.Kind.class),
            buffer.readUtf(64),
            buffer.readDouble(),
            buffer.readDouble()
        );
    }

    public static QteInputPayload from(UUID sessionId, QteInput input) {
        return new QteInputPayload(
            sessionId,
            input.kind(),
            input.key(),
            input.value(),
            input.secondaryValue()
        );
    }

    public QteInput input() {
        return new QteInput(kind, key, value, secondaryValue);
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(sessionId);
        buffer.writeEnum(kind);
        buffer.writeUtf(key, 64);
        buffer.writeDouble(value);
        buffer.writeDouble(secondaryValue);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
