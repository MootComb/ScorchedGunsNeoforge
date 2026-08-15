package top.ribs.scguns.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.block.PrimedNitroKeg;
import top.ribs.scguns.entity.block.PrimedPowderKeg;
import top.ribs.scguns.entity.monster.*;
import top.ribs.scguns.entity.projectile.BrassBoltEntity;
import top.ribs.scguns.entity.projectile.*;
import top.ribs.scguns.entity.projectile.turret.TurretProjectileEntity;
import top.ribs.scguns.entity.throwable.*;

import java.util.function.BiFunction;

public class ModEntities
{
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(Registries.ENTITY_TYPE, Reference.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<PrimedPowderKeg>> PRIMED_POWDER_KEG = REGISTER.register("primed_powder_keg",
            () -> EntityType.Builder.<PrimedPowderKeg>of(PrimedPowderKeg::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("primed_powder_keg"));
    public static final DeferredHolder<EntityType<?>, EntityType<PrimedNitroKeg>> PRIMED_NITRO_KEG = REGISTER.register("primed_nitro_keg",
            () -> EntityType.Builder.<PrimedNitroKeg>of(PrimedNitroKeg::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("primed_nitro_keg"));
    public static final DeferredHolder<EntityType<?>, EntityType<TurretProjectileEntity>> TURRET_PROJECTILE = REGISTER.register("basic_turret", () ->
            EntityType.Builder.<TurretProjectileEntity>of(TurretProjectileEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).build("basic_turret"));

    public static final DeferredHolder<EntityType<?>, EntityType<ProjectileEntity>> PROJECTILE = registerProjectile("projectile", ProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<BearPackShellProjectileEntity>> BEARPACK_SHELL_PROJECTILE = registerBasic("bearpack_shell_projectile", BearPackShellProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<OsborneSlugProjectileEntity>> OSBORNE_SLUG_PROJECTILE = registerBasic("osborne_slug_projectile", OsborneSlugProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<PlasmaProjectileEntity>> PLASMA_PROJECTILE = registerBasic("plasma_projectile", PlasmaProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<RamrodProjectileEntity>> RAMROD_PROJECTILE = registerBasic("ramrod_projectile", RamrodProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<HogRoundProjectileEntity>> HOG_ROUND_PROJECTILE = registerBasic("hog_round_projectile", HogRoundProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<BeowulfProjectileEntity>> BEOWULF_PROJECTILE = registerBasic("beowulf_projectile", BeowulfProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<BlazeRodProjectileEntity>> BLAZE_ROD_PROJECTILE = registerBasic("blaze_rod_projectile", BlazeRodProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<BasicBulletProjectileEntity>> BASIC_BULLET_PROJECTILE = registerBasic("basic_bullet_projectile", BasicBulletProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<HardenedBulletProjectileEntity>> HARDENED_BULLET_PROJECTILE = registerBasic("hardened_bullet_projectile", HardenedBulletProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<BuckshotProjectileEntity>> BUCKSHOT_PROJECTILE = registerBasic("buckshot_projectile", BuckshotProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<FireRoundEntity>> FIRE_ROUND_PROJECTILE = registerBasic("fire_round_projectile", FireRoundEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<GrenadeEntity>> GRENADE = registerBasic("grenade", GrenadeEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<RocketEntity>> ROCKET = registerBasic("rocket", RocketEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<MicroJetEntity>> MICROJET = registerBasic("microjet", MicroJetEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ShulkshotProjectileEntity>> SHULKSHOT = registerBasic("shulkshot_projectile", ShulkshotProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkCellEntity>> SCULK_CELL = registerBasic("sculk_cell", SculkCellEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ShatterRoundProjectileEntity>> SHATTER_ROUND_PROJECTILE = registerBasic("shatter_round_projectile", ShatterRoundProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<SyringeProjectileEntity>> SYRINGE_PROJECTILE = registerBasic("syringe_projectile", SyringeProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<KrahgRoundProjectileEntity>> KRAHG_ROUND_PROJECTILE = registerBasic("krahg_round_projectile", KrahgRoundProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<AdvancedRoundProjectileEntity>> ADVANCED_ROUND_PROJECTILE = registerBasic("advanced_round_projectile", AdvancedRoundProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<GibbsRoundProjectileEntity>> GIBBS_ROUND_PROJECTILE = registerBasic("gibbs_round_projectile", GibbsRoundProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ShotballProjectileEntity>> SHOTBALL_PROJECTILE = registerBasic("shotball_projectile", ShotballProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<HeGrenadeRoundEntity>> HE_GRENADE_ROUND = registerBasic("he_grenade_round", HeGrenadeRoundEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<FireGrenadeRoundEntity>> FIRE_GRENADE_ROUND = registerBasic("fire_grenade_round", FireGrenadeRoundEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<GasGrenadeRoundEntity>> GAS_GRENADE_ROUND = registerBasic("gas_grenade_round", GasGrenadeRoundEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<BouncyGrenadeRoundEntity>> BOUNCY_GRENADE_ROUND = registerBasic("bouncy_grenade_round", BouncyGrenadeRoundEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableGrenadeEntity>> THROWABLE_GRENADE = registerBasic("throwable_grenade", ThrowableGrenadeEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableStunGrenadeEntity>> THROWABLE_STUN_GRENADE = registerBasic("throwable_stun_grenade", ThrowableStunGrenadeEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableMolotovCocktailEntity>> THROWABLE_MOLOTOV_COCKTAIL = registerBasic("throwable_molotov_cocktail", ThrowableMolotovCocktailEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableGasGrenadeEntity>> THROWABLE_GAS_GRENADE = registerBasic("throwable_gas_grenade", ThrowableGasGrenadeEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableBeaconGrenadeEntity>> THROWABLE_BEACON_GRENADE = registerBasic("throwable_beacon_grenade", ThrowableBeaconGrenadeEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableChokeBombEntity>> THROWABLE_CHOKE_BOMB = registerBasic("throwable_choke_bomb", ThrowableChokeBombEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableSwarmBombEntity>> THROWABLE_SWARM_BOMB = registerBasic("throwable_swarm_bomb", ThrowableSwarmBombEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableShotballEntity>> THROWABLE_SHOTBALL = registerBasic("throwable_shotball", ThrowableShotballEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableNailBombEntity>> THROWABLE_NAIL_BOMB = registerBasic("throwable_nail_bomb", ThrowableNailBombEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableHellfireBombEntity>> THROWABLE_HELLFIRE_BOMB = registerBasic("throwable_hellfire_bomb", ThrowableHellfireBombEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<RaidFlareEntity>> RAID_FLARE = REGISTER.register("raid_flare", () ->
            EntityType.Builder.<RaidFlareEntity>of(RaidFlareEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .setTrackingRange(64)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("raid_flare"));
    /* Mobs */
    public static final DeferredHolder<EntityType<?>, EntityType<CogMinionEntity>> COG_MINION = REGISTER.register("cog_minion", () -> EntityType.Builder.of(CogMinionEntity::new, MobCategory.MONSTER).sized(0.8F, 2.0F).build("cog_minion"));
    public static final DeferredHolder<EntityType<?>, EntityType<CogKnightEntity>> COG_KNIGHT = REGISTER.register("cog_knight", () -> EntityType.Builder.of(CogKnightEntity::new, MobCategory.MONSTER).sized(0.8F, 2.2F).build("cog_knight"));
    public static final DeferredHolder<EntityType<?>, EntityType<SkyCarrierEntity>> SKY_CARRIER = REGISTER.register("sky_carrier", () -> EntityType.Builder.of(SkyCarrierEntity::new, MobCategory.MONSTER).sized(1.4F, 1.7F).build("sky_carrier"));
    public static final DeferredHolder<EntityType<?>, EntityType<HiveEntity>> HIVE = REGISTER.register("hive", () -> EntityType.Builder.of(HiveEntity::new, MobCategory.MONSTER).sized(0.8F, 2.0F).build("hive"));
    public static final DeferredHolder<EntityType<?>, EntityType<SwarmEntity>> SWARM = REGISTER.register("swarm", () -> EntityType.Builder.of(SwarmEntity::new, MobCategory.MONSTER).sized(0.8F, 2.0F).build("swarm"));
    public static final DeferredHolder<EntityType<?>, EntityType<RedcoatEntity>> REDCOAT = REGISTER.register("redcoat", () -> EntityType.Builder.of(RedcoatEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F).build("redcoat"));
    public static final DeferredHolder<EntityType<?>, EntityType<SupplyScampEntity>> SUPPLY_SCAMP = REGISTER.register("supply_scamp", () -> EntityType.Builder.of(SupplyScampEntity::new, MobCategory.CREATURE).sized(1.0F, 1.3F).build("supply_scamp"));
    public static final DeferredHolder<EntityType<?>, EntityType<DissidentEntity>> DISSIDENT = REGISTER.register("dissident", () -> EntityType.Builder.of(DissidentEntity::new, MobCategory.MONSTER).sized(1.4F, 1.7F).build("dissident"));
    public static final DeferredHolder<EntityType<?>, EntityType<HornlinEntity>> HORNLIN = REGISTER.register("hornlin", () -> EntityType.Builder.of(HornlinEntity::new, MobCategory.MONSTER).sized(1.4F, 1.7F).build("hornlin"));
    public static final DeferredHolder<EntityType<?>, EntityType<ZombifiedHornlinEntity>> ZOMBIFIED_HORNLIN = REGISTER.register("zombified_hornlin", () -> EntityType.Builder.of(ZombifiedHornlinEntity::new, MobCategory.MONSTER).sized(1.4F, 1.7F).build("zombified_hornlin"));
    public static final DeferredHolder<EntityType<?>, EntityType<TheMerchantEntity>> THE_MERCHANT = REGISTER.register("the_merchant", () ->
            EntityType.Builder.of((EntityType<TheMerchantEntity> entityType, Level level) -> new TheMerchantEntity(entityType, level), MobCategory.MONSTER)
                    .sized(1.5F, 2.25F)
                    .build("the_merchant"));
    public static final DeferredHolder<EntityType<?>, EntityType<BlundererEntity>> BLUNDERER = REGISTER.register("blunderer", () -> EntityType.Builder.of(BlundererEntity::new, MobCategory.MONSTER).sized(0.8F, 1.7F).build("blunderer"));
    public static final DeferredHolder<EntityType<?>, EntityType<AdjudicatorEntity>> ADJUDICATOR = REGISTER.register("adjudicator", () -> EntityType.Builder.of(AdjudicatorEntity::new, MobCategory.MONSTER).sized(0.8F, 2.0F).build("adjudicator"));
    public static final DeferredHolder<EntityType<?>, EntityType<SubjugatorEntity>> SUBJUGATOR = REGISTER.register("subjugator", () -> EntityType.Builder.of(SubjugatorEntity::new, MobCategory.MONSTER).sized(0.8F, 2.0F).build("subjugator"));
    public static final DeferredHolder<EntityType<?>, EntityType<FinforcerEntity>> FINFORCER = REGISTER.register("finforcer", () -> EntityType.Builder.of(FinforcerEntity::new, MobCategory.MONSTER).sized(0.8F, 2.0F).build("finforcer"));
    public static final DeferredHolder<EntityType<?>, EntityType<PraetorEntity>> PRAETOR = REGISTER.register("praetor", () -> EntityType.Builder.of(PraetorEntity::new, MobCategory.MONSTER).sized(1.2F, 2.8F).build("praetor"));
    public static final DeferredHolder<EntityType<?>, EntityType<MotherGhastEntity>> MOTHER_GHAST = REGISTER.register("mother_ghast", () -> EntityType.Builder.of(MotherGhastEntity::new, MobCategory.MONSTER).sized(4.0F, 4.0F).fireImmune().clientTrackingRange(10).build("mother_ghast"));
    public static final DeferredHolder<EntityType<?>, EntityType<ViventrumEntity>> VIVENTRUM = REGISTER.register("viventrum", () -> EntityType.Builder.of(ViventrumEntity::new, MobCategory.CREATURE).sized(0.8F, 1.2F).build("viventrum"));
    public static final DeferredHolder<EntityType<?>, EntityType<SulfurheadEntity>> SULFURHEAD = REGISTER.register("sulfurhead", () -> EntityType.Builder.of(SulfurheadEntity::new, MobCategory.MONSTER).sized(0.9F, 1.2F).build("sulfurhead"));
    public static final DeferredHolder<EntityType<?>, EntityType<TraumaUnitEntity>> TRAUMA_UNIT = REGISTER.register("trauma_unit", () -> EntityType.Builder.of(TraumaUnitEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F).build("trauma_unit"));
    public static final DeferredHolder<EntityType<?>, EntityType<ScampTankEntity>> SCAMP_TANK = REGISTER.register("scamp_tank", () -> EntityType.Builder.of(ScampTankEntity::new, MobCategory.MONSTER).sized(5.0F, 4F).build("scamp_tank"));
    public static final DeferredHolder<EntityType<?>, EntityType<SignalBeaconEntity>> SIGNAL_BEACON = REGISTER.register("signal_beacon", () -> EntityType.Builder.of(SignalBeaconEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).build("signal_beacon"));
    public static final DeferredHolder<EntityType<?>, EntityType<ScamplerEntity>> SCAMPLER = REGISTER.register("scampler", () -> EntityType.Builder.of(ScamplerEntity::new, MobCategory.MONSTER).sized(1.0F, 1.0F).build("scampler"));
    public static final DeferredHolder<EntityType<?>, EntityType<BeaconProjectileEntity>> BEACON_PROJECTILE = REGISTER.register("beacon_projectile", () ->
            EntityType.Builder.<BeaconProjectileEntity>of(BeaconProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .setTrackingRange(64)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("beacon_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<TraumaHookEntity>> TRAUMA_HOOK = REGISTER.register("trauma_hook", () -> EntityType.Builder.<TraumaHookEntity>of(TraumaHookEntity::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .setShouldReceiveVelocityUpdates(true)
            .build("trauma_hook"));

    public static final DeferredHolder<EntityType<?>, EntityType<BrassBoltEntity>> BRASS_BOLT = REGISTER.register("brass_bolt", () ->
            EntityType.Builder.<BrassBoltEntity>of(BrassBoltEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .setTrackingRange(64)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("brass_bolt"));
    public static final DeferredHolder<EntityType<?>, EntityType<ScampRocketEntity>> SCAMP_ROCKET = REGISTER.register("scamp_rocket", () ->
            EntityType.Builder.<ScampRocketEntity>of(ScampRocketEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .setTrackingRange(64)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("scamp_rocket"));
    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> registerBasic(String id, BiFunction<EntityType<T>, Level, T> function)
    {
        return REGISTER.register(id, () -> EntityType.Builder.of(function::apply, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .setTrackingRange(100)
                .setUpdateInterval(1)
                .noSummon()
                .fireImmune()
                .noSave()
                .setShouldReceiveVelocityUpdates(true).build(id));
    }

    private static <T extends ProjectileEntity> DeferredHolder<EntityType<?>, EntityType<T>> registerProjectile(String id, BiFunction<EntityType<T>, Level, T> function)
    {
        return REGISTER.register(id, () -> EntityType.Builder.of(function::apply, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .setTrackingRange(0)
                .noSummon()
                .fireImmune()
                .noSave()
                .setShouldReceiveVelocityUpdates(false)
                .build(id));
    }

    @EventBusSubscriber(modid = Reference.MOD_ID)
    public static class SpawnBlocker
    {
        @SubscribeEvent
        public static void onMobSpawn(MobSpawnEvent.FinalizeSpawn event)
        {
            Entity entity = event.getEntity();
            if (entity instanceof CogMinionEntity ||
                entity instanceof CogKnightEntity ||
                entity instanceof SkyCarrierEntity ||
                entity instanceof HiveEntity ||
                entity instanceof SwarmEntity ||
                entity instanceof RedcoatEntity ||
                entity instanceof DissidentEntity ||
                entity instanceof HornlinEntity ||
                entity instanceof ZombifiedHornlinEntity ||
                entity instanceof TheMerchantEntity ||
                entity instanceof BlundererEntity ||
                entity instanceof AdjudicatorEntity ||
                entity instanceof SubjugatorEntity ||
                entity instanceof FinforcerEntity ||
                entity instanceof PraetorEntity ||
                entity instanceof MotherGhastEntity ||
                entity instanceof SulfurheadEntity ||
                entity instanceof TraumaUnitEntity ||
                entity instanceof ScampTankEntity ||
                entity instanceof ScamplerEntity)
            {
                event.setCanceled(true);
            }
        }
    }
}
