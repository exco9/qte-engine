# QTE Engine

A NeoForge 1.21.1 mod for server-authoritative quick-time events with persistent definitions and a configurable HUD.

## Commands

```text
/qte create <id> <type> <inputs> <duration> <success_command> <failure_command> [exclusive_input] [hide_hud] [texture]
/qte edit <id> <type> <inputs> <duration> <success_command> <failure_command> [exclusive_input] [hide_hud] [texture]
/qte play <id>
/qte remove <id>
/qte list
/qte types
```

`create`, `edit`, and `remove` require operator permission level 2. `play` must be run by a player. Duration is expressed in seconds and must be between `0.1` and `300`.

Commands containing spaces must be quoted. A leading `/` is optional inside result commands. Both `@s` and `%player%` target the player running the QTE.

- `exclusive_input`: blocks normal keyboard and mouse handling while the QTE is active. Default: `false`.
- `hide_hud`: temporarily hides the vanilla HUD while keeping the QTE visible. Default: `false`.
- `texture`: optional resource location for a 40×40 QTE image. Both boolean arguments must be provided before it.

## Examples

<img width="406" height="116" alt="uiqteengine" src="https://github.com/user-attachments/assets/ba59518f-9a29-4ffa-be6a-3b5b1cfc0980" />

```mcfunction
/qte create 1 hold space 2.5 "say @s succeed" "say @s failed" true true
/qte create rune input_sequence "w,a,s,d" 6 "say @s succeed" "say @s failed" true false qte_engine:textures/gui/rune.png
/qte play 1
```

Inputs accept short names such as `space`, `w`, and `left_shift`, or complete Minecraft identifiers such as `key.keyboard.space` and `key.mouse.left`. Mouse aliases include `m1`, `m2`, `m3`, `mouse1`, `mouse2`, and `mouse3`.

Separate multiple inputs with commas. `input_sequence`, `reaction_choice`, `memory`, and `rhythm` require at least two inputs; every other type accepts exactly one. Single-letter inputs follow the player's keyboard layout.

## QTE types

- `observation`: press the expected input before time expires.
- `reaction_choice`: select the first input from the displayed choices. (WIP)
- `hold`: hold the configured input for 60% of the duration.
- `mash`: press repeatedly until the target is reached.
- `input_sequence`: enter every input in order.
- `balance`: press when the moving marker reaches the center.
- `aim`: move the cursor into the fixed target, then press the configured input.
- `tracking`: hold the configured input while following the moving target.

Success, failure, and timeout results are validated and executed by the server.

## UI textures

The HUD sprites can be replaced through a normal Minecraft resource pack. A ready-to-edit template with PNG and Aseprite sources is available in [`examples/qte-engine-ui-template`](examples/qte-engine-ui-template/).

Custom QTE images use full resource locations such as `my_pack:textures/gui/rune.png`.

## Build

Java 21 is required.

```powershell
.\gradlew.bat clean test build
```

The built JAR is written to `build/libs/qte_engine-0.3.0.jar`.
