package top.ribs.scguns.client.screen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.screen.widget.ThermolithMenu;


public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Reference.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<AmmoBoxMenu>> AMMO_BOX =
            registerMenuType("ammo_box", AmmoBoxMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<ShellCatcherModuleMenu>> SHELL_CATCHER_MODULE =
            registerMenuType("shell_catcher_module", ShellCatcherModuleMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<AmmoModuleMenu>> AMMO_MODULE =
            registerMenuType("ammo_module", AmmoModuleMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<CryoniterMenu>> CRYONITER_MENU =
            registerMenuType("cryoniter_menu", CryoniterMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<VentCollectorMenu>> VENT_COLLECTOR_MENU =
            registerMenuType("vent_collector_menu", VentCollectorMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<ThermolithMenu>> THERMOLITH_MENU =
            registerMenuType("thermolith_menu", ThermolithMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<PolarGeneratorMenu>> POLAR_GENERATOR_MENU =
            registerMenuType("polar_generator_menu", PolarGeneratorMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MaceratorMenu>> MACERATOR_MENU =
            registerMenuType("macerator_menu", MaceratorMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<PoweredMaceratorMenu>> POWERED_MACERATOR_MENU =
            registerMenuType("powered_macerator_menu", PoweredMaceratorMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<BasicTurretMenu>> BASIC_TURRET_MENU =
            registerMenuType("basic_turret_menu", BasicTurretMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<ShotgunTurretMenu>> SHOTGUN_TURRET_MENU =
            registerMenuType("shotgun_turret_menu", ShotgunTurretMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<AutoTurretMenu>> AUTO_TURRET_MENU =
            registerMenuType("auto_turret_menu", AutoTurretMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<SniperTurretMenu>> SNIPER_TURRET_MENU =
            registerMenuType("sniper_turret_menu", SniperTurretMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<LightningBatteryMenu>> LIGHTING_BATTERY_MENU =
            registerMenuType("lightning_battery_menu", LightningBatteryMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MechanicalPressMenu>> MECHANICAL_PRESS_MENU =
            registerMenuType("mechanical_press_menu", MechanicalPressMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<PoweredMechanicalPressMenu>> POWERED_MECHANICAL_PRESS_MENU =
            registerMenuType("powered_mechanical_press_menu", PoweredMechanicalPressMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<ExoSuitMenu>> EXOSUIT_MENU =
            registerMenuType("exosuit_menu", ExoSuitMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<GunBenchMenu>> GUN_BENCH
            = registerMenuType("gun_bench", GunBenchMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<ChestMenu>> SUPPLY_SCAMP_MENU =
            registerMenuType("supply_scamp_menu", (id, playerInventory, buffer) ->
                    new ChestMenu(MenuType.GENERIC_9x3, id, playerInventory, new SimpleContainer(27), 3));

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
