package fr.aicha.freshsmooth.qte.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import fr.aicha.freshsmooth.qte.domain.QteDefinition;
import fr.aicha.freshsmooth.qte.domain.QteInput;
import fr.aicha.freshsmooth.qte.domain.QteJudge;
import fr.aicha.freshsmooth.qte.domain.QteStatus;
import fr.aicha.freshsmooth.qte.domain.QteType;
import fr.aicha.freshsmooth.qte.QteEngine;
import fr.aicha.freshsmooth.qte.network.FinishQtePayload;
import fr.aicha.freshsmooth.qte.network.StartQtePayload;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = QteEngine.MOD_ID, value = Dist.CLIENT)
public final class QteClient {
    private static ClientSession active;

    private QteClient() {}

    public static void handleStart(StartQtePayload payload) {
        QteDefinition definition = new QteDefinition(
            payload.id(),
            payload.qteType(),
            payload.keys(),
            payload.durationTicks(),
            "client-only",
            payload.exclusiveInput(),
            payload.texture()
        );
        active = new ClientSession(payload, definition);
        if (blocksGameInput()) {
            KeyMapping.releaseAll();
        }
    }

    public static boolean blocksGameInput() {
        return fr.aicha.freshsmooth.qte.domain.InputCapturePolicy.blocksGameInput(
            active != null && !active.judge().status().terminal(),
            active != null && active.definition().exclusiveInput()
        );
    }

    static String keyboardLayout() {
        return ClientKeyboardLayout.detect();
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
            active = null;
            return;
        }

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
                active = null;
            }
        }
    }

    @SubscribeEvent
    public static void renderHud(RenderGuiEvent.Post event) {
        if (active != null) {
            QteHud.render(event.getGuiGraphics(), active);
        }
    }

    static final class ClientSession {
        private final StartQtePayload payload;
        private final QteDefinition definition;
        private final QteJudge judge;
        private final Set<String> keys;
        private final Map<String, Boolean> previous = new HashMap<>();
        private long elapsed;
        private boolean sent;
        private int lingerTicks;

        ClientSession(StartQtePayload payload, QteDefinition definition) {
            this.payload = payload;
            this.definition = definition;
            this.judge = new QteJudge(definition);
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
            judge.tick(elapsed);
            if (judge.status().terminal()) {
                return;
            }
            for (String name : keys) {
                boolean down = isDown(name);
                boolean wasDown = previous.getOrDefault(name, false);
                if (down != wasDown) {
                    if (isPrecision() && down) {
                        judge.accept(QteInput.axis(markerDistance()), elapsed);
                    } else {
                        judge.accept(down ? QteInput.press(name) : QteInput.release(name), elapsed);
                    }
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

        boolean lingerComplete() {
            return lingerTicks >= 20;
        }

        double markerDistance() {
            return Math.cos(elapsed * 0.18);
        }

        double timeRemaining() {
            return Math.max(0, (definition.durationTicks() - elapsed) / 20.0);
        }

        private boolean isPrecision() {
            return switch (definition.type()) {
                case ANALOG_PRECISION, AIM, TRACKING, BALANCE -> true;
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
