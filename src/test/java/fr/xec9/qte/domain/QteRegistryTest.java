package fr.xec9.qte.domain;

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

    @Test
    void editReplacesExistingDefinitionButNeverCreatesMissingId() {
        QteRegistry registry = new QteRegistry();
        QteDefinition original = definition("editable");
        QteDefinition edited = QteDefinition.create(
            "editable",
            QteType.INPUT_SEQUENCE,
            "w,a,s,d",
            5,
            "say edited",
            true,
            null
        );

        assertTrue(registry.add(original));
        assertTrue(registry.replace(edited));
        assertEquals(edited, registry.find("editable").orElseThrow());
        assertFalse(registry.replace(QteDefinition.create(
            "missing",
            QteType.OBSERVATION,
            "space",
            2,
            "say missing",
            null
        )));
    }

    private static QteDefinition definition(String id) {
        return QteDefinition.create(id, QteType.OBSERVATION, "space", 2, "say ok", null);
    }
}
