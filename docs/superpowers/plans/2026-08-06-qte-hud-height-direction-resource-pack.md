# QTE HUD Height, Direction Removal, and Resource Pack Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move the QTE panel above vanilla survival HUD, remove duplicate `direction` type safely, and ship a working UI resource-pack template.

**Architecture:** Keep positioning in pure `QteHudModel`, migrate legacy `direction` definitions through `QteType.parse`, and let vanilla resource resolution override the existing `qte_engine` GUI sprite IDs. Template mirrors exact runtime paths and includes editable guidance.

**Tech Stack:** Java 21, NeoForge 1.21.1, JUnit 5, Minecraft resource pack format 34.

## Global Constraints

- Preserve current server-authoritative QTE results.
- Preserve old saved `direction` definitions by parsing them as `input_sequence`.
- Keep GUI sprites replaceable through normal Minecraft resource packs.
- Do not copy third-party mod assets.

---

### Task 1: Raise HUD anchor

**Files:**
- Modify: `src/test/java/fr/aicha/freshsmooth/qte/client/QteHudModelTest.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/client/QteHudModel.java`

**Interfaces:**
- Consumes: `QteHudModel.layout(int, int)`
- Produces: panel bottom positioned 52 GUI pixels above screen bottom

- [x] Change layout expectations to the raised coordinates.
- [x] Run focused test and confirm expected failure.
- [x] Increase bottom safe margin from 16 to 52.
- [x] Run focused test and confirm pass.

### Task 2: Remove duplicate direction type

**Files:**
- Modify: `src/test/java/fr/aicha/freshsmooth/qte/domain/QteTypeTest.java`
- Modify: `src/test/java/fr/aicha/freshsmooth/qte/domain/QteJudgeTest.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/domain/QteType.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/domain/QteDefinition.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/domain/QteJudge.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/client/QteHudModel.java`
- Modify: `src/main/resources/assets/qte_engine/lang/en_us.json`
- Modify: `src/main/resources/assets/qte_engine/lang/fr_fr.json`
- Modify: `README.md`

**Interfaces:**
- Consumes: legacy serialized type string `direction`
- Produces: `QteType.INPUT_SEQUENCE` migration and 13 advertised types

- [x] Add test requiring `direction` migration and 13 enum values; remove duplicate strategy assertion.
- [x] Run focused tests and confirm expected failure.
- [x] Remove enum and switch/set references; add parse alias.
- [x] Remove obsolete localization and documentation entries.
- [x] Run focused tests and confirm pass.

### Task 3: Add UI resource-pack template

**Files:**
- Create: `examples/QTE Engine UI Template/pack.mcmeta`
- Create: `examples/QTE Engine UI Template/README.md`
- Create: `examples/QTE Engine UI Template/assets/qte_engine/textures/gui/sprites/qte_hud_frame.png`
- Create: `examples/QTE Engine UI Template/assets/qte_engine/textures/gui/sprites/qte_hud_frame.png.mcmeta`
- Create: `examples/QTE Engine UI Template/assets/qte_engine/textures/gui/sprites/qte_keycap.png`
- Create: `examples/QTE Engine UI Template/assets/qte_engine/textures/gui/sprites/qte_keycap.png.mcmeta`
- Create: `examples/QTE Engine UI Template/assets/qte_engine/textures/gui/sprites/qte_marker.png`
- Modify: `README.md`

**Interfaces:**
- Consumes: Minecraft resource pack override order
- Produces: drop-in pack using exact `qte_engine` sprite resource paths

- [x] Add format-34 metadata and customization guide.
- [x] Copy current sprites and nine-slice metadata as safe starter assets.
- [x] Document install, zip structure, dimensions, and per-QTE custom texture path.
- [x] Validate JSON, PNG headers, resource paths, full tests, build, and diff whitespace.
