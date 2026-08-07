package fr.xec9.qte.mixin;

import fr.xec9.qte.client.QteClient;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void qteEngine$blockKeyPress(
        long windowPointer,
        int key,
        int scanCode,
        int action,
        int modifiers,
        CallbackInfo callback
    ) {
        if (QteClient.blocksGameInput()) {
            callback.cancel();
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void qteEngine$blockCharTyped(long windowPointer, int codePoint, int modifiers, CallbackInfo callback) {
        if (QteClient.blocksGameInput()) {
            callback.cancel();
        }
    }
}
