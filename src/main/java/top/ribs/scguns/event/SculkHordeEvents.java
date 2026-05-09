package top.ribs.scguns.event;


import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.entity.projectile.FireRoundEntity;
import top.ribs.scguns.entity.projectile.ProjectileEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class SculkHordeEvents {
    private static final ResourceLocation PURITY_EFFECT = ResourceLocation.fromNamespaceAndPath("sculkhorde", "purity");
    private static Method tryToCureBlockMethod;
    private static Constructor<?> cursorSurfacePurifierConstructor;
    private static boolean sculkReflectionUnavailable;

    //Doing this in a separate event, so we can do this optionally.
    @SubscribeEvent
    public static void onProjectileHit(GunProjectileHitEvent event) {
        if (event.getProjectile().level().isClientSide) {
            return;
        }

        ProjectileEntity projectile = event.getProjectile();
        if (!(projectile instanceof FireRoundEntity)) return;

        Level level = projectile.level();
        HitResult result = event.getRayTrace();


        if (result.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockResult = (BlockHitResult) result;

            SpawnCursorAndDisinfect(1, blockResult.getBlockPos(), level);
        }

        if (result.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entResult = (EntityHitResult) result;
            Entity entity = entResult.getEntity();

            if (entity instanceof LivingEntity livingEntity) {
                BuiltInRegistries.MOB_EFFECT.getHolder(PURITY_EFFECT)
                        .map(holder -> (Holder<MobEffect>) holder)
                        .ifPresent(holder -> livingEntity.addEffect(new MobEffectInstance(holder, 20 * 10)));
            }

            SpawnCursorAndDisinfect(1, entity.blockPosition(), level);
        }
    }

    private static void SpawnCursorAndDisinfect(int size, BlockPos pos, Level level) {
        for (int y = -size; y < size; y++) {
            for (int x = -size; x < size; x++) {
                for (int z = -size; z < size; z++) {
                    BlockPos new_pos = new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    tryToCureBlock((ServerLevel) level, new_pos);
                }
            }
        }

        if (Math.random() >= 0.33) {
            spawnCursorSurfacePurifier(level, pos);
        }
    }

    private static void tryToCureBlock(ServerLevel level, BlockPos pos) {
        if (sculkReflectionUnavailable) {
            return;
        }
        try {
            if (tryToCureBlockMethod == null) {
                Class<?> system = Class.forName("com.github.sculkhorde.systems.infestation_systems.block_infestation_system.BlockInfestationSystem");
                tryToCureBlockMethod = system.getMethod("tryToCureBlock", ServerLevel.class, BlockPos.class);
            }
            tryToCureBlockMethod.invoke(null, level, pos);
        } catch (ReflectiveOperationException exception) {
            sculkReflectionUnavailable = true;
            ScorchedGuns.LOGGER.warn("Sculk Horde block cure bridge is unavailable; fire-round purification is skipped.", exception);
        }
    }

    private static void spawnCursorSurfacePurifier(Level level, BlockPos pos) {
        if (sculkReflectionUnavailable) {
            return;
        }
        try {
            if (cursorSurfacePurifierConstructor == null) {
                Class<?> cursorClass = Class.forName("com.github.sculkhorde.common.entity.infection.CursorSurfacePurifierEntity");
                cursorSurfacePurifierConstructor = cursorClass.getConstructor(Level.class);
            }
            Object cursor = cursorSurfacePurifierConstructor.newInstance(level);
            invoke(cursor, "setPos", new Class<?>[]{double.class, double.class, double.class}, pos.getCenter().x, pos.getCenter().y, pos.getCenter().z);
            invoke(cursor, "setMaxTransformations", new Class<?>[]{int.class}, 8);
            invoke(cursor, "setMaxRange", new Class<?>[]{int.class}, 50);
            invoke(cursor, "setSearchIterationsPerTick", new Class<?>[]{int.class}, 5);
            invoke(cursor, "setMaxLifeTimeMillis", new Class<?>[]{int.class}, 10000 / 2);
            invoke(cursor, "setTickIntervalMilliseconds", new Class<?>[]{int.class}, 150);
            if (cursor instanceof Entity entity) {
                level.addFreshEntity(entity);
            }
        } catch (ReflectiveOperationException exception) {
            sculkReflectionUnavailable = true;
            ScorchedGuns.LOGGER.warn("Sculk Horde cursor purifier bridge is unavailable; cursor spawning is skipped.", exception);
        }
    }

    private static void invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws ReflectiveOperationException {
        Method method = target.getClass().getMethod(methodName, parameterTypes);
        method.invoke(target, args);
    }
}
