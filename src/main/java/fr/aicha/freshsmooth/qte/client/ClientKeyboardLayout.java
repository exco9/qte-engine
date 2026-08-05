package fr.aicha.freshsmooth.qte.client;

import com.mojang.blaze3d.platform.InputConstants;
import fr.aicha.freshsmooth.qte.domain.KeyboardLayoutDetector;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.lwjgl.glfw.GLFW;

final class ClientKeyboardLayout {
    private static final String LOCALIZED_PREFIX = "key.localized.";
    private static final Map<String, InputConstants.Key> CACHE = new HashMap<>();

    private ClientKeyboardLayout() {}

    static InputConstants.Key resolve(String specification) {
        if (!specification.startsWith(LOCALIZED_PREFIX)) {
            return InputConstants.getKey(specification);
        }
        return CACHE.computeIfAbsent(specification, ClientKeyboardLayout::findLocalizedKey);
    }

    static String detect() {
        return KeyboardLayoutDetector.detect(
            localizedName(GLFW.GLFW_KEY_Q),
            localizedName(GLFW.GLFW_KEY_W),
            localizedName(GLFW.GLFW_KEY_Y)
        );
    }

    private static InputConstants.Key findLocalizedKey(String specification) {
        String requested = specification.substring(LOCALIZED_PREFIX.length()).toLowerCase(Locale.ROOT);
        for (int keyCode = GLFW.GLFW_KEY_SPACE; keyCode <= GLFW.GLFW_KEY_LAST; keyCode++) {
            String localized = localizedName(keyCode);
            if (localized != null && localized.equalsIgnoreCase(requested)) {
                return InputConstants.Type.KEYSYM.getOrCreate(keyCode);
            }
        }
        return InputConstants.UNKNOWN;
    }

    private static String localizedName(int keyCode) {
        int scanCode = GLFW.glfwGetKeyScancode(keyCode);
        return GLFW.glfwGetKeyName(keyCode, scanCode);
    }
}
