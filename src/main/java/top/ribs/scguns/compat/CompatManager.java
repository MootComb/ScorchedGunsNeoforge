package top.ribs.scguns.compat;

import net.neoforged.fml.ModList;

public class CompatManager {
    public static final boolean SCULK_HORDE_LOADED = modLoaded("sculkhorde");


    public static boolean modLoaded(String modID) {
        return ModList.get().isLoaded(modID);
    }


}
