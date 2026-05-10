# Changelog

All notable public changes to Scorched Guns are recorded in this file.

## Unreleased

No unreleased changes.

## 1.2.5 - 2026-05-11

### Added

- Restored the Scorched Guns gunner mob system: eligible hostile mobs can spawn with Scorched Guns weapons, tier progression controls which weapon tiers can appear, and elite gunner variants can equip stronger gear.
- Backported upstream gunner mob weapon drop chance support, including separate configured drop chances for illager, piglin, and Hornlin gunners.
- Restored the existing Blunderer raid reinforcement hook through explicit runtime event registration.
- Restored data-driven Vent Collector settings and sulfur/geothermal vent output definitions from upstream.
- Added weighted vent outputs, including rare blaze powder production from sulfur vents.
- Restored bat guano as a placeable/fertilizer item with layer stacking and dropped-item layer formation.
- Restored guano candles, including flint-and-steel lighting, crouch empty-hand extinguishing, sulfur dust ambience, and bat attraction/spawn behavior.
- Restored bat guano recipes for guano candles, smoked phosphor dust, and Farmer's Delight organic compost when Farmer's Delight is installed.
- Restored phosphor dust as an upstream fertilizer item that can also place and stack phosphor layers.
- Added phosphor layer block assets, loot behavior and localization.
- Restored Vicious Acid as an upstream fluid slice, including the acid bucket, fluid block, acid cauldron, acid bubble particles, crafting recipe, and localized names.
- Restored upstream Vicious Acid world reactions with water, lava, and fire.
- Restored Mine Unit as a grenade-loaded proximity trap, including priming behavior, stored grenade drops, model assets, recipe, loot table and entity ignore tag.
- Restored Shock Coil as an energy-powered defensive block with data-driven zap settings, redstone disabling, adjacent turret targeting module support, recipe, loot table, assets and localized tooltips.
- Restored Metal Detector as a durability-based tool that pulls the player toward nearby metal-detectable blocks and highlights the detected target.
- Added the upstream Turret Platform support item required by the Shock Coil recipe.

### Fixed

- Fixed ADS mouse sensitivity not being reduced while aiming, especially with high-zoom scopes.
- Fixed gunner mob tier progression so high-tier weapons are recognized through restored item tier tags and progression spawns skip empty weapon tiers.
- Fixed ExoSuit upgrade visuals not appearing on the armor model when the client cannot resolve ExoSuit upgrade metadata from the datapack reload manager.
- Fixed bat guano and phosphor layers not dropping their source items when broken normally.
- Fixed Vent Collector weak filter data so wool is accepted as the low-tier filter configured by upstream vent data.
- Fixed Mine Unit follow-up placement behavior by preventing non-living helper entities from triggering armed mines.

### Changed

- Raised the public version to `1.2.5`.

## 1.2.0 - 2026-05-10

### Added

- Integrated optional first-person compatibility handling for Punchy and Fresh Animations: Player Extensions / Entity Model Features, so separate Scorched Guns compat jars are no longer needed for those client-side arm-render fixes.

### Changed

- Raised the public version to `1.2.0`.

## 1.1.1 - 2026-05-10

### Fixed

- Fixed ExoSuit power core energy drain so powered upgrades spend and persist FE from the installed core instead of only mutating a temporary upgrade-stack copy.
- Fixed ExoSuit power cooldown gating so powered modules drain at their intended intervals, and moved jetpack drain to the server-side flight tick instead of relying on client-local item changes.
- Fixed Minecraft 1.21.1 registry tag folder paths for item, block, and entity type tags so Scorched Guns recipe ingredients no longer appear as empty tags in recipe viewers.

### Changed

- Raised the public hotfix version to `1.1.1`.

## 1.1.0 - 2026-05-10

### Fixed

- Fixed remaining NeoForge resource namespace migrations for optional compatibility recipes, global loot modifiers, and common tags so old `forge:` condition/tag paths no longer drive recipe and loot loading.
- Fixed Scorched Guns ore and vent biome modifiers using the old Forge namespace/path so Anthralite, Sulfur, Phosphorite, Vehement Coal, Sulfur Vents, and Geothermal Vents can be added to NeoForge 1.21.1 world generation.
- Fixed remaining Scorched Guns recipe datapack parse errors in Create 6 integration recipes, Macerator and Powered Macerator alternative ingredients, Create filling fluid ingredients, and stale item ids.

### Changed

- Cleaned the public changelog so it contains release-facing notes only.
- Removed the obsolete legacy `changelog` file from the public source package; `CHANGELOG.md` is now the only public changelog.
- Raised the public version to `1.1.0`.

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
