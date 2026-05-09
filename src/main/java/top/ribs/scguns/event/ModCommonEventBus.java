package top.ribs.scguns.event;

import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
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
                CogMinionEntity::checkMonsterSpawnRules,
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
                BlundererEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );

        event.register(
                ModEntities.COG_KNIGHT.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                CogKnightEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        event.register(
                ModEntities.TRAUMA_UNIT.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                CogKnightEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );

        event.register(
                ModEntities.DISSIDENT.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                DissidentEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        event.register(
                ModEntities.REDCOAT.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                RedcoatEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );

        event.register(
                ModEntities.HIVE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                HiveEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        event.register(
                ModEntities.HORNLIN.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                HornlinEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        event.register(
                ModEntities.ZOMBIFIED_HORNLIN.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                HornlinEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
    }

}
