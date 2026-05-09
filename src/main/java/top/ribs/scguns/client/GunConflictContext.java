package top.ribs.scguns.client;

import com.mrcrayfish.controllable.client.binding.context.BindingContext;
import com.mrcrayfish.controllable.client.binding.context.InGameContext;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import top.ribs.scguns.item.GunItem;


/**
 * Author: MrCrayfish
 */
public enum GunConflictContext implements IKeyConflictContext {
    IN_GAME_HOLDING_WEAPON {
        public boolean isActive() {
            return !KeyConflictContext.GUI.isActive() && Minecraft.getInstance().player != null && Minecraft.getInstance().player.getMainHandItem().getItem() instanceof GunItem;
        }

        public boolean conflicts(IKeyConflictContext other) {
            return this == other;
        }
    };

    private final BindingContext bindingContext = InGameContext.INSTANCE;

    private GunConflictContext() {
    }

    public BindingContext bindingContext() {
        return this.bindingContext;
    }
}
