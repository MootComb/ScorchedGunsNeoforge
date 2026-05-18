package top.ribs.scguns.event;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import top.ribs.scguns.Config;
import top.ribs.scguns.entity.monster.*;
import top.ribs.scguns.init.ModEntities;

public class ModCommonEventBus {

    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.COG_MINION.get(), CogMinionEntity.createAttributes().build());
        event.put(ModEntities.SUPPLY_SCAMP.get(), SupplyScampEntity.createAttributes().build());
        event.put(ModEntities.COG_KNIGHT.get(), CogKnightEntity.createAttributes().build());
        event.put(ModEntities.SKY_CARRIER.get(), SkyCarrierEntity.createAttributes().build());
        event.put(ModEntities.DISSIDENT.get(), DissidentEntity.createAttributes().build());
        event.put(ModEntities.REDCOAT.get(), RedcoatEntity.createAttributes().build());
        event.put(ModEntities.SCAMP_TANK.get(), ScampTankEntity.createAttributes().build());
        event.put(ModEntities.BLUNDERER.get(), BlundererEntity.createAttributes().build());
        event.put(ModEntities.ADJUDICATOR.get(), AdjudicatorEntity.createAttributes().build());
        event.put(ModEntities.SUBJUGATOR.get(), SubjugatorEntity.createAttributes().build());
        event.put(ModEntities.FINFORCER.get(), FinforcerEntity.createAttributes().build());
        event.put(ModEntities.PRAETOR.get(), PraetorEntity.createAttributes().build());
        event.put(ModEntities.MOTHER_GHAST.get(), MotherGhastEntity.createAttributes().build());
        event.put(ModEntities.VIVENTRUM.get(), ViventrumEntity.createAttributes().build());
        event.put(ModEntities.SULFURHEAD.get(), SulfurheadEntity.createAttributes().build());
        event.put(ModEntities.HIVE.get(), HiveEntity.createAttributes().build());
        event.put(ModEntities.SWARM.get(), SwarmEntity.createAttributes().build());
        event.put(ModEntities.SIGNAL_BEACON.get(), SignalBeaconEntity.createAttributes().build());
        event.put(ModEntities.HORNLIN.get(), HornlinEntity.createAttributes().build());
        event.put(ModEntities.ZOMBIFIED_HORNLIN.get(), ZombifiedHornlinEntity.createAttributes().build());
        event.put(ModEntities.THE_MERCHANT.get(), TheMerchantEntity.createAttributes().build());
        event.put(ModEntities.TRAUMA_UNIT.get(), TraumaUnitEntity.createAttributes().build());
        event.put(ModEntities.SCAMPLER.get(), ScamplerEntity.createAttributes().build());
    }

    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                ModEntities.COG_MINION.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                ModCommonEventBus::checkScorchedGunsMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        event.register(
                ModEntities.SUPPLY_SCAMP.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                SupplyScampEntity::checkAnimalSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        event.register(
                ModEntities.BLUNDERER.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModCommonEventBus::checkScorchedGunsMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );

        event.register(
                ModEntities.COG_KNIGHT.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                ModCommonEventBus::checkScorchedGunsMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        event.register(
                ModEntities.TRAUMA_UNIT.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                ModCommonEventBus::checkScorchedGunsMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );

        event.register(
                ModEntities.DISSIDENT.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                ModCommonEventBus::checkScorchedGunsMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        event.register(
                ModEntities.REDCOAT.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                ModCommonEventBus::checkScorchedGunsMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        event.register(
                ModEntities.PRAETOR.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                ModCommonEventBus::checkScorchedGunsMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );

        event.register(
                ModEntities.HIVE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                ModCommonEventBus::checkScorchedGunsMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        event.register(
                ModEntities.HORNLIN.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                ModCommonEventBus::checkScorchedGunsMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        event.register(
                ModEntities.ZOMBIFIED_HORNLIN.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                ModCommonEventBus::checkScorchedGunsMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        event.register(
                ModEntities.SULFURHEAD.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                ModCommonEventBus::checkScorchedGunsMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
    }

    private static <T extends Monster> boolean checkScorchedGunsMonsterSpawnRules(
            EntityType<T> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)) {
            return true;
        }

        if (spawnType != MobSpawnType.NATURAL || !level.getLevel().isDay()) {
            return false;
        }

        if (!Config.COMMON.naturalMobSpawning.enableDaytimeAggressiveMobSpawns.get()) {
            return false;
        }

        if (level.getDifficulty() == Difficulty.PEACEFUL || !Mob.checkMobSpawnRules(type, level, spawnType, pos, random)) {
            return false;
        }

        int baseProtectionRadius = Config.COMMON.naturalMobSpawning.daytimeAggressiveMobBaseProtectionRadius.get();
        if (baseProtectionRadius > 0 && hasNearbyDaytimeSpawnProtection(level, pos, baseProtectionRadius)) {
            return false;
        }

        return true;
    }

    private static boolean hasNearbyDaytimeSpawnProtection(ServerLevelAccessor level, BlockPos pos, int radius) {
        return level.getLevel()
                .getPoiManager()
                .getInRange(poiType -> poiType.is(PoiTypes.HOME), pos, radius, PoiManager.Occupancy.ANY)
                .findAny()
                .isPresent();
    }

}
