package top.ribs.scguns.client;

import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.binding.handlers.EmptyHandler;
import com.mrcrayfish.controllable.client.input.Buttons;

/**
 * Author: MrCrayfish
 */
public class GunButtonBindings
{
    public static final ButtonBinding SHOOT = create(Buttons.RIGHT_TRIGGER, "scguns.button.shoot");
    public static final ButtonBinding AIM = create(Buttons.LEFT_TRIGGER, "scguns.button.aim");
    public static final ButtonBinding RELOAD = create(Buttons.X, "scguns.button.reload");
    public static final ButtonBinding OPEN_ATTACHMENTS = create(Buttons.B, "scguns.button.attachments");
    public static final ButtonBinding STEADY_AIM = create(Buttons.RIGHT_THUMB_STICK, "scguns.button.steadyAim");

    private static ButtonBinding create(int button, String description)
    {
        return new ButtonBinding(button, description, "button.categories.scguns", GunConflictContext.IN_GAME_HOLDING_WEAPON.bindingContext(), EmptyHandler.INSTANCE);
    }

    public static void register()
    {
        BindingRegistry.getInstance().register(SHOOT);
        BindingRegistry.getInstance().register(AIM);
        BindingRegistry.getInstance().register(RELOAD);
        BindingRegistry.getInstance().register(OPEN_ATTACHMENTS);
        BindingRegistry.getInstance().register(STEADY_AIM);

    }
}
