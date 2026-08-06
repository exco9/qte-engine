# QTE Engine UI — template de resource pack

Ce dossier est un resource pack Minecraft 1.21.1 directement utilisable. Il remplace les sprites du HUD via les identifiants de ressources standards du mod; aucune modification Java n'est nécessaire.

## Installation

1. Copier le dossier `qte-engine-ui-template` dans `.minecraft/resourcepacks/`.
2. Le renommer librement.
3. Modifier les PNG dans `assets/qte_engine/textures/gui/sprites/`.
4. Activer le pack au-dessus des ressources par défaut dans Minecraft.
5. Recharger les ressources avec `F3 + T` après chaque export.

Le dossier peut aussi être compressé en ZIP. Dans ce cas, `pack.mcmeta` doit rester à la racine du ZIP, pas dans un sous-dossier supplémentaire.

## Sprites remplaçables

- `qte_hud_frame.png` — 16×16 px, cadre extensible. Garder les coins et bordures dans les 4 px extérieurs. Son fichier `.png.mcmeta` active le nine-slice.
- `qte_keycap.png` — 12×12 px, touche extensible. Garder les coins et bordures dans les 4 px extérieurs. Son fichier `.png.mcmeta` active le nine-slice.
- `qte_marker.png` — 5×11 px, marqueur à taille fixe.

Conserver transparence PNG et contours nets. Éviter filtrage, flou et redimensionnement non entier. Les fichiers `sources/*.aseprite` sont les sources éditables; exporter leurs PNG vers le chemin `assets/.../sprites/` correspondant.

## Texture propre à un QTE

L'argument optionnel `texture` accepte aussi une image fournie par ce pack. Exemple de commande :

```mcfunction
/qte create rune observation space 3 "say @s réussi" "say @s échoué" false false qte_custom:textures/gui/rune.png
```

Le fichier correspondant doit se trouver ici :

```text
assets/qte_custom/textures/gui/rune.png
```

Cette illustration est rendue en 40×40 px. Une image carrée évite la déformation.
