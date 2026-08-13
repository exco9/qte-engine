package fr.xec9.qte.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InputCapturePolicyTest {
    @Test
    void blocksGameInputOnlyForActiveExclusiveQte() {
        assertTrue(InputCapturePolicy.blocksGameInput(true, true));
        assertFalse(InputCapturePolicy.blocksGameInput(true, false));
        assertFalse(InputCapturePolicy.blocksGameInput(false, true));
        assertFalse(InputCapturePolicy.blocksGameInput(false, false));
    }

    @Test
    void escapeAndPauseMenuRemainAvailableDuringExclusiveQte() {
        assertFalse(InputCapturePolicy.blocksKeyPress(true, true, 256));
        assertTrue(InputCapturePolicy.blocksKeyPress(true, true, 65));
        assertFalse(InputCapturePolicy.blocksGameInput(true, true, true));
        assertTrue(InputCapturePolicy.blocksGameInput(true, true, false));
    }
}
