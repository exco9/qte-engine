# QTE Failure Results and HUD Controls Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add authoritative failure commands and per-QTE HUD hiding while removing redundant types, matching pointer speed to Minecraft sensitivity, removing the keyboard label, and preventing chat/QTE overlap.

**Architecture:** Extend `QteDefinition` and saved data with an optional failure command plus `hideHud`, transmit only the presentation flag to the client, and let the existing server judge select the success or failure command. Keep removed type names as load-only aliases to canonical types so existing worlds remain readable. Put mouse scaling and chat-safe layout calculations in pure models covered by unit tests.

**Tech Stack:** Java 21, NeoForge 21.1.236 for Minecraft 1.21.1, Brigadier commands, NeoForge payload codecs, JUnit 5.

## Global Constraints

- Preserve all unrelated dirty-worktree changes.
- Server remains authoritative for success, failure, timeout, and command execution.
- `/qte create` and `/qte edit` use `<success_result> <failure_result> [exclusive_input] [hide_hud] [texture]`.
- `reaction`, `attention`, and `pattern` disappear from advertised types but migrate to `observation`, `observation`, and `input_sequence` when old data is loaded.
- F1 mode hides vanilla HUD only; the QTE overlay remains visible and the previous F1 state is restored afterward.

---

### Task 1: Canonical types and dual results

**Files:**
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/domain/QteType.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/domain/QteDefinition.java`
- Test: `src/test/java/fr/aicha/freshsmooth/qte/domain/QteTypeTest.java`
- Test: `src/test/java/fr/aicha/freshsmooth/qte/domain/QteDefinitionTest.java`

**Interfaces:**
- Produces: `QteDefinition.failureCommand()`, `QteDefinition.hideHud()`, and canonical legacy aliases from `QteType.parse(String)`.

- [ ] Write failing tests asserting 14 advertised enum values, legacy alias migration, normalized failure commands, and the HUD flag.
- [ ] Run focused domain tests and confirm failures reference missing fields or obsolete enum values.
- [ ] Remove the three redundant enum constants and extend the definition factory/record without breaking legacy factory overloads.
- [ ] Run focused domain tests and confirm they pass.

### Task 2: Command syntax, persistence, payload, and server outcome

**Files:**
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/command/QteCommands.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/server/QteSavedData.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/server/QteServerSession.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/server/QteSessions.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/network/StartQtePayload.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/network/QtePayloads.java`
- Test: `src/test/java/fr/aicha/freshsmooth/qte/server/QteServerSessionTest.java`

**Interfaces:**
- Consumes: `failureCommand()` and `hideHud()` from Task 1.
- Produces: authoritative `Optional<QteStatus> finish(...)` and client `StartQtePayload.hideHud()`.

- [ ] Write failing server-session tests for authoritative failure and timeout outcomes.
- [ ] Run the focused server tests and confirm the current boolean-only finish API fails the new assertions.
- [ ] Make session completion return a terminal server status, execute the matching command as the player source, and retain the session on premature forged finish packets.
- [ ] Add failure/HUD fields to commands and NBT, default missing legacy fields safely, extend the start payload, and bump network protocol to `4`.
- [ ] Run the focused tests and compile production sources.

### Task 3: Sensitivity-aware pointer motion

**Files:**
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/domain/QtePointerModel.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/client/QteClient.java`
- Test: `src/test/java/fr/aicha/freshsmooth/qte/domain/QtePointerModelTest.java`

**Interfaces:**
- Produces: `move(Point, double, double, double, boolean)` using Minecraft's cubic sensitivity curve and inverted-Y preference.

- [ ] Write failing tests for low/default/high sensitivity and inverted Y.
- [ ] Run the pointer test and confirm the requested overload is absent.
- [ ] Apply Minecraft's `(s * 0.6 + 0.2)^3 * 8` curve with a lower calibrated base scale and pass live client options into the model.
- [ ] Run the pointer tests and confirm they pass.

### Task 4: HUD lifecycle and chat-safe layout

**Files:**
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/client/QteClient.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/client/QteHud.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/client/QteHudModel.java`
- Test: `src/test/java/fr/aicha/freshsmooth/qte/client/QteHudModelTest.java`
- Modify: `src/main/resources/assets/qte_engine/lang/en_us.json`
- Modify: `src/main/resources/assets/qte_engine/lang/fr_fr.json`

**Interfaces:**
- Consumes: `StartQtePayload.hideHud()` from Task 2.
- Produces: `QteHudModel.chatBottom(...)`, a shorter footer, and reversible vanilla `Options.hideGui` ownership.

- [ ] Write failing layout tests asserting the compact height and chat bottom above the panel.
- [ ] Run focused HUD-model tests and confirm the old 92-pixel layout fails.
- [ ] Remove the keyboard label, compact the footer, reposition chat through `CustomizeGuiOverlayEvent.Chat`, enforce F1 during active QTE, and restore the previous state on replacement, disconnect, or linger completion.
- [ ] Run focused tests and compile.

### Task 5: Documentation and verification

**Files:**
- Modify: `README.md`
- Read: `C:/Users/aicha/.codex/skills/neoforge-ui-designer/references/qa-checklist.md`

**Interfaces:**
- Documents the exact new command grammar, migrations, pointer behavior, failure authority, chat behavior, and F1 option.

- [ ] Update examples and type documentation with success/failure commands and `[exclusive_input] [hide_hud] [texture]`.
- [ ] Run `test build`, validate JSON, run `git diff --check`, and inspect the complete diff.
- [ ] Launch dedicated server and development client when no user-owned process must be interrupted; otherwise report that runtime relaunch is required for the already-running client.
