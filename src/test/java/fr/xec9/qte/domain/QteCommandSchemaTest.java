package fr.xec9.qte.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class QteCommandSchemaTest {
    @Test
    void qteRootRequiresOperatorPermissionSoEverySubcommandIsProtected() throws Exception {
        String commands = Files.readString(Path.of(
            "src/main/java/fr/xec9/qte/command/QteCommands.java"
        ));
        org.junit.jupiter.api.Assertions.assertTrue(
            commands.contains("Commands.literal(\"qte\")\n            .requires(source -> source.hasPermission(2))"),
            "the permission check must be on the /qte root, not only selected children"
        );
    }

    @Test
    void createInputArgumentIsPluralBecauseItAcceptsSequences() {
        assertEquals("inputs", QteCommandSchema.INPUTS_ARGUMENT);
        assertEquals("success_result", QteCommandSchema.SUCCESS_RESULT_ARGUMENT);
        assertEquals("failure_result", QteCommandSchema.FAILURE_RESULT_ARGUMENT);
        assertEquals("hide_hud", QteCommandSchema.HIDE_HUD_ARGUMENT);
        assertEquals("targets", QteCommandSchema.PLAY_TARGETS_ARGUMENT);
    }

    @Test
    void playCommandHasAnExplicitPlayerSelectorForCommandBlocks() throws Exception {
        String commands = Files.readString(Path.of(
            "src/main/java/fr/xec9/qte/command/QteCommands.java"
        ));
        org.junit.jupiter.api.Assertions.assertTrue(
            commands.contains("Commands.argument(QteCommandSchema.PLAY_TARGETS_ARGUMENT, EntityArgument.players())"),
            "command blocks need /qte play <id> <targets> without execute as"
        );
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
