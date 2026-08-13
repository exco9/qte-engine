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
Keep PNG transparency and crisp pixel edges. Keycap labels are rendered dynamically with Minecraft Five Bold and automatically scaled for labels such as `SPACE`, `SHIFT`, `CTRL`, and mouse buttons. The pressed label follows the lowered face of `qte_key_pressed.png`.

## Custom QTE image

Place an image at a resource path such as:

```text
assets/qte_custom/textures/gui/rune.png
```

Then use `qte_custom:textures/gui/rune.png` as the command's optional `texture` argument. Custom images are rendered compactly beside the prompt, so square textures work best.
