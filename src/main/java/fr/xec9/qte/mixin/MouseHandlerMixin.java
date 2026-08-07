package fr.xec9.qte.mixin;

import fr.xec9.qte.client.QteClient;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
abstract class MouseHandlerMixin {
    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    private void qteEngine$blockMouseButton(
        long windowPointer,
        int button,
        int action,
        int modifiers,
        CallbackInfo callback
    ) {
        if (QteClient.blocksGameInput()) {
            callback.cancel();
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void qteEngine$blockMouseScroll(
        long windowPointer,
        double xOffset,
        double yOffset,
        CallbackInfo callback
    ) {
        if (QteClient.blocksGameInput()) {
            callback.cancel();
        }
    }

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void qteEngine$blockCameraTurn(double movementTime, CallbackInfo callback) {
        QteClient.onMouseMovement(accumulatedDX, accumulatedDY);
        if (QteClient.blocksGameInput()) {
            callback.cancel();
        }
    }
}
