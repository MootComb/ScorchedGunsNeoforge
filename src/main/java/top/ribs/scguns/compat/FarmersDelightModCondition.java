package top.ribs.scguns.compat;

import com.mojang.serialization.MapCodec;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;

public class FarmersDelightModCondition implements ICondition {
    public static final String ID = "farmers_delight_mod_loaded";
    public static final MapCodec<FarmersDelightModCondition> CODEC = MapCodec.unit(FarmersDelightModCondition::new);

    @Override
    public boolean test(IContext context) {
        return ModList.get().isLoaded("farmersdelight");
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
