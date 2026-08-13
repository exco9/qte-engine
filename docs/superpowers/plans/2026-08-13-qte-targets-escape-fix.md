# QTE Targets and Escape Safety Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make QTE playback safe from command blocks, add direct player targeting, and guarantee that exclusive input never captures Escape.

**Architecture:** Keep `/qte play <id>` as the self-targeting player form and add `/qte play <id> <targets>` using Minecraft's player selector argument. Move the Escape exception into the pure input-capture policy so the keyboard mixin can apply one tested rule before cancellation.

**Tech Stack:** Java 21, NeoForge 1.21.1, Brigadier, JUnit 5.

## Global Constraints

- All `/qte` commands remain permission-level 2.
- Command blocks and console must use an explicit target.
- Escape must always reach Minecraft while an exclusive QTE is active.
- Existing `/qte play <id>` behavior remains available to player command sources.

---

### Task 1: Escape-safe exclusive input

**Files:**
- Modify: `src/main/java/fr/xec9/qte/domain/InputCapturePolicy.java`
- Modify: `src/main/java/fr/xec9/qte/mixin/KeyboardHandlerMixin.java`
- Test: `src/test/java/fr/xec9/qte/domain/InputCapturePolicyTest.java`

**Interfaces:**
- Produces: `InputCapturePolicy.blocksKeyPress(boolean, boolean, int)` returning `false` for GLFW key 256 (Escape).

- [x] Add a failing test asserting active exclusive capture blocks ordinary keys but never Escape.
- [x] Run `test --tests fr.xec9.qte.domain.InputCapturePolicyTest` and confirm the missing method fails compilation.
- [x] Implement `blocksKeyPress` and use it in `KeyboardHandlerMixin.keyPress`.
- [x] Re-run the focused test and confirm it passes.

### Task 2: Direct target selection

**Files:**
- Modify: `src/main/java/fr/xec9/qte/command/QteCommands.java`
- Modify: `src/main/resources/assets/qte_engine/lang/en_us.json`
- Modify: `src/main/resources/assets/qte_engine/lang/fr_fr.json`
- Modify: `README.md`
- Test: `src/test/java/fr/xec9/qte/domain/QteCommandSchemaTest.java`

**Interfaces:**
- Produces: `/qte play <id> <targets>` where `targets` is `EntityArgument.players()`.
- Preserves: `/qte play <id>` self-targeting for a player source.

- [x] Add a failing command-schema regression test for the player-selector branch.
- [x] Run the focused command-schema test and confirm failure.
- [x] Add the selector branch and start one independent server session per selected player.
- [x] Return the number of targeted players and localize singular/self and multi-target feedback.
- [x] Document command-block examples such as `/qte play chase @a`.
- [x] Run the focused command-schema test and confirm it passes.

### Task 3: Verification and installation

**Files:**
- Modify: `gradle.properties`
- Build: `build/libs/qte_engine-0.4.7.jar`
- Install: `../../mods/qte_engine-0.4.7.jar`

**Interfaces:**
- Produces: verified version 0.4.7 active in the Prism instance.

- [x] Bump `mod_version` to `0.4.7`.
- [x] Run clean tests and full build with the configured JDK 21.
- [x] Confirm zero failures and inspect `git diff --check`.
- [x] Disable 0.4.6, install 0.4.7, and compare SHA-256 hashes.
