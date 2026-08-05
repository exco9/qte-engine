package fr.aicha.freshsmooth.qte.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class QteRegistryTest {
    @Test
    void createsFindsListsAndRemovesDefinitions() {
        QteRegistry registry = new QteRegistry();
        QteDefinition zebra = definition("zebra");
        QteDefinition alpha = definition("alpha");

        assertTrue(registry.add(zebra));
        assertTrue(registry.add(alpha));
        assertFalse(registry.add(zebra));
        assertEquals(zebra, registry.find("zebra").orElseThrow());
        assertEquals(List.of("alpha", "zebra"), registry.ids());
        assertTrue(registry.remove("zebra"));
        assertFalse(registry.remove("missing"));
    }

    private static QteDefinition definition(String id) {
        return QteDefinition.create(id, QteType.REACTION, "space", 2, "say ok", null);
    }
}
