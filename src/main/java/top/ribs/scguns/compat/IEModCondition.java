package top.ribs.scguns.compat;

import com.mojang.serialization.MapCodec;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;

public class IEModCondition implements ICondition {
    public static final String ID = "immersiveengineering_mod_loaded";
    public static final MapCodec<IEModCondition> CODEC = MapCodec.unit(IEModCondition::new);

    @Override
    public boolean test(IContext context) {
        return ModList.get().isLoaded("immersiveengineering");
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
