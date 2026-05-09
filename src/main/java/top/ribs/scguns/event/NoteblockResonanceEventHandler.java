package top.ribs.scguns.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.ribs.scguns.Reference;
import top.ribs.scguns.block.ChargedAmethystRelayBlock;

@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class NoteblockResonanceEventHandler {

    @SubscribeEvent
    public static void onNoteBlockPlay(NoteBlockEvent.Play event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        BlockPos noteBlockPos = event.getPos();
        NoteBlockInstrument playedInstrument = event.getInstrument();

        int searchRadius = ChargedAmethystRelayBlock.LISTEN_RADIUS;
        int foundRelays = 0;

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);

                    if (distance <= searchRadius) {
                        BlockPos relayPos = noteBlockPos.offset(x, y, z);

                        if (serverLevel.getBlockState(relayPos).getBlock() instanceof ChargedAmethystRelayBlock relay) {
                            foundRelays++;
                            NoteBlockInstrument relayInstrument = serverLevel.getBlockState(relayPos).getValue(ChargedAmethystRelayBlock.INSTRUMENT);
                            relay.onInstrumentHeard(serverLevel, relayPos, playedInstrument);
                        }
                    }
                }
            }
        }
    }
}
