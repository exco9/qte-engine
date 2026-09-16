package fr.xec9.qte.client;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.TranslatableEnum;

final class QteClientConfig {
    static final ModConfigSpec SPEC;
    private static final ModConfigSpec.EnumValue<KeyFont> KEY_FONT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        KEY_FONT = builder
            .comment("Font used for keyboard labels in QTE prompts.")
            .translation("qte_engine.configuration.key_font")
            .defineEnum("key_font", KeyFont.SMALL_CAPS);
        SPEC = builder.build();
    }

    private QteClientConfig() {}

    static KeyFont keyFont() {
        return KEY_FONT.get();
    }

    enum KeyFont implements TranslatableEnum {
        SMALL_CAPS("qte_engine.configuration.key_font.small_caps"),
        MINECRAFT_FIVE("qte_engine.configuration.key_font.minecraft_five");

        private final String translationKey;

        KeyFont(String translationKey) {
            this.translationKey = translationKey;
        }

        @Override
        public Component getTranslatedName() {
            return Component.translatable(translationKey);
        }
    }
}
