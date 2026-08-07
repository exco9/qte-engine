package fr.xec9.qte.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import fr.xec9.qte.domain.QteDefinition;
import fr.xec9.qte.domain.QteInput;
import fr.xec9.qte.domain.QteJudge;
import fr.xec9.qte.domain.QtePointerModel;
import fr.xec9.qte.domain.QteStatus;
import fr.xec9.qte.domain.QteType;
import fr.xec9.qte.QteEngine;
import fr.xec9.qte.network.FinishQtePayload;
import fr.xec9.qte.network.QteInputPayload;
import fr.xec9.qte.network.StartQtePayload;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = QteEngine.MOD_ID, value = Dist.CLIENT)
public final class QteClient {
    private static ClientSession active;

    private QteClient() {}

    public static void handleStart(StartQtePayload payload) {
        clearActive();
        QteDefinition definition = new QteDefinition(
            payload.id(),
            payload.qteType(),
            payload.keys(),
            payload.durationTicks(),
            "client-only",
            null,
            payload.exclusiveInput(),
            payload.hideHud(),
            payload.texture()
        );
        active = new ClientSession(payload, definition);
        if (blocksGameInput()) {
            KeyMapping.releaseAll();
        }
    }

    public static boolean blocksGameInput() {
        return fr.xec9.qte.domain.InputCapturePolicy.blocksGameInput(
            active != null && !active.judge().status().terminal(),
            active != null && active.definition().exclusiveInput()
        );
    }

    public static void onMouseMovement(double deltaX, double deltaY) {
        if (active != null) {
            active.addMouseMovement(deltaX, deltaY);
        }
    }

    static ClientSession active() {
        return active;
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        if (active == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getConnection() == null) {
            clearActive();
            return;
        }

        active.enforceHudHidden();
        active.tick();
        if (active.judge().status().terminal()) {
            if (!active.sent()) {
                KeyMapping.releaseAll();
                PacketDistributor.sendToServer(new FinishQtePayload(
                    active.payload().sessionId(),
                    active.judge().status() == QteStatus.SUCCESS
                ));
                active.markSent();
            }
            if (active.lingerComplete()) {
                clearActive();
            }
        }
    }

    @SubscribeEvent
    public static void repositionChat(CustomizeGuiOverlayEvent.Chat event) {
        if (active == null) {
            return;
        }
        int chatBottom = QteHudModel.chatBottom(
            event.getGuiGraphics().guiWidth(),
            event.getGuiGraphics().guiHeight()
        );
        event.setPosY(Math.min(event.getPosY(), chatBottom));
    }

    @SubscribeEvent
    public static void renderHud(RenderGuiEvent.Post event) {
        if (active != null) {
            QteHud.render(event.getGuiGraphics(), active);
        }
    }

    @SubscribeEvent
    public static void renderHudAboveFocusedChat(ScreenEvent.Render.Post event) {
        if (active != null && event.getScreen() instanceof ChatScreen) {
            QteHud.render(event.getGuiGraphics(), active);
        }
    }

    private static void clearActive() {
        if (active != null) {
            active.restoreHud();
            active = null;
        }
    }

    static final class ClientSession {
        private final StartQtePayload payload;
        private final QteDefinition definition;
        private final QteJudge judge;
        private final Set<String> keys;
        private final Map<String, Boolean> previous = new HashMap<>();
        private final QteHudVisibility hudVisibility;
        private long elapsed;
        private boolean sent;
        private int lingerTicks;
        private QtePointerModel.Point pointer = new QtePointerModel.Point(0, 0);
        private double pendingMouseX;
        private double pendingMouseY;

        ClientSession(StartQtePayload payload, QteDefinition definition) {
            this.payload = payload;
            this.definition = definition;
            this.judge = new QteJudge(definition);
            this.hudVisibility = new QteHudVisibility(definition.hideHud());
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.options.hideGui = hudVisibility.begin(minecraft.options.hideGui);
            this.keys = new LinkedHashSet<>(definition.keys());
            for (String name : keys) {
                previous.put(name, isDown(name));
            }
        }

        void tick() {
            if (judge.status().terminal()) {
                lingerTicks++;
                return;
            }
            elapsed++;
            flushMouseMovement();
            judge.tick(elapsed);
            if (judge.status().terminal()) {
                return;
            }
            for (String name : keys) {
                boolean down = isDown(name);
                boolean wasDown = previous.getOrDefault(name, false);
                if (down != wasDown) {
                    QteInput input;
                    if (isAutomaticPrecision() && down) {
                        input = QteInput.axis(markerDistance());
                    } else {
                        input = down ? QteInput.press(name) : QteInput.release(name);
                    }
                    acceptAndSend(input);
                    previous.put(name, down);
                    if (judge.status().terminal()) {
                        break;
                    }
                }
            }
        }

        StartQtePayload payload() {
            return payload;
        }

        QteDefinition definition() {
            return definition;
        }

        QteJudge judge() {
            return judge;
        }

        long elapsed() {
            return elapsed;
        }

        boolean sent() {
            return sent;
        }

        void markSent() {
            sent = true;
        }

        void enforceHudHidden() {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.options.hideGui = hudVisibility.enforce(minecraft.options.hideGui);
        }

        void restoreHud() {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.options.hideGui = hudVisibility.restore(minecraft.options.hideGui);
        }

        boolean lingerComplete() {
            return lingerTicks >= 20;
        }

        double markerDistance() {
            return Math.cos(elapsed * 0.18);
        }

        QtePointerModel.Point pointer() {
            return pointer;
        }

        QtePointerModel.Point pointerTarget() {
            return QtePointerModel.target(definition.type(), definition.id(), elapsed, definition.durationTicks());
        }

        double timeRemaining() {
            return Math.max(0, (definition.durationTicks() - elapsed) / 20.0);
        }

        private void addMouseMovement(double deltaX, double deltaY) {
            if (!usesPointer() || !Double.isFinite(deltaX) || !Double.isFinite(deltaY)) {
                return;
            }
            pendingMouseX += deltaX;
            pendingMouseY += deltaY;
        }

        private void flushMouseMovement() {
            if (!usesPointer()) {
                return;
            }
            if (pendingMouseX != 0 || pendingMouseY != 0) {
                Minecraft minecraft = Minecraft.getInstance();
                pointer = QtePointerModel.move(
                    pointer,
                    pendingMouseX,
                    pendingMouseY,
                    minecraft.options.sensitivity().get(),
                    minecraft.options.invertYMouse().get()
                );
            }
            pendingMouseX = 0;
            pendingMouseY = 0;
            acceptAndSend(QteInput.pointer(pointer.x(), pointer.y()));
        }

        private void acceptAndSend(QteInput input) {
            judge.accept(input, elapsed);
            PacketDistributor.sendToServer(QteInputPayload.from(payload.sessionId(), input));
        }

        private boolean usesPointer() {
            return definition.type() == QteType.AIM || definition.type() == QteType.TRACKING;
        }

        private boolean isAutomaticPrecision() {
            return switch (definition.type()) {
                case ANALOG_PRECISION, BALANCE -> true;
                default -> false;
            };
        }

        private static boolean isDown(String name) {
            try {
                InputConstants.Key key = ClientKeyboardLayout.resolve(name);
                long window = Minecraft.getInstance().getWindow().getWindow();
                if (key.getType() == InputConstants.Type.MOUSE) {
                    return GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
                }
                return InputConstants.isKeyDown(window, key.getValue());
            } catch (IllegalArgumentException error) {
                return false;
            }
        }
    }
}
