# QTE HUD Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the large QTE panel with a compact keycap-centered HUD, smooth radial countdown, subtle entry/result feedback, and compact specialized mechanics.

**Architecture:** Keep server, networking, and QTE judging unchanged. Extend the pure client HUD model for layout and animation math, extract reusable keycap/radial rendering to `QteKeyPromptRenderer`, and leave `QteHud` responsible only for mechanic routing and specialized minimal displays.

**Tech Stack:** Java 21, Minecraft 1.21.1 rendering APIs, NeoForge 21.1.236, JUnit 5, supplied 32×32 PNG sprites.

## Global Constraints

- Use supplied `Key.png` and `Key_pressed.png` unchanged.
- Store sprites under `assets/qte_engine/textures/gui/sprites/` and mirror them in `examples/qte-engine-ui-template`.
- No new Screen, menu, shader, server/network change, or required EaseGUI/GUI Tween dependency.
- Timer starts at 12 o'clock, empties clockwise, and interpolates between client ticks.
- Default simple prompt is 64×64 with 32×32 keycap, centered above hotbar.

---

### Task 1: Pure HUD model and animation contract

**Files:**
- Modify: `src/test/java/fr/xec9/qte/client/QteHudModelTest.java`
- Modify: `src/main/java/fr/xec9/qte/client/QteHudModel.java`

**Interfaces:**
- Produces: compact `Layout`, `remainingFraction(elapsed,duration,partialTick)`, `easeOutCubic(progress)`, `entryScale(progress)`, `entryAlpha(progress)`, and feedback transforms.

- [ ] Write tests for 64px prompt layout, safe bottom anchoring, interpolated countdown, easing endpoints, and result feedback.
- [ ] Run `./gradlew test --tests '*QteHudModelTest'`; expect failures against old 216–320px panel model.
- [ ] Implement minimal pure math and rerun until green.

### Task 2: Prompt renderer and mechanic-specific compact HUD

**Files:**
- Create: `src/main/java/fr/xec9/qte/client/QteKeyPromptRenderer.java`
- Modify: `src/main/java/fr/xec9/qte/client/QteHud.java`
- Modify: `src/main/java/fr/xec9/qte/client/QteClient.java`
- Create: `src/main/resources/assets/qte_engine/textures/gui/sprites/qte_key.png`
- Create: `src/main/resources/assets/qte_engine/textures/gui/sprites/qte_key_pressed.png`

**Interfaces:**
- Consumes: `ClientSession`, `GuiGraphics`, interpolated frame time.
- Produces: keycap rendering, triangle-strip radial ring, entry transform, success/failure feedback, compact sequence/hold/mash prompt.

- [ ] Copy supplied PNG bytes unchanged and verify hashes match originals.
- [ ] Render smooth ring using tessellated quads with enough angular segments for clean 3–5px stroke.
- [ ] Scale/fade in over 125ms using local cubic ease-out; interpolate countdown with `DeltaTracker.getGameTimeDeltaPartialTick(false)`.
- [ ] Route SINGLE/HOLD/MASH through central prompt, SEQUENCE through compact small keycaps, and retain minimal bars/fields for TIMING/PRECISION/AIM/TRACKING.
- [ ] Remove obsolete frame/header/footer and chat displacement.

### Task 3: Template, documentation, build, and install

**Files:**
- Modify: `examples/qte-engine-ui-template/README.md`
- Create: `examples/qte-engine-ui-template/assets/qte_engine/textures/gui/sprites/qte_key.png`
- Create: `examples/qte-engine-ui-template/assets/qte_engine/textures/gui/sprites/qte_key_pressed.png`
- Modify: `README.md`
- Modify: `gradle.properties`

**Interfaces:**
- Produces: resource-pack override documentation and verified release JAR.

- [ ] Document new sprite names, sizes, replacement behavior, animation implementation, and no EaseGUI/GUI Tween dependency.
- [ ] Run `./gradlew clean test build`; expect `BUILD SUCCESSFUL` and zero test failures.
- [ ] Inspect JAR entries, install versioned JAR into instance, remove only prior QTE Engine JAR, and compare SHA-256.

## Self-Review

- Coverage: simple, hold, mash, sequence, timing, precision, aim, tracking, sprites, interpolation, feedback, template, and build validation included.
- Scope: server/network/domain behavior excluded.
- Placeholder scan: no TBD/TODO/later entries.
