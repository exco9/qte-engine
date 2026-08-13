# QTE Pointer Settings and Font Fix Plan

**Goal:** Slow tracking by default, expose persistent per-QTE pointer settings, and rebuild the key-label bitmap font without clipped glyphs.

## Tasks

- [x] Add failing domain/resource tests for tracking speed, fixed Aim coordinates, validation, and padded font cells.
- [x] Extend `QteDefinition` with a bounded tracking-speed multiplier and optional normalized Aim coordinates.
- [x] Apply the same pointer settings in both the authoritative judge and client HUD.
- [x] Persist settings in saved data format 5 and transmit them in `StartQtePayload`.
- [x] Add `/qte settings <id> tracking_speed <0.1..2.0>`, `aim_position <x> <y>`, and `aim_random`.
- [x] Rebuild the bitmap atlas from `MinecraftFive-Bold.ttf` with padded cells and update documentation.
- [x] Bump to 0.4.3, run focused/full tests and build, inspect the atlas, then install the jar in the instance.

## Defaults and coordinate contract

- Tracking speed defaults to `0.45`; `1.0` preserves the former motion rate.
- Aim coordinates use normalized HUD space: `(-0.92, -0.92)` is near top-left and `(0.92, 0.92)` near bottom-right.
- Missing Aim coordinates keep the existing deterministic random placement per play session.
