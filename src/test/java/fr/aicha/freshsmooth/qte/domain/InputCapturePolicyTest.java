package fr.aicha.freshsmooth.qte.domain;

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
}
