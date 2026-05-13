# Changelog

All notable public changes to Scorched Guns are recorded in this file.

## 1.3.0 - 2026-05-13

### Added

- Restored the Scorched Guns raid progression stack: data-driven raid configs, active raid tracking, boss and henchman spawning, boss bars, raid timeout/defeat/surrender flow, raid flares, Flare Pistol behavior, White Flag surrender item and raid trophy rewards.
- Restored Blueprint Scrap progression with merchant and loot sources, trophy-based blueprint duplication recipes, the Wrecker blueprint duplication recipe and weighted gunner weapon JSON support with optional `drop_weight` values.
- Restored late-raid enemies and related raid content: Adjudicator, Subjugator, Finforcer, Praetor, Mother Ghast, Sulfurhead and Spirulida, including renderers, models, spawn eggs, loot tables, raid integration and localization.
- Restored additional weapons and combat items from newer upstream versions: Fencer Carabine, Teslock Rifle, Hyperbaria, Kiln Gun, Blooper, Triquetra, Hammer GL, Truant, Zilk .45, Whistler, Minksy, Libertas, Mas Peddler, J.R. Wristbreaker, Fencer Thumper, Fencer Longarm, Nailer, Railworker, Drill, Drill Conversion, Inquisitor, Red Raydar, Atom Sterilizer, Whizzbanger, Winnie Millend and War Axe.
- Restored supporting ammo and crafting content for the late weapons: Needle, Frog Dart, HE/Gas/Bouncy/Fire grenade rounds, hidden Create transitional items, GunBench entries, Blueprint/GunBench ordering, tier tags, Create sequenced assembly chains and optional Create recycling/mechanical crafting recipes.
- Restored Viventrum as a tameable mechanical companion with package item, spawn egg, renderer/model, behavior logic, recipe, loot table, item/entity tags and localization.
- Restored Iron Mask, Hellfire Bomb, Air Canister, Reinforced Air Canister, Creative Air Canister and Bump Stock with their associated recipes, assets, tooltips and runtime behavior.
- Restored decorative and building block families for Anthralite, Diamond Steel, Treated Iron, Treated Brass and Asgharian materials, including block registrations, assets, loot tables, mining tags, crafting and stonecutting recipes.
- Restored turret data parity: shared data-driven player turret backend, GunBench entries for existing Basic, Auto and Shotgun Turrets, Sniper Turret, turret config JSON, JEI info and the `scguns:entity_blacklisted_gun` item tag.
- Restored utility/progression content: Cog Mace, Anthralite Hammer, Anthralite Paxel, Shulker Core, Shulker Core End Blueprint duplication support and Shulker Core rare Shulker drops.
- Restored visual/resource parity for throwable grenades and bombs, Scorched Block, temporary fake soul fire for Hellfire effects and optional Create recipe parity.
- Updated the first targeted weapon asset parity batch for RG Jigsaw, Birdfeeder and M3 Marksman, including upstream special models and animated gun resources.
- Improved JEI recipe integration for Scorched Guns machine and GunBench recipes by preserving recipe IDs through `RecipeHolder`, enabling advanced tooltip IDs and recipe bookmarking. Thanks to `gisellevonbingen` for the pull request.
- Updated localization key coverage across English, Russian, Korean, Ukrainian and Vietnamese; Russian wording was refreshed with help from `jpegemerald`.

### Fixed

- Fixed mouse settings screen crashes caused by a stale Scorched Guns mouse-options reflection hook.
- Fixed ADS mouse sensitivity reduction so high-zoom scopes slow camera movement while aiming again.
- Tuned high-zoom optic ADS sensitivity scaling so long scopes reduce camera movement more noticeably.
- Fixed reload FOV handling so reloading no longer forces the player's configured FOV back to the default value or blocks in-game FOV changes until restart.
- Fixed ExoSuit jetpack landing damage by clearing accumulated fall distance while jetpack flight is active.
- Fixed melee miss feedback so accepted melee swings still play the local visual animation even when no target is hit.
- Fixed a dedicated-server crash when Scorched Guns projectiles collided with lava before client particle config values were available.
- Fixed a dedicated-server crash where Cog Minion explosive self-destructs could break modded storage blocks and crash external storage controllers.
- Fixed Scorched Guns enchantment availability so custom enchanted books appear in the mod creative tab and can be selected by vanilla loot/trade/equipment enchantment pools without crashing the creative inventory.
- Fixed missing Mechanical Press and Powered Mechanical Press recipes for Copper, Iron, Wrecker, Gold, Diamond Steel, Treated Brass, Ocean and Sculk raid flares.
- Fixed Praetor natural spawning parity with upstream 0.5.5 by restoring its low-weight general-biome spawn rule and spawn placement registration.

### Notes

- The post-0.5.0 gun JSON/content set is now present; remaining audits are focused on non-gun content, polish and runtime parity.
- Viventrum's old in-game guide book helper is not currently restored; the package still spawns a tamed companion.
- Grenade-round ammo is registered and data-driven; Hammer GL, Truant, Blooper and Triquetra now consume the restored alternate grenade-round types.
- Player turrets now share the upstream-style data-driven `TurretBlockEntity` backend; Enemy Turrets intentionally remain on their separate hostile turret logic, matching upstream.
- Raised the public version to `1.3.0`.

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
