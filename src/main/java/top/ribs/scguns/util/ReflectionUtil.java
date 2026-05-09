package top.ribs.scguns.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Author: MrCrayfish
 */
public class ReflectionUtil
{
    private static final Method updateRedstoneOutputMethod = findUpdateRedstoneOutputMethod();

    public static int updateTargetBlock(TargetBlock block, LevelAccessor accessor, BlockState state, BlockHitResult result, Entity entity)
    {
        try
        {
            return (int) updateRedstoneOutputMethod.invoke(null, accessor, state, result, entity);
        }
        catch(IllegalAccessException | InvocationTargetException ignored)
        {
            return 0;
        }
    }

    private static Method findUpdateRedstoneOutputMethod() {
        try {
            Method method = TargetBlock.class.getDeclaredMethod("updateRedstoneOutput", LevelAccessor.class, BlockState.class, BlockHitResult.class, Entity.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException namedMissing) {
            try {
                Method method = TargetBlock.class.getDeclaredMethod("m_57391_", LevelAccessor.class, BlockState.class, BlockHitResult.class, Entity.class);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException obfuscatedMissing) {
                throw new IllegalStateException("Unable to find TargetBlock redstone update method", obfuscatedMissing);
            }
        }
    }
}
