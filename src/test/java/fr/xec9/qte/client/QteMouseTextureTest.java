package fr.xec9.qte.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class QteMouseTextureTest {
    @Test
    void mousePromptSpritesUseNativeKeycapDimensions() throws Exception {
        for (String name : new String[] {
            "qte_mouse_left", "qte_mouse_right", "qte_mouse_mb3", "qte_mouse_base"
        }) {
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream(
                "assets/qte_engine/textures/gui/sprites/" + name + ".png"
            )) {
                assertNotNull(stream, name + " must be bundled");
                BufferedImage image = ImageIO.read(stream);
                assertNotNull(image, name + " must be a readable PNG");
                assertEquals(32, image.getWidth());
                assertEquals(32, image.getHeight());
            }
        }
    }
}
