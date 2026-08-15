package top.ribs.scguns.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.*;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = Reference.MOD_ID)
public class RemoveExistingMobsHandler 
{
    private static final Set<Class<?>> MOB_CLASSES = new HashSet<>();
    private static boolean firstTick = true;

    static {
        MOB_CLASSES.add(CogMinionEntity.class);
        MOB_CLASSES.add(CogKnightEntity.class);
        MOB_CLASSES.add(SkyCarrierEntity.class);
        MOB_CLASSES.add(HiveEntity.class);
        MOB_CLASSES.add(SwarmEntity.class);
        MOB_CLASSES.add(RedcoatEntity.class);
        MOB_CLASSES.add(SupplyScampEntity.class);
        MOB_CLASSES.add(DissidentEntity.class);
        MOB_CLASSES.add(HornlinEntity.class);
        MOB_CLASSES.add(ZombifiedHornlinEntity.class);
        MOB_CLASSES.add(TheMerchantEntity.class);
        MOB_CLASSES.add(BlundererEntity.class);
        MOB_CLASSES.add(AdjudicatorEntity.class);
        MOB_CLASSES.add(SubjugatorEntity.class);
        MOB_CLASSES.add(FinforcerEntity.class);
        MOB_CLASSES.add(PraetorEntity.class);
        MOB_CLASSES.add(MotherGhastEntity.class);
        MOB_CLASSES.add(ViventrumEntity.class);
        MOB_CLASSES.add(SulfurheadEntity.class);
        MOB_CLASSES.add(TraumaUnitEntity.class);
        MOB_CLASSES.add(ScampTankEntity.class);
        MOB_CLASSES.add(SignalBeaconEntity.class);
        MOB_CLASSES.add(ScamplerEntity.class);
    }

    private static boolean isScorchedGunsMob(Entity entity)
    {
        for (Class<?> mobClass : MOB_CLASSES)
        {
            if (mobClass.isInstance(entity))
            {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event)
    {
        Entity entity = event.getEntity();
        if (isScorchedGunsMob(entity))
        {
            entity.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event)
    {
        if (!firstTick)
        {
            return;
        }
        firstTick = false;

        ServerLevel level = event.getServer().overworld();
        
        for (Entity entity : level.getAllEntities())
        {
            if (isScorchedGunsMob(entity))
            {
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }
}
