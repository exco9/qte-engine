package fr.xec9.qte.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class KeyboardLayoutDetectorTest {
    @Test
    void identifiesCommonLatinKeyboardLayoutsFromPhysicalKeyLabels() {
        assertEquals("AZERTY", KeyboardLayoutDetector.detect("a", "z", "y"));
        assertEquals("QWERTZ", KeyboardLayoutDetector.detect("q", "w", "z"));
        assertEquals("QWERTY", KeyboardLayoutDetector.detect("q", "w", "y"));
        assertEquals("CUSTOM", KeyboardLayoutDetector.detect(null, null, null));
    }
}
