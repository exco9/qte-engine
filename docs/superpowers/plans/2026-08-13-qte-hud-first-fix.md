# QTE HUD First Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Corriger le HUD QTE, limiter le moteur aux huit types demandés et reconstruire balance, aim et tracking autour de widgets circulaires lisibles.

**Architecture:** Le domaine expose uniquement les types supportés et utilise une graine de session identique côté serveur/client pour les positions. Les calculs de phase, cible, placement écran et progression restent dans des modèles purs testables ; les classes client ne font que transformer ces valeurs en géométrie GUI. Le HUD devient une GUI layer NeoForge enregistrée au-dessus du chat, avec un rendu supplémentaire au-dessus de `ChatScreen`.

**Tech Stack:** Java 21, Minecraft 1.21.1, NeoForge 21.1.228, JUnit 5, ressources JSON/OTF Minecraft.

## Global Constraints

- Types conservés uniquement : `observation`, `reaction_choice`, `hold`, `mash`, `input_sequence`, `balance`, `aim`, `tracking`.
- Ne pas ajouter de dépendance runtime.
- Garder les résultats et la réussite autoritaires côté serveur.
- Utiliser Minecraft Five Bold sous Open Font License et inclure la licence.
- Garder les textures de touches remplaçables par resource pack.
- Préserver clavier, souris, disposition AZERTY/QWERTY, GUI scale et redimensionnement.

---

### Task 1: Restrict the QTE domain

**Files:**
- Modify: `src/main/java/fr/xec9/qte/domain/QteType.java`
- Modify: `src/main/java/fr/xec9/qte/domain/QteDefinition.java`
- Modify: `src/main/java/fr/xec9/qte/domain/QteJudge.java`
- Modify: `src/test/java/fr/xec9/qte/domain/QteTypeTest.java`
- Modify: `src/test/java/fr/xec9/qte/domain/QteJudgeTest.java`

**Interfaces:**
- Produces: `QteType.values()` contenant exactement les huit types autorisés.
- Produces: `QteJudge.strategy(QteType)` sans anciens chemins timing/precision/rhythm.

- [ ] **Step 1: Write failing type and strategy tests**

Tester la liste exacte des huit noms, le rejet de `timing`, `memory` et `analog_precision`, puis les stratégies des huit types.

- [ ] **Step 2: Run focused tests and verify RED**

Run: `./gradlew test --tests fr.xec9.qte.domain.QteTypeTest --tests fr.xec9.qte.domain.QteJudgeTest`

Expected: FAIL car treize types existent encore.

- [ ] **Step 3: Remove unsupported enum values and dead judge branches**

Supprimer les cinq valeurs et leurs alias/branches, tout en laissant `QteSavedData` ignorer proprement les anciennes entrées devenues invalides.

- [ ] **Step 4: Run focused tests and verify GREEN**

Run: même commande ; expected PASS.

### Task 2: Session-seeded circular mechanics

**Files:**
- Create: `src/main/java/fr/xec9/qte/domain/QteBalanceModel.java`
- Modify: `src/main/java/fr/xec9/qte/domain/QtePointerModel.java`
- Modify: `src/main/java/fr/xec9/qte/domain/QteJudge.java`
- Modify: `src/main/java/fr/xec9/qte/server/QteServerSession.java`
- Modify: `src/main/java/fr/xec9/qte/client/QteClient.java`
- Create: `src/test/java/fr/xec9/qte/domain/QteBalanceModelTest.java`
- Modify: `src/test/java/fr/xec9/qte/domain/QtePointerModelTest.java`
- Modify: `src/test/java/fr/xec9/qte/domain/QteJudgeTest.java`

**Interfaces:**
- Produces: `QteJudge(QteDefinition definition, long sessionSeed)`.
- Produces: `QtePointerModel.target(QteType type, long seed, long elapsedTicks, int durationTicks)`.
- Produces: `QteBalanceModel.targetPhase(long seed)`, `needlePhase(long,int)`, `angularDistance(double,double)`.

- [ ] **Step 1: Write failing seed, bounds and balance tests**

Vérifier que deux graines donnent des cibles aim différentes, que tracking bouge dans `[-1,1]`, et qu’un appui balance réussit seulement dans l’arc de session.

- [ ] **Step 2: Run focused domain tests and verify RED**

Run: `./gradlew test --tests fr.xec9.qte.domain.QtePointerModelTest --tests fr.xec9.qte.domain.QteBalanceModelTest --tests fr.xec9.qte.domain.QteJudgeTest`

Expected: FAIL car les API seed/phase n’existent pas.

- [ ] **Step 3: Implement deterministic per-session mechanics**

Mélanger les bits du UUID en `long`, générer aim sur toute la plage normalisée sûre, décaler la trajectoire tracking par session, et juger balance avec une aiguille circulaire et un arc aléatoire.

- [ ] **Step 4: Run focused domain tests and verify GREEN**

Run: même commande ; expected PASS.

### Task 3: Reliable HUD geometry and placement

**Files:**
- Modify: `src/main/java/fr/xec9/qte/client/QteHudModel.java`
- Modify: `src/main/java/fr/xec9/qte/client/QteKeyPromptRenderer.java`
- Modify: `src/main/java/fr/xec9/qte/client/QteHud.java`
- Modify: `src/test/java/fr/xec9/qte/client/QteHudModelTest.java`

**Interfaces:**
- Produces: placement plein écran borné pour les points normalisés.
- Produces: offset vertical de label dépendant de l’état pressé.
- Produces: anneaux visibles avec culling désactivé/restauré et géométrie circulaire partagée.

- [ ] **Step 1: Write failing layout and pressed-label tests**

Tester les quatre coins sûrs à 240×180, l’offset texte pressé, le placement du HUD au-dessus du chat et la fraction de tracking colorée.

- [ ] **Step 2: Run HUD model tests and verify RED**

Run: `./gradlew test --tests fr.xec9.qte.client.QteHudModelTest`

Expected: FAIL sur les nouvelles méthodes.

- [ ] **Step 3: Implement the circular renderers**

Rendre l’anneau de durée sur chaque type ; dessiner aim/tracking comme cercles plein écran ; colorer tracking par secteur selon `judge.progress()` ; dessiner balance comme cadran circulaire avec arc de réussite et aiguille au-dessus du cadran.

- [ ] **Step 4: Run HUD model tests and verify GREEN**

Run: même commande ; expected PASS.

### Task 4: Font and overlay ordering

**Files:**
- Modify: `src/main/java/fr/xec9/qte/client/QteClient.java`
- Modify: `src/main/java/fr/xec9/qte/client/QteKeyPromptRenderer.java`
- Create: `src/main/resources/assets/qte_engine/font/qte_key.json`
- Create: `src/main/resources/assets/qte_engine/minecraft-five-bold.otf`
- Create: `src/main/resources/MINECRAFT_FIVE_OFL.txt`

**Interfaces:**
- Produces: GUI layer `qte_engine:hud` enregistrée avec `registerAboveAll`.
- Produces: texte de touche stylé avec `qte_engine:qte_key`.

- [ ] **Step 1: Register the HUD above vanilla layers**

Remplacer `RenderGuiEvent.Post` par `RegisterGuiLayersEvent.registerAboveAll`, restaurer le décalage vertical du chat, garder `ScreenEvent.Render.Post` pour le chat ouvert.

- [ ] **Step 2: Add the OFL font resource**

Copier `minecraft-five-bold.otf`, déclarer un provider `ttf` dans `qte_key.json`, appliquer le font ID au `Component` de la touche et inclure le texte OFL.

- [ ] **Step 3: Compile to validate resolved NeoForge/font APIs**

Run: `./gradlew compileJava processResources`

Expected: BUILD SUCCESSFUL et ressources présentes.

### Task 5: Documentation and full verification

**Files:**
- Modify: `README.md`
- Modify: `examples/qte-engine-ui-template/README.md`
- Modify: `gradle.properties`

**Interfaces:**
- Produces: documentation limitée aux huit types et version patch suivante.

- [ ] **Step 1: Update public documentation and version**

Documenter les huit types, le cadran balance, les cibles plein écran et la police sous OFL.

- [ ] **Step 2: Run static checks**

Run: `git diff --check` et rechercher tous les anciens types dans code/tests/docs.

- [ ] **Step 3: Run the complete build**

Run: `./gradlew clean test build`

Expected: BUILD SUCCESSFUL, zéro test échoué.

- [ ] **Step 4: Launch a development client when feasible**

Run: `./gradlew runClient`, vérifier au moins observation, balance, aim et tracking, chat ouvert/fermé et plusieurs GUI scales. Si l’environnement ne permet pas cette inspection, le signaler explicitement sans qualifier le rendu de finalisé visuellement.

- [ ] **Step 5: Install the verified JAR**

Désactiver l’ancien JAR dans `mods`, copier le nouveau, puis vérifier taille et SHA-256.
