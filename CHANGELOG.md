# Changelog

All notable public changes to Scorched Guns are recorded in this file.

## Unreleased

### Changed

- Cleaned the public changelog so it contains release-facing notes only.
- Removed the obsolete legacy `changelog` file from the public source package; `CHANGELOG.md` is now the only public changelog.

## 1.0.2 - 2026-05-10

### Fixed

- Fixed Minecraft 1.21.1 recipe result schema for Scorched Guns recipes so GunBench blueprint recipes can parse and display their ingredients, output names, and lore.
- Fixed Scorched Guns recipe datapack parse errors caused by legacy `result.item`, string `result`, `results.item`, and Create `acceptMirrored` fields.
- Fixed a first-person animated gun arm-render crash that could happen with `renderArms=true` on some clients when taking a weapon in hand.
- Fixed a crash when rendering weapons with scope or laser attachments caused by invalid attachment model resource paths.
- Fixed animated-gun handling for the `plus_p_mag` attachment so it no longer tries to load a missing dynamic attachment model.

### Changed

- Raised the public hotfix version to `1.0.2`.

## 1.0.1 - 2026-05-10

### Fixed

- Fixed dedicated-server log spam when firing guns by preventing server-side visual animation controller access.
- Fixed blurred blueprint recipe GUI rendering on Minecraft 1.21.1.
- Fixed blueprint recipe loading on Minecraft 1.21.1 so blueprint pages show GunBench recipes, item names, and lore instead of an empty grid.
- Fixed localization coverage for English, Russian, Ukrainian, Korean, and Vietnamese.
- Fixed missing and empty language keys while preserving formatting placeholders.

### Changed

- Raised the public version to `1.0.1`.
- Updated the public release package and clean source package after the latest smoke-test fixes.

## 1.0.0 - 2026-05-10

### Added

- Ported Scorched Guns to Minecraft 1.21.1 on NeoForge.
- Added NeoForge 1.21.1 runtime metadata for required dependencies: Framework, Curios, and GeckoLib.
- Added optional integration metadata for supported optional mods without bundling them.

### Fixed

- Migrated client startup, world loading, dedicated server startup, and multiplayer connection paths for NeoForge 1.21.1.
- Migrated structures, structure loot, supply crates, and Scorched Guns mob spawning for 1.21.1 runtime behavior.
- Migrated weapon rendering, shooting, reloading, third-person weapon poses, ExoSuit GUI behavior, projectiles, and basic machine interactions for smoke-tested 1.21.1 play.

### Known Non-Blocking Follow-Ups

- Beowulf and Gibbs exact bonus-looting restoration remains deferred.
- Projectile ping and lag-compensation behavior uses a conservative fallback and needs more multiplayer QA.
- Some minor asset or model warnings may still need visual QA.
- Netherite gas mask fire-resistant behavior should receive additional runtime QA.
