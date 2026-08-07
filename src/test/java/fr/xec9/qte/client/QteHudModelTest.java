package fr.xec9.qte.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.xec9.qte.domain.QteType;
import org.junit.jupiter.api.Test;

class QteHudModelTest {
    @Test
    void clampsPanelWidthAndCentersItInsideSafeMargins() {
        assertEquals(new QteHudModel.Layout(12, 84, 216, 84), QteHudModel.layout(240, 220));
        assertEquals(new QteHudModel.Layout(16, 144, 288, 84), QteHudModel.layout(320, 280));
        assertEquals(new QteHudModel.Layout(240, 300, 320, 84), QteHudModel.layout(800, 436));
    }

    @Test
    void anchorsChatAboveTheQtePanel() {
        assertEquals(80, QteHudModel.chatBottom(240, 220));
        assertEquals(140, QteHudModel.chatBottom(320, 280));
    }

    @Test
    void classifiesEveryQteIntoItsVisualMechanic() {
        assertEquals(QteHudModel.Mechanic.TIMING, QteHudModel.mechanic(QteType.TIMING));
        assertEquals(QteHudModel.Mechanic.TIMING, QteHudModel.mechanic(QteType.DIALOGUE_TIMING));
        assertEquals(QteHudModel.Mechanic.PRECISION, QteHudModel.mechanic(QteType.BALANCE));
        assertEquals(QteHudModel.Mechanic.AIM, QteHudModel.mechanic(QteType.AIM));
        assertEquals(QteHudModel.Mechanic.TRACKING, QteHudModel.mechanic(QteType.TRACKING));
        assertEquals(QteHudModel.Mechanic.SEQUENCE, QteHudModel.mechanic(QteType.MEMORY));
        assertEquals(QteHudModel.Mechanic.SEQUENCE, QteHudModel.mechanic(QteType.RHYTHM));
        assertEquals(QteHudModel.Mechanic.HOLD, QteHudModel.mechanic(QteType.HOLD));
        assertEquals(QteHudModel.Mechanic.MASH, QteHudModel.mechanic(QteType.MASH));
        assertEquals(QteHudModel.Mechanic.SINGLE, QteHudModel.mechanic(QteType.REACTION_CHOICE));
    }

    @Test
    void createsCompactReadableKeyLabels() {
        assertEquals("Z", QteHudModel.keyLabel("key.localized.z"));
        assertEquals("SPACE", QteHudModel.keyLabel("key.keyboard.space"));
        assertEquals("M1", QteHudModel.keyLabel("key.mouse.left"));
        assertEquals("M2", QteHudModel.keyLabel("key.mouse.right"));
        assertEquals("M3", QteHudModel.keyLabel("key.mouse.middle"));
        assertEquals("BUTTON 4", QteHudModel.keyLabel("key.mouse.4"));
    }

    @Test
    void clampsProgressAndSeparatesUrgencyFromExpiration() {
        assertEquals(0.0, QteHudModel.clampProgress(-0.2));
        assertEquals(0.42, QteHudModel.clampProgress(0.42));
        assertEquals(1.0, QteHudModel.clampProgress(1.4));
        assertEquals(QteHudModel.Urgency.NORMAL, QteHudModel.urgency(0.26));
        assertEquals(QteHudModel.Urgency.URGENT, QteHudModel.urgency(0.25));
        assertEquals(QteHudModel.Urgency.EXPIRED, QteHudModel.urgency(0.0));
    }

    @Test
    void timingSuccessBandMatchesJudgeWindowAndNeverBecomesTooThin() {
        assertEquals(new QteHudModel.Band(58, 82), QteHudModel.timingSuccessBand(100));
        QteHudModel.Band compact = QteHudModel.timingSuccessBand(20);
        assertEquals(8, compact.width());
        assertEquals(14, compact.center());
    }
}
