package io.github.lightman314.lightmanscurrency.features.cash_register;

import io.github.lightman314.lightmanscurrency.api.trader.world.block.TraderBlock;
import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CashRegisterItem extends BlockItem {

    public CashRegisterItem(Block block,Properties properties) {
        super(block, properties.component(LCDataComponents.TRADER_POSITIONS, TraderPositionSet.EMPTY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos lookPos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = level.getBlockState(lookPos);
        if(state.getBlock() instanceof TraderBlock block) {
            TraderBlockEntity be = block.getBlockEntity(level,state,lookPos);
            ItemStack heldItem = context.getItemInHand();
            if(be != null) {
                if(!this.hasBlockEntity(heldItem,be)) {
                    //Add to the trader position set
                    heldItem.update(LCDataComponents.TRADER_POSITIONS, TraderPositionSet.EMPTY, s -> s.withPosition(be.getBlockPos()));
                    if(level.isClientSide())
                        level.playSound(context.getPlayer(),be.getBlockPos(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL,1f,0f);
                }
                else {
                    if(level.isClientSide())
                        level.playSound(context.getPlayer(),be.getBlockPos(),SoundEvents.EXPERIENCE_ORB_PICKUP,SoundSource.NEUTRAL,1f,1.35f);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(context);
    }

    private boolean hasBlockEntity(ItemStack stack,TraderBlockEntity be) {
        return stack.getOrDefault(LCDataComponents.TRADER_POSITIONS, TraderPositionSet.EMPTY)
                .positions().contains(be.getBlockPos());
    }

}
