package top.ribs.scguns.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class ShieldGeneratorCompat {
    private static final String MOD_ID = "shield_generators";
    private static boolean initialized;
    private static boolean unavailable;
    private static Field shieldsField;
    private static Method clipShellMethod;
    private static Method hitWorldMethod;
    private static Method hitNormalMethod;

    private ShieldGeneratorCompat() {
    }

    @Nullable
    public static BlockHitResult clip(Level level, ClipContext context) {
        if (!(level instanceof ServerLevel serverLevel) || !isAvailable()) {
            return null;
        }
        try {
            Object shieldsByDimension = shieldsField.get(null);
            if (!(shieldsByDimension instanceof Map<?, ?> dimensions)) {
                return null;
            }
            Object shieldsForLevel = dimensions.get(serverLevel.dimension());
            if (!(shieldsForLevel instanceof Map<?, ?> shields) || shields.isEmpty()) {
                return null;
            }

            Vec3 start = context.getFrom();
            Vec3 end = context.getTo();
            ShieldHitResult nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            Collection<?> instances = shields.values();
            for (Object instance : instances) {
                Object optional = clipShellMethod.invoke(instance, start, end);
                if (!(optional instanceof Optional<?> hitOptional) || hitOptional.isEmpty()) {
                    continue;
                }
                Object hit = hitOptional.get();
                Object worldPos = hitWorldMethod.invoke(hit);
                if (!(worldPos instanceof Vec3 location)) {
                    continue;
                }
                Object normalPos = hitNormalMethod.invoke(hit);
                Direction direction = normalPos instanceof Vec3 normal
                        ? Direction.getNearest(normal.x, normal.y, normal.z)
                        : Direction.getNearest(start.x - end.x, start.y - end.y, start.z - end.z);
                double distance = start.distanceToSqr(location);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = new ShieldHitResult(location, direction, BlockPos.containing(location));
                }
            }
            return nearest;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            unavailable = true;
            return null;
        }
    }

    public static boolean isShieldHit(HitResult result) {
        return result instanceof ShieldHitResult;
    }

    private static boolean isAvailable() {
        if (unavailable || !ModList.get().isLoaded(MOD_ID)) {
            return false;
        }
        if (initialized) {
            return true;
        }
        try {
            Class<?> registryClass = Class.forName("com.anton.shieldgenerators.ShieldRegistry");
            shieldsField = registryClass.getDeclaredField("SHIELDS");
            shieldsField.setAccessible(true);

            Class<?> shieldInstanceClass = Class.forName("com.anton.shieldgenerators.ShieldRegistry$ShieldInstance");
            clipShellMethod = shieldInstanceClass.getDeclaredMethod("clipShell", Vec3.class, Vec3.class);
            clipShellMethod.setAccessible(true);

            Class<?> shieldHitClass = Class.forName("com.anton.shieldgenerators.ShieldRegistry$ShieldHit");
            hitWorldMethod = shieldHitClass.getMethod("world");
            hitNormalMethod = shieldHitClass.getMethod("normal");

            initialized = true;
            return true;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            unavailable = true;
            return false;
        }
    }

    private static class ShieldHitResult extends BlockHitResult {
        ShieldHitResult(Vec3 location, Direction direction, BlockPos blockPos) {
            super(location, direction, blockPos, false);
        }
    }
}
