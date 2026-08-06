# QTE HUD Redesign Implementation Plan

> **For Codex:** Execute this plan test-first. Keep QTE judgment and networking unchanged; all new behavior is client presentation.

**Goal:** Replace the generic fixed rectangle with a distinctive, responsive NeoForge 1.21.1 HUD that gives every QTE type a clear action, readable timing, and strong success/failure feedback.

**Architecture:** Add a pure Java presentation model (`QteHudModel`) that classifies mechanics, clamps layout, formats keys, and computes urgency. Keep Minecraft rendering in `QteHud`, driven by this model and existing authoritative `QteJudge` state. Use small Aseprite-authored sprite textures for the frame and keycaps; draw dynamic meters in code.

**Tech Stack:** Java 21, NeoForge 21.1.236 / Minecraft 1.21.1, JUnit 5, Aseprite MCP.

---

## Experience brief

- **Player goal:** understand the required input in under one second, execute it, and immediately understand the result.
- **Information hierarchy:** required action → current key/target → remaining time/progress → QTE family and keyboard layout.
- **States:** active, urgent (last 25%), success, failure/timeout, memory-hidden, optional custom illustration.
- **Interaction:** keyboard/mouse input remains captured by the existing client session; HUD never steals focus and never predicts server results.
- **Accessibility:** no meaning is encoded by color alone; status uses text and glyphs, key labels have strong contrast, meters remain legible at GUI scale 2–4.
- **Responsive rule:** centered bottom HUD, width clamped to 216–320 px, safe margin 16 px, optional illustration only when enough width exists.

## Visual directions considered

1. **Signal Rail — selected.** A compact dark instrument panel, cyan signal edge, amber countdown, inset keycaps, and a segmented lower rail. Best fit for repeated, high-tempo QTEs and all 17 mechanic types.
2. **Cinematic Prompt.** One oversized floating key and radial countdown. Strong for reaction QTEs, weak for sequences and precision.
3. **Tactical Console.** Dense modular readouts and labels. Flexible, but visually heavy for frequent gameplay.

Identity words: **pulse, sequence, signal**. Desired feel: **precise, kinetic, urgent**.

## Task 1: Lock presentation behavior with failing tests

**Files:**
- Create: `src/test/java/fr/aicha/freshsmooth/qte/client/QteHudModelTest.java`
- Create: `src/main/java/fr/aicha/freshsmooth/qte/client/QteHudModel.java`

1. Write tests for width clamping at narrow, normal, and wide GUI sizes.
2. Write tests for mechanic classification: timing, precision, sequence, hold, mash, single.
3. Write tests for key-label normalization, including localized keyboard and mouse inputs.
4. Write tests for progress clamping and urgency thresholds.
5. Run the focused test and confirm it fails because `QteHudModel` does not exist.
6. Implement only enough pure Java model code to pass.
7. Run the focused test and full suite.

## Task 2: Build Aseprite source assets

**Files:**
- Create: `art/ui/qte_hud.aseprite`
- Create: `src/main/resources/assets/qte_engine/textures/gui/qte_hud_frame.png`
- Create: `src/main/resources/assets/qte_engine/textures/gui/qte_keycap.png`
- Create: `src/main/resources/assets/qte_engine/textures/gui/qte_marker.png`

1. Use Aseprite MCP to create a compact pixel-art palette: ink `#101820`, panel `#172630`, edge `#29414C`, signal `#52D6C8`, warning `#FFBE55`, success `#74DF8B`, failure `#FF6577`, text `#EAF7F5`.
2. Draw the frame corners/edge motifs, keycap, and timing marker on separate layers.
3. Preserve an editable `.aseprite` source under `art/ui`.
4. Export deterministic PNG resources with nearest-neighbor pixel edges.
5. Inspect the exported PNGs for transparency and dimensions.

## Task 3: Replace the HUD renderer

**Files:**
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/client/QteHud.java`
- Modify: `src/main/resources/assets/qte_engine/lang/en_us.json`
- Modify: `src/main/resources/assets/qte_engine/lang/fr_fr.json`

1. Render the responsive Signal Rail frame from the new texture.
2. Add a compact header: mechanic glyph/label, keyboard-layout badge, numeric countdown.
3. Render mechanic-specific center stages:
   - timing/dialogue timing: target band and moving marker;
   - analog/aim/tracking/balance: centered precision band and live marker;
   - sequence/direction/pattern/rhythm/memory: individual keycaps, completed/current/pending states;
   - hold: keycap plus hold-fill rail;
   - mash: keycap plus press counter/segmented fill;
   - reaction/choice/observation/attention: prominent current keycaps.
4. Add urgency pulses based on remaining fraction, without changing input logic.
5. Add explicit success and failure plates with icons and translated action hints.
6. Correct the corrupted French UTF-8 strings while extending translations.
7. Keep optional user texture support, cropped into a framed 48×48 area only when space permits.

## Task 4: Verification

**Files:**
- Modify if needed: `README.md`

1. Run `QteHudModelTest`, then all tests via the Gradle wrapper main class (the checked-in batch script mishandles `&` in the parent path).
2. Run `build` to validate resource processing and NeoForge compilation.
3. Validate PNG dimensions/transparency and ensure the editable `.aseprite` source opens through MCP inspection.
4. Inspect the final diff for accidental domain/network changes.
5. Document the new HUD and asset source location in README if the public usage surface changed.
