package fr.aicha.freshsmooth.qte.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class QteCommandSchemaTest {
    @Test
    void createInputArgumentIsCalledKey() {
        assertEquals("key", QteCommandSchema.KEY_ARGUMENT);
    }
}
