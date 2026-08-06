# QTE Mechanics Consistency Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use systematic-debugging, test-driven-development, and verification-before-completion. Execute inline because this repository already contains uncommitted UI work.

**Goal:** Make command inputs intuitive, make timing success zones unmistakable, give aim/tracking real mouse mechanics, and make reward execution server-authoritative.

**Architecture:** Keep `QteDefinition` as persisted configuration. Add a pure normalized pointer model shared by client rendering and `QteJudge`; feed relative mouse movement through the existing mouse mixin. Replicate input intents to a server-side judge, while the client keeps a mirrored judge for immediate HUD feedback.

**Tech Stack:** Java 21, NeoForge 21.1.236, Brigadier, NeoForge payloads, JUnit 5.

## Global Constraints

- Preserve existing saved QTE definitions and old comma-separated command syntax.
- Keep all pointer coordinates finite and clamped to `[-1, 1]`.
- Client renders feedback; server alone executes reward commands.
- Add tests before every behavior change.

---

### Task 1: Input specification and command usability

**Files:**
- Modify: `src/test/java/fr/aicha/freshsmooth/qte/domain/QteDefinitionTest.java`
- Modify: `src/test/java/fr/aicha/freshsmooth/qte/domain/QteCommandSchemaTest.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/domain/QteDefinition.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/domain/QteCommandSchema.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/command/QteCommands.java`

- [ ] Test plural `inputs` schema and aliases `m1`, `mouse1`, `left_click`, `m2`, `right_click`.
- [ ] Run focused tests and confirm assertion failures.
- [ ] Rename the internal Brigadier field to `inputs`, switch it to `StringArgumentType.string()`, add CSV-aware suggestions, and normalize aliases.
- [ ] Run focused and full domain tests.

### Task 2: Editable definitions

**Files:**
- Modify: `src/test/java/fr/aicha/freshsmooth/qte/domain/QteRegistryTest.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/domain/QteRegistry.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/server/QteSavedData.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/command/QteCommands.java`
- Modify: `src/main/resources/assets/qte_engine/lang/en_us.json`
- Modify: `src/main/resources/assets/qte_engine/lang/fr_fr.json`

- [ ] Test replacement of an existing definition and rejection of a missing id.
- [ ] Run the focused registry test and confirm failure.
- [ ] Add `QteRegistry.replace`, dirty saved data after replacement, and `/qte edit` with the same arguments as `/qte create`.
- [ ] Add translated edited/missing feedback and run focused tests.

### Task 3: Pointer mechanics

**Files:**
- Create: `src/main/java/fr/aicha/freshsmooth/qte/domain/QtePointerModel.java`
- Create: `src/test/java/fr/aicha/freshsmooth/qte/domain/QtePointerModelTest.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/domain/QteInput.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/domain/QteJudge.java`
- Modify: `src/test/java/fr/aicha/freshsmooth/qte/domain/QteJudgeTest.java`

- [ ] Test pointer clamping, deterministic aim target, moving tracking target, aim click validation, and tracking hold/dwell.
- [ ] Run focused tests and confirm missing pointer behavior.
- [ ] Implement normalized relative movement and deterministic targets.
- [ ] Make `AIM` require pointer alignment plus configured press; make `TRACKING` require configured hold plus pointer dwell on a moving target.
- [ ] Keep `ANALOG_PRECISION` and `BALANCE` on the existing timing-axis strategy for compatibility.
- [ ] Run focused tests.

### Task 4: Mouse capture and authoritative input replication

**Files:**
- Create: `src/main/java/fr/aicha/freshsmooth/qte/network/QteInputPayload.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/network/QtePayloads.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/network/FinishQtePayload.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/client/QteClient.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/mixin/MouseHandlerMixin.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/server/QteSessions.java`
- Create: `src/test/java/fr/aicha/freshsmooth/qte/server/QteSessionAuthorityTest.java`

- [ ] Test that a claimed success cannot execute unless the server judge is successful.
- [ ] Run the test and confirm the current trust-based behavior fails the desired contract.
- [ ] Capture `MouseHandler.accumulatedDX/DY` before optional camera cancellation.
- [ ] Send at most one pointer payload per client tick and one payload per key transition.
- [ ] Store a server-side `QteJudge`; validate session id, input kind, key length, finite pointer values, and current elapsed time.
- [ ] Execute rewards only when the server judge reports `SUCCESS`; ignore the client success boolean.
- [ ] Run server authority and payload tests.

### Task 5: HUD clarity and distinct aim/tracking views

**Files:**
- Modify: `src/test/java/fr/aicha/freshsmooth/qte/client/QteHudModelTest.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/client/QteHudModel.java`
- Modify: `src/main/java/fr/aicha/freshsmooth/qte/client/QteHud.java`
- Modify: `src/main/resources/assets/qte_engine/lang/en_us.json`
- Modify: `src/main/resources/assets/qte_engine/lang/fr_fr.json`

- [ ] Test distinct `AIM`, `TRACKING`, and bar mechanic classifications plus a minimum timing success-band width.
- [ ] Run the focused test and confirm failure.
- [ ] Render timing zones with bright fill, dark boundary posts, and a translated `ZONE` label.
- [ ] Render aim as a 2D reticle/target field and tracking as a moving target with dwell meter and held-key indicator.
- [ ] Add non-color status glyphs and translated instructions.
- [ ] Run focused tests and compile.

### Task 6: Consistency audit and verification

**Files:**
- Modify: `README.md`

- [ ] Document plural inputs, mouse aliases, and exact aim/tracking rules.
- [ ] Verify all 17 types map to an explicit judge strategy and an explicit HUD mechanic.
- [ ] Run `clean build`, count test failures, validate resource JSON, and run `git diff --check`.
- [ ] Launch the client and inspect logs for mixin, payload, sprite, and render errors.
