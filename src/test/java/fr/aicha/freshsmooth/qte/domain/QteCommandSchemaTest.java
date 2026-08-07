package fr.aicha.freshsmooth.qte.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class QteCommandSchemaTest {
    @Test
    void createInputArgumentIsPluralBecauseItAcceptsSequences() {
        assertEquals("inputs", QteCommandSchema.INPUTS_ARGUMENT);
        assertEquals("success_result", QteCommandSchema.SUCCESS_RESULT_ARGUMENT);
        assertEquals("failure_result", QteCommandSchema.FAILURE_RESULT_ARGUMENT);
        assertEquals("hide_hud", QteCommandSchema.HIDE_HUD_ARGUMENT);
    }

    @Test
    void suggestionsContinueAfterEachCommaAndIncludeMouseButtons() {
        assertEquals(
            List.of("w,a,mouse.left", "w,a,mouse.middle", "w,a,mouse.right"),
            QteCommandSchema.inputSuggestions("w,a,mou")
        );
        assertEquals(List.of("mouse.left", "mouse.middle", "mouse.right"), QteCommandSchema.inputSuggestions("mou"));
    }
}
