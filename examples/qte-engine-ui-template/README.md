# QTE Engine UI resource pack template

This folder is a ready-to-use Minecraft 1.21.1 resource pack. It replaces QTE Engine HUD sprites through standard resource locations; no Java changes are required.

## Installation

1. Copy `qte-engine-ui-template` into `.minecraft/resourcepacks/`.
2. Edit the PNG files in `assets/qte_engine/textures/gui/sprites/`.
3. Enable the pack above the default resources in Minecraft.
4. Press `F3 + T` after exporting changes.

The folder can also be distributed as a ZIP. Keep `pack.mcmeta` at the ZIP root.

## Sprites

- `qte_key.png`: released 32×32 keycap.
- `qte_key_pressed.png`: pressed 32×32 keycap.
- `qte_mouse_left.png`: M1 prompt.
- `qte_mouse_right.png`: M2 prompt.
- `qte_mouse_mb3.png`: M3 prompt.
- `qte_mouse_base.png`: neutral mouse template.

Keep PNG transparency and crisp pixel edges. Keyboard label font is selected in QTE Engine's client config. `SMALL_CAPS` uses Unicode small capitals through Minecraft's built-in font fallback, while `MINECRAFT_FIVE` uses QTE Engine's compact optional bitmap atlas and falls back to Minecraft's default font for unsupported characters. Long labels are automatically scaled to fit their keycaps. Mouse buttons 4 and above keep a text keycap because no dedicated icon identifies them.

Advanced resource packs can override `assets/qte_engine/font/qte_key_compact.json` for small caps and `assets/qte_engine/font/qte_key_minecraft_five.json` for Minecraft Five while keeping the same renderer.

## Custom QTE image

Place an image at a resource path such as:

```text
assets/qte_custom/textures/gui/rune.png
```

Then use `qte_custom:textures/gui/rune.png` as the command's optional `texture` argument. Custom images are rendered compactly beside the prompt, so square textures work best.
