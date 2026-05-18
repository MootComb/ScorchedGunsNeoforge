# Changelog

All notable public changes to Scorched Guns are recorded in this file.

## Unreleased

## 1.4.0 - 2026-05-18

### Added

- Restored companion guide book generation for Viventrum and Supply Scamp packages.
- Updated upstream weapon asset parity for Astella, Auvtomag, Bomb Lance, Bruiser, Cogloader, Combat Shotgun, Freyr, Gauss Rifle, Howler, Jackhammer, M22 Waltz, M3 Carabine, MAK MkII, Makeshift Rifle, MK43 Rifle, Niami, Big Bore, Boomstick, Brawler, Callwell, Callwell Terminal, Defender Pistol, Dozier RL, Echoes 2, Fencer Carabine, Flintlock Pistol, Basker, Callwell Conversion, Floundergat, Gale, Grandle, Grandle OG, Greaser SMG, Gyrojet Pistol, Homemaker, Howler Conversion, Iron Javelin, Iron Spear, Kalaskah, Krauser, LLR Director, Lockewood, Mangalitsa, Marlin, MAS-55, Micina, Nervepinch, Osgood 50, Pax, Plasgun, Plasmabuss, Prima Materia, Pyroclastic Flow, Rat King and Queen, Repeating Musket, Rocket Rifle, Rusty Gnat, Saketini, Saketini Ironport, Scrapper, Scratches, Sculk Resonator, Sequoia, Soul Drummer, Stigg, Stiletto, Trenchur, Trotters, Turnpike, UMAX Pistol, Uppercut, Locust, Shellurker, Venturi, Vulcanic Repeater, Waltz Conversion, Winnie, and the Basic Turret flame top, including changed animated gun resources, attachment special-model JSONs and relevant item textures.
- Updated upstream ammo item asset parity for Advanced Round, Compact Advanced Round, Standard Bullet, Buckshot, Nitro Buckshot, Shotgun Shell, Gibbs Round, Beowulf Round, Hog Round, Krahg Round, Shatter Round and Osborne Slug, including related unfinished and 3D item resources.
- Updated upstream ammo/resource item asset parity for Compact Copper Round, Standard Copper Round, Bearpack Shell, Ramrod Round, Microjet, Rocket, Shotball, Sculk Cell, Shock Cell, Energy Cell, Plasma Core, Shulkshot, Blaze Fuel, Flechette, Grapeshot, Powder and Ball, Hardened Bullet and Needle, including related unfinished and 3D item resources.
- Updated upstream crafting-component item asset parity for molds, casings, gun parts, gun frames, firing units and raw/resource materials, including mold custom-model override resources.
- Updated upstream equipment and ExoSuit item asset parity for armor icons, respirator and mask icons, ExoSuit cores, upgrade modules, armor plates, pouches, pauldrons, repair kits and related suit components.
- Updated upstream blueprint, material, consumable and utility item asset parity for blueprint tiers, Anthralite materials, sulfur resources, energy cores, healing items, Cog Locator, The Pact and related resource icons.
- Updated remaining upstream weapon and attachment visual/audio asset parity for Defender Pistol, Jackhammer, Weevil, War Axe, Laser Sight, Long Scope and several existing gun animation and handling sound resources.
- Updated upstream block, mob and status-effect asset parity for Anthralite blocks and lamps, Auto Turret display, Cog Minion, Dissident and the Lacerated effect icon.
- Updated upstream GUI asset parity for the attachment screen, blueprint viewer, HUD fill bar, Hot Barrel bar and melee indicator textures.
- Updated upstream Netherite Respirator armor texture parity.
- Added upstream Spanish locale files for Argentina, Chile, Ecuador, Spain, Mexico, Uruguay and Venezuela.
- Restored upstream projectile trail textures, grenade/fireball particle resources and dedicated Praetor, Dissident and Supply Scamp sound assets.
- Added common config options for daytime natural spawning of hostile Scorched Guns mobs, preserving night spawn rates while adding configurable bed-radius protection for player bases.
- Added category-aware Supply Scamp patrol sorting so tamed scamps deposit ammo, weapons, armor, potions, food, tools, wood, materials, blocks and miscellaneous items into matching barrels instead of dumping everything into the nearest container.
- Added a common `gunnerMobInaccuracyMultiplier` config option, defaulting to `1.5`, to make hostile gunner and raid mobs less precise without changing player weapon spread or gun JSON values.
- Added optional Guard Villagers compatibility: when Guard Villagers is installed, village guards can spawn with low-tier Scorched Guns militia weapons through the existing gunner-mob equipment system.

### Fixed

- Fixed Netherite Respirator fire resistance metadata for Minecraft 1.21.1 so dropped respirators use the current fire-resistant item component instead of an obsolete item override.
- Fixed newly synced ammo item models rendering as missing black-purple squares by migrating their separate-transform loaders and texture paths to NeoForge 1.21.1 resource ids.
- Restored Beowulf and Gibbs Round projectile bonus-looting behavior for entity loot table count increases and enchanted rare-drop chances.
- Fixed Supply Scamp persistence so server restarts and world reloads preserve carried inventory and no longer reset tamed scamps back to full health.
- Fixed Supply Scamp repair interaction so using a Repair Kit repairs the tamed scamp instead of opening its inventory.
- Improved Sable/Create Aeronautics compatibility by preventing Scorched Guns block inventories from dropping during sublevel assembly, allowing gun projectiles to collide with Sable sublevel contraptions and making turret targeting use sublevel world-space positions.
- Retimed inspect-animation sound keyframes for Freyr, Gauss Rifle, Howler, Jackhammer and M22 Waltz so handling sounds line up with their updated weapon animations.
- Restored reload windows for raid and gunner mob weapons so armed mobs no longer fire indefinitely without magazine downtime, hardened reload completion so mob guns cannot get stuck reloading, restored firing feedback for mob-held silenced/fallback-sound weapons such as sculk guns, fixed stale silenced-fire sound ids on Arc Worker and Lone Wonder, and kept dropped mob guns from retaining infinite-ammo behavior.
- Fixed gunner and raid mob reload timing so empty single-shot weapons, including musket-style guns, include their configured empty-mag reload delay instead of firing far faster than intended.
- Fixed third-person muzzle flashes for gunner and raid mobs using Scorched Guns weapons.
- Improved Create Aeronautics/Sable compatibility for turrets by giving turret block entities a Sable sublevel physics tick bridge and making Ammo Modules feed adjacent turret ammo slots directly instead of relying only on generic item capability insertion.
- Fixed Sable/Create Aeronautics turret aiming so turrets on moving ships use the active sublevel pose during physics ticks and fire toward their selected target instead of a stale local-world direction.
- Added Sable/Create Aeronautics-aware block interaction for direct block-breaking projectile hits, beam mining and custom projectile explosions, allowing compatible Scorched Guns shots and explosive impacts to damage physical contraption blocks instead of only colliding with them.
- Restored newer gun recoil, spread, pellet-count and player knockback data that had been present in resource JSON but ignored by the current gun parser, covering Kiln Gun and the recent late-weapon batches.
- Restored Choke Bomb's lasting cloud/effect behavior and regular Grenade's upstream-style explosion profile.
- Fixed sculk gun fire sound spatialization by restoring the mono upstream sound asset.
- Fixed sulfur vent worldgen/tick recovery so generated vents can activate and produce sulfur clouds in fresh chunks.
- Hardened Hive/Swarm cleanup against async entity-unload crashes reported in heavily modded Sable/C2ME/Connector client packs.
- Fixed Create funnel item transfer for Ammo Modules and Shell Catcher Modules by exposing their inventories through NeoForge item capabilities and making their handlers support real insertion/extraction.
- Fixed Supply Scamp patrol sorting stalls where a scamp could keep one or two leftover items indefinitely after depositing one sorted category.
- Fixed Guard Villagers holding Scorched Guns weapons so their humanoid model uses Scorched Guns weapon holding/aiming poses instead of the default guard pose.
- Fixed Guard Villagers spawned from generated/loaded villages not rolling Scorched Guns militia weapons while spawn-egg guards did.
- Fixed Guard Villagers generated during village structure placement being equipped before their vanilla guard gear pass finished, and made their Scorched Guns weapon roll visible at default gunner spawn settings.
- Fixed Buckshot crafting so regular vanilla Gunpowder is accepted by the shared gunpowder ingredient tag again, alongside Sheol and Peal.
- Restored the upstream Geo armor renderer/assets for Treated Brass armor so the equipped set uses its intended custom model instead of the vanilla armor layer.
- Restored projectile lag-compensation hitbox rewind to use the shooter's current server latency instead of the temporary zero-ping fallback.
- Fixed Blueprint pages showing the undiscovered-knowledge placeholder instead of the available weapon lore by resolving lore keys from the actual item registry id.
- Fixed manual per-round reload animations getting stuck in the reload loop when topping off partially loaded weapons such as revolvers.
- Fixed animated gun item rendering crashes in external GUI/item preview screens when no local player is available.

### Changed

- Raised the public version to `1.4.0`.

## 1.3.2 - 2026-05-15

### Added

- Restored the configurable ammo-type swap keybind for multi-ammo weapons. Blooper, Hammer GL, Truant, Triquetra, Thunderhead, Hullbreaker and Bomb Lance can now cycle their supported ammo types through the controls menu keybind.

### Fixed

- Fixed gun/custom-gun JSON validation compatibility with newer Java versions by preventing the reflection validator from recursing into internal JDK classes. Thanks to `unilock` for the pull request.
- Fixed ExoSuit jetpack landing protection so accumulated fall distance is still cleared when the player touches down on the same tick that vanilla/server flight state stops being active.
- Fixed ExoSuit jetpack flight cleanup so Scorched Guns no longer disables survival flight granted by other mods when the player is not using the ExoSuit jetpack.
- Fixed ExoSuit jetpack battery persistence so active flight is reported to the server and power core charge no longer restores to full after re-entering a world.
- Fixed ExoSuit jetpack landing cleanup so normal sprinting is restored after touching down.
- Fixed Niter Glass redstone updates so connected panes switch to their transparent state when powered and reliably redraw on clients.
- Restored upstream Geo armor models, renderers, animations and textures for Redcoat and Scrap armor so both sets render with their intended equipped models instead of falling back to broken vanilla armor layers.
- Made the common gunner mob spawn chance meaningfully scale thematic gunner mobs from `gunner_mobs.json`, so lowering `gunnerSpawnChance` reduces both progression-based and themed vanilla mob gun spawns.
- Fixed Scorched Guns config validation and reload handling so server cooldown settings no longer auto-correct from an invalid default range, and gunner mob settings refresh when the common config is reloaded.
- Fixed ammo-type swapping so weapons skip unavailable ammo types instead of cycling through every configured projectile, and restored automatic reload start after switching to a new ammo type without reintroducing the broken infinite reload animation.
- Fixed ammo-type swap state isolation so switching ammo on one weapon no longer reuses stale reload tracking or selected-ammo data on other multi-ammo weapons.
- Fixed animated gun reload visuals leaking to other weapons in the player's inventory while only one weapon is actually being reloaded.
- Fixed AmmoBox reload consumption so reloading from inventory, Curios-equipped, or ExoSuit-stored ammo boxes no longer deletes stored ammo or leaves the weapon stuck in a reload animation.
- Limited gunner mob firing range to their follow-range attribute, preventing aggroed gunner mobs from shooting across unlimited line-of-sight distances.
- Fixed animated item textures for Cog Heart, Sculk Tome, Ocean Flare and Sculk Flare by restoring their upstream animation metadata.

## 1.3.1 - 2026-05-14

### Fixed

- Fixed a rare client JVM crash when removing and reinserting weapon magazine attachments by moving attachment-slot compatibility checks out of the per-frame render overlay path.
- Fixed crashes when inserting enchanted attachments into weapons by saving and reading attached ItemStacks with the active world/player registry access instead of the built-in registry snapshot.
- Fixed old Silk Touch loot predicates on Scorched Guns ores and related mineable blocks so normal mining drops the intended raw/resources again while Silk Touch still drops the block.
- Fixed Scorched Guns enchantment availability so custom enchanted books appear in the mod creative tab and can be selected by vanilla loot/trade/equipment enchantment pools without crashing the creative inventory.
- Fixed Scorched Guns enchanted book search visibility by adding all custom enchantment book levels to the vanilla Ingredients/search creative output with duplicate protection.
- Fixed Scorched Guns enchanted book creative-tab output so the books are added through the current creative-tab holder lookup and all levels are searchable.
- Fixed Scorched Guns enchantments not appearing in creative or the enchanting table by restoring their Minecraft 1.21.1 data-driven enchantment definitions and supported-item tags.
- Fixed mojibake in the English localization where symbols such as bullets, section formatting, check marks, warning icons, hearts and energy icons displayed as corrupted text.

### Changed

- Updated the attachment workbench screen to match the newer upstream layout with a larger weapon preview, horizontal attachment slots and a weapon stat readout.
- Raised the public version to `1.3.1`.

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
- Updated weapon asset parity for RG Jigsaw, Birdfeeder and M3 Marksman, including upstream special models and animated gun resources.
- Improved JEI recipe integration for Scorched Guns machine and GunBench recipes by preserving recipe IDs through `RecipeHolder`, enabling advanced tooltip IDs and recipe bookmarking. Thanks to `gisellevonbingen` for the pull request.
- Added optional compatibility with Sound Physics Remastered so remote Scorched Guns shots are handled as world-positioned sounds while local shots remain centered for stable first-person audio.
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

- Some minor asset or model warnings may still need visual QA.
- Netherite gas mask fire-resistant behavior should receive additional runtime QA.
