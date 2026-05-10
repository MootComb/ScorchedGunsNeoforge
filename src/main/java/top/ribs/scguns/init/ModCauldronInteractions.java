package top.ribs.scguns.init;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.RegisterCauldronFluidContentEvent;

public class ModCauldronInteractions {
    public static final CauldronInteraction.InteractionMap VICIOUS_ACID = CauldronInteraction.newInteractionMap("scguns_vicious_acid");

    public static void register() {
        CauldronInteraction.EMPTY.map().put(ModItems.VICIOUS_ACID_BUCKET.get(), ModCauldronInteractions::emptyViciousAcidBucket);
        VICIOUS_ACID.map().put(Items.BUCKET, ModCauldronInteractions::fillViciousAcidBucket);
    }

    public static void registerCauldronFluidContent(RegisterCauldronFluidContentEvent event) {
        event.register(ModBlocks.VICIOUS_ACID_CAULDRON.get(), ModFluids.VICIOUS_ACID_SOURCE.get(), FluidType.BUCKET_VOLUME, null);
    }

    private static ItemInteractionResult emptyViciousAcidBucket(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack filledStack) {
        if (!level.isClientSide) {
            Item item = filledStack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(filledStack, player, new ItemStack(Items.BUCKET)));
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(pos, ModBlocks.VICIOUS_ACID_CAULDRON.get().defaultBlockState());
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private static ItemInteractionResult fillViciousAcidBucket(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack emptyStack) {
        if (!level.isClientSide) {
            Item item = emptyStack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(emptyStack, player, new ItemStack(ModItems.VICIOUS_ACID_BUCKET.get())));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
