package fr.aicha.freshsmooth.qte.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class QteHudVisibilityTest {
    @Test
    void enforcesRequestedF1ModeAndRestoresPreviousState() {
        QteHudVisibility visibility = new QteHudVisibility(true);

        assertTrue(visibility.begin(false));
        assertTrue(visibility.enforce(false));
        assertFalse(visibility.restore(true));

        QteHudVisibility alreadyHidden = new QteHudVisibility(true);
        assertTrue(alreadyHidden.begin(true));
        assertTrue(alreadyHidden.restore(true));
    }

    @Test
    void leavesHudUntouchedWhenF1ModeWasNotRequested() {
        QteHudVisibility visibility = new QteHudVisibility(false);

        assertFalse(visibility.begin(false));
        assertTrue(visibility.enforce(true));
        assertTrue(visibility.restore(true));
    }
}
