# QTE Engine

Mod NeoForge pour Minecraft 1.21.1. Définitions persistantes par monde, HUD client, résultats exécutés côté serveur.

## Commandes

```text
/qte create <id> <type> <key> <durée_secondes> <commande_résultat> [exclusive_input] [texture]
/qte play <id>
/qte remove <id>
/qte list
/qte types
```

`create` et `remove` demandent permission opérateur niveau 2. `play`, `list` et `types` sont accessibles aux joueurs. Une commande résultat contenant des espaces doit être entre guillemets. `/` initial facultatif. `%player%` est remplacé par nom du joueur ayant réussi.

`exclusive_input` vaut `false` par défaut. Avec `true`, clavier, boutons, molette et mouvement caméra sont réservés au QTE jusqu'à son résultat; Minecraft ne reçoit pas ces entrées.

Exemples:

```mcfunction
/qte create esquive reaction space 2.5 "say %player% a esquivé" true
/qte create rune input_sequence w,a,s,d 6 "function histoire:rune_reussie" true qte_engine:textures/gui/rune.png
/qte create serrure mash e 4 "give %player% minecraft:tripwire_hook"
/qte create parade timing mouse.left 2 "damage %player% 0"
/qte play esquive
/qte remove esquive
```

Touches acceptées sous forme courte (`space`, `w`, `left_shift`) ou identifiant Minecraft (`key.keyboard.space`, `key.mouse.left`). Séparer séquences par virgules, sans espaces.

Les lettres courtes (`z`, `w`, `a`...) suivent automatiquement disposition du joueur. Ainsi `z` demande touche imprimée Z sur AZERTY comme QWERTY. HUD indique disposition détectée (`AZERTY`, `QWERTZ`, `QWERTY` ou `CUSTOM`). Utiliser identifiant complet comme `key.keyboard.w` pour cibler position physique Minecraft indépendamment disposition.

## Types et règles

- `reaction`, `observation`, `attention`: appuyer touche attendue avant timeout.
- `reaction_choice`: première touche de liste `key` est bonne réponse; autres choix échouent.
- `timing`, `dialogue_timing`: appuyer dans zone verte du timing bar.
- `hold`: maintenir touche pendant 60 % durée.
- `mash`: appuyer rapidement; objectif dépend durée, minimum 5 pressions.
- `input_sequence`, `direction`, `pattern`: reproduire séquence dans ordre.
- `memory`: mémoriser séquence avant masquage, puis la reproduire.
- `rhythm`: saisir chaque touche sur marque temporelle correspondante.
- `analog_precision`, `aim`, `tracking`, `balance`: appuyer touche configurée lorsque curseur mobile atteint zone centrale.

Mauvaise touche configurée ou relâchement trop tôt provoque échec. Expiration ne lance jamais commande résultat.

## Textures

Texture optionnelle utilise identifiant de ressource complet, par exemple `mon_pack:textures/gui/qte_rune.png`. Image affichée en 48×48. Sans texture, HUD compact standard.

## Build

Java 21 requis:

```powershell
.\gradlew.bat clean test build
```

JAR produit: `build/libs/qte_engine-0.3.0.jar`.
