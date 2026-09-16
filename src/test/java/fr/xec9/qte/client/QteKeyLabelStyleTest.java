package fr.xec9.qte.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class QteKeyLabelStyleTest {
    @Test
    void smallCapsModeKeepsNativeSmallCapLabels() {
        assertEquals(
            "ꜱᴘᴀᴄᴇ",
            QteKeyLabelStyle.forFont("ꜱᴘᴀᴄᴇ", QteClientConfig.KeyFont.SMALL_CAPS)
        );
    }

    @Test
    void minecraftFiveModeRestoresRegularCapitalLabels() {
        assertEquals(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
            QteKeyLabelStyle.forFont(
                QteHudModel.smallCaps("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
                QteClientConfig.KeyFont.MINECRAFT_FIVE
            )
        );
        assertEquals(
            "BUTTON 4",
            QteKeyLabelStyle.forFont("ʙᴜᴛᴛᴏɴ 4", QteClientConfig.KeyFont.MINECRAFT_FIVE)
        );
    }

    @Test
    void mouseSpriteLabelsStayUnchangedInBothModes() {
        for (QteClientConfig.KeyFont font : QteClientConfig.KeyFont.values()) {
            assertEquals("M1", QteKeyLabelStyle.forFont("M1", font));
            assertEquals("M2", QteKeyLabelStyle.forFont("M2", font));
            assertEquals("M3", QteKeyLabelStyle.forFont("M3", font));
        }
    }
}
