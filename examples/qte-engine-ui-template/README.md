# QTE Engine UI resource pack template

This folder is a ready-to-use Minecraft 1.21.1 resource pack. It replaces QTE Engine HUD sprites through standard resource locations; no Java changes are required.

## Installation

1. Copy `qte-engine-ui-template` into `.minecraft/resourcepacks/`.
2. Edit the PNG files in `assets/qte_engine/textures/gui/sprites/`.
3. Enable the pack above the default resources in Minecraft.
4. Press `F3 + T` after exporting changes.

The folder can also be distributed as a ZIP. Keep `pack.mcmeta` at the ZIP root.

## Sprites

- `qte_hud_frame.png`: 16×16 resizable frame. Keep corners and borders inside the outer 4 pixels.
- `qte_keycap.png`: 12×12 resizable keycap. Keep corners and borders inside the outer 4 pixels.
- `qte_marker.png`: fixed 5×11 marker.

Keep PNG transparency and crisp pixel edges. The matching `.png.mcmeta` files define nine-slice scaling. Editable files are stored in `sources/*.aseprite`; export each PNG to its matching path under `assets/`.

## Custom QTE image

Place an image at a resource path such as:

```text
assets/qte_custom/textures/gui/rune.png
```

Then use `qte_custom:textures/gui/rune.png` as the command's optional `texture` argument. Custom images are rendered at 40×40, so square textures work best.
