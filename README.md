<img width="850" height="85" alt="logo qte mod (1)" src="https://github.com/user-attachments/assets/a1a0c6aa-d102-42d9-971c-5eab49acfdb0" />


A NeoForge 1.21.1 mod for server-authoritative quick-time events with persistent definitions and a configurable HUD.

## Commands

```text
/qte create <id> <type> <inputs> <duration> <success_command> <failure_command> [exclusive_input] [hide_hud] [texture]
/qte edit <id> <type> <inputs> <duration> <success_command> <failure_command> [exclusive_input] [hide_hud] [texture]
/qte play <id>
/qte play <id> <targets>
/qte settings <id> tracking_speed <0.1..2.0>
/qte settings <id> aim_position <-0.92..0.92> <-0.92..0.92>
/qte settings <id> aim_random
/qte remove <id>
/qte list
/qte types
```

Every `/qte` command requires operator permission level 2. A player can use `/qte play <id>` on themselves. Consoles and command blocks use `/qte play <id> <targets>` with a player name or selector such as `@a`, `@p`, or `@r`. Duration is expressed in seconds and must be between `0.1` and `300`.

Commands containing spaces must be quoted. A leading `/` is optional inside result commands. Both `@s` and `%player%` target the player running the QTE.

- `exclusive_input`: blocks normal gameplay keyboard and mouse handling while the QTE is active. Escape and the pause menu always remain usable so the player can leave safely. Default: `false`.
- `hide_hud`: temporarily hides the vanilla HUD while keeping the QTE visible. Default: `false`.
- `texture`: optional resource location for a 40×40 QTE image. Both boolean arguments must be provided before it.

## Examples

<img width="406" height="116" alt="uiqteengine" src="https://github.com/user-attachments/assets/ba59518f-9a29-4ffa-be6a-3b5b1cfc0980" />

```mcfunction
/qte create 1 hold space 2.5 "say @s succeed" "say @s failed" true true
/qte create rune input_sequence "w,a,s,d" 6 "say @s succeed" "say @s failed" true false qte_engine:textures/gui/rune.png
/qte play 1
/qte play rune @a
/qte settings chase tracking_speed 0.35
/qte settings target aim_position -0.5 0.25
```

Tracking defaults to `0.45`; `1.0` matches the former movement rate. Aim coordinates are normalized screen positions: negative X/Y moves left/up, positive X/Y moves right/down. `aim_random` restores a new deterministic position for every play session.

Inputs accept short names such as `space`, `w`, and `left_shift`, or complete Minecraft identifiers such as `key.keyboard.space` and `key.mouse.left`. Mouse aliases include `m1`, `m2`, `m3`, `mouse1`, `mouse2`, and `mouse3`.

Separate multiple inputs with commas. `input_sequence` and `reaction_choice` require at least two inputs; every other type accepts exactly one. Single-letter inputs follow the player's keyboard layout.

## QTE types

- `observation`: press the expected input before time expires.
- `reaction_choice`: react to the displayed choices; the expected choice is the first configured input.
- `hold`: hold the configured input for 60% of the duration.
- `mash`: press repeatedly until the target is reached.
- `input_sequence`: enter every input in order.
- `balance`: press while the rotating needle crosses the session-randomized success arc.
- `aim`: move the circular cursor into a session-randomized target anywhere in the safe screen area, then press.
- `tracking`: hold the configured input while following a session-randomized moving circle; successful tracking progressively fills it.

Success, failure, and timeout results are validated and executed by the server.

## UI textures

Every QTE exposes a smooth radial countdown that starts at 12 o'clock and shrinks clockwise with frame interpolation. Simple QTEs use a compact 32×32 keycap; hold and mash add an inner progress ring; balance uses a circular skill-check dial; aim and tracking use circular full-screen targets.

The keycap sprites can be replaced through a normal Minecraft resource pack:

- `qte_key.png`: released keycap.
- `qte_key_pressed.png`: pressed keycap.
- `qte_mouse_left.png`, `qte_mouse_right.png`, and `qte_mouse_mb3.png`: dedicated M1, M2, and M3 prompts.
- `qte_mouse_base.png`: neutral 32×32 mouse template.
A mouse prompt slowly crossfades between its neutral and highlighted textures; pressing the requested button makes its highlight fully visible.
A ready-to-edit template is available in [`examples/qte-engine-ui-template`](examples/qte-engine-ui-template/). Key labels use the replaceable `qte_engine:textures/font/ascii.png` bitmap atlas: 128×128 pixels, arranged as 16×16 cells of 8×8 pixels. A resource pack can replace both `assets/qte_engine/textures/font/ascii.png` and `assets/qte_engine/font/qte_key_compact.json` to support another character set. Long key names are still scaled to remain inside the keycap. Aim and tracking update their visual pointer on every mouse frame while bounded samples remain server-validated.

Custom QTE images use full resource locations such as `my_pack:textures/gui/rune.png`.

## Build

Java 21 is required.

```powershell
.\gradlew.bat clean test build
```

The built JAR is written to `build/libs/qte_engine-0.4.22.jar`.
