package top.ribs.scguns.compat;

import com.mojang.serialization.MapCodec;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;

public class CreateModCondition implements ICondition {
    public static final String ID = "create_mod_loaded";
    public static final MapCodec<CreateModCondition> CODEC = MapCodec.unit(CreateModCondition::new);

    @Override
    public boolean test(IContext context) {
        return ModList.get().isLoaded("create");
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
