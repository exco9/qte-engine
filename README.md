# QTE Engine

Mod NeoForge pour Minecraft 1.21.1. Définitions persistantes par monde, HUD client, résultats exécutés côté serveur.

## Commandes

```text
/qte create <id> <type> <inputs> <durée_secondes> <commande_réussite> <commande_échec> [exclusive_input] [hide_hud] [texture]
/qte edit <id> <type> <inputs> <durée_secondes> <commande_réussite> <commande_échec> [exclusive_input] [hide_hud] [texture]
/qte play <id>
/qte remove <id>
/qte list
/qte types
```

`create`, `edit` et `remove` demandent permission opérateur niveau 2. `edit` remplace tous les paramètres d'un QTE existant sans changer son identifiant. `play`, `list` et `types` sont accessibles aux joueurs. Les commandes de réussite et d'échec contenant des espaces doivent être entre guillemets. `/` initial facultatif. `%player%` est remplacé par le nom du joueur et `@s` cible directement ce joueur.

`exclusive_input` vaut `false` par défaut. Avec `true`, clavier, boutons, molette et mouvement caméra sont réservés au QTE jusqu'à son résultat; Minecraft ne reçoit pas ces entrées.

`hide_hud` vaut `false` par défaut. Avec `true`, le HUD vanilla passe temporairement en mode F1 tandis que la fenêtre QTE reste visible. L'état F1 précédent est restauré à la fin. Pour fournir une texture, les deux booléens doivent être indiqués.

Exemples:

```mcfunction
/qte create esquive observation space 2.5 "say @s a esquivé" "damage @s 2" true false
/qte create rune input_sequence w,a,s,d 6 "function histoire:rune_reussie" "function histoire:rune_echec" true true qte_engine:textures/gui/rune.png
/qte create serrure mash e 4 "give @s minecraft:tripwire_hook" "say @s a échoué"
/qte create parade timing mouse.left 2 "damage @s 0" "damage @s 4"
/qte edit parade aim m1 3 "damage @s 0" "damage @s 4" true false
/qte play esquive
/qte remove esquive
```

Touches acceptées sous forme courte (`space`, `w`, `left_shift`) ou identifiant Minecraft (`key.keyboard.space`, `key.mouse.left`). Les alias `m1`, `m2`, `m3`, `mouse1`, `mouse2`, `mouse3`, `left_click`, `right_click` et `middle_click` sont acceptés. Séparer les séquences par virgules; entourer l'ensemble de guillemets si des espaces sont utilisés, par exemple `"w, a, m1"`.

`input_sequence`, `reaction_choice`, `memory` et `rhythm` exigent au moins deux inputs. Les autres types exigent exactement un input.

Les lettres courtes (`z`, `w`, `a`...) suivent automatiquement la disposition du joueur. Ainsi `z` demande la touche imprimée Z sur AZERTY comme QWERTY. Utiliser un identifiant complet comme `key.keyboard.w` pour cibler la position physique Minecraft indépendamment de la disposition.

## Types et règles

- `observation`: appuyer sur la touche attendue avant le timeout.
- `reaction_choice`: première touche de liste `key` est bonne réponse; autres choix échouent.
- `timing`, `dialogue_timing`: appuyer dans zone verte du timing bar.
- `hold`: maintenir touche pendant 60 % durée.
- `mash`: appuyer rapidement; objectif dépend durée, minimum 5 pressions.
- `input_sequence`: reproduire la séquence dans l'ordre.
- `memory`: mémoriser séquence avant masquage, puis la reproduire.
- `rhythm`: saisir chaque touche sur marque temporelle correspondante.
- `analog_precision`, `balance`: appuyer sur la touche configurée lorsque le marqueur automatique atteint la zone centrale.
- `aim`: déplacer le viseur avec la souris jusque dans la cible fixe, puis appuyer sur l'input configuré.
- `tracking`: maintenir l'input configuré tout en suivant à la souris une cible mobile pendant 45 % de la durée. `aim` et `tracking` suivent la sensibilité et l'inversion verticale configurées dans Minecraft.

Mauvaise touche configurée, relâchement trop tôt ou expiration déclenche la commande d'échec. Les inputs sont rejugés côté serveur avant toute commande; le résultat annoncé par le client n'est pas considéré comme autoritaire.

## Textures

Texture optionnelle utilise identifiant de ressource complet, par exemple `mon_pack:textures/gui/qte_rune.png`. Image affichée en 40×40. Sans texture, HUD compact standard.

Le HUD « Signal Rail » adapte automatiquement sa largeur à l'échelle GUI et change de présentation selon la mécanique : touches individuelles, fenêtre de timing, précision centrale, maintien ou martelage. Le chat est automatiquement repoussé au-dessus de la fenêtre pendant un QTE. Les sources pixel-art Aseprite sont conservées dans `art/ui/`; les PNG exportés se trouvent dans `src/main/resources/assets/qte_engine/textures/gui/sprites/`.

Les sprites du HUD utilisent les identifiants standards `qte_engine:qte_hud_frame`, `qte_engine:qte_keycap` et `qte_engine:qte_marker`. Ils peuvent donc être remplacés par un resource pack. Un template prêt à copier, avec PNG, métadonnées nine-slice et sources Aseprite, se trouve dans `examples/qte-engine-ui-template/`.

## Build

Java 21 requis:

```powershell
.\gradlew.bat clean test build
```

JAR produit: `build/libs/qte_engine-0.3.0.jar`.
