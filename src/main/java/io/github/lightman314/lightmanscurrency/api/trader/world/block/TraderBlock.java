package io.github.lightman314.lightmanscurrency.api.trader.world.block;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderState;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.WorldNode;
import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.item.StoredTrader;
import io.github.lightman314.lightmanscurrency.api.world.block.EasyBlock;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IMultiBlock;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IProtectedBlock;
import io.github.lightman314.lightmanscurrency.api.world.data.WorldPosition;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.BlockValidator;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;

public abstract class TraderBlock extends EasyBlock implements EntityBlock, IProtectedBlock {

    public TraderBlock(Properties properties) { super(properties); }

    @Override
    @Nullable
    public abstract TraderBlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState);
    @Override
    @Nullable
    public final PushReaction getPistonPushReaction(BlockState state) { return PushReaction.BLOCK; }
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        super.setPlacedBy(level, pos, state, by, stack);
        if(level.isClientSide())
            return;
        if(by instanceof Player player && level.getBlockEntity(pos) instanceof TraderBlockEntity be)
            be.onTraderPlacement(player,stack);
    }

    @Nullable
    public TraderBlockEntity getBlockEntity(LevelAccessor level,BlockState state,BlockPos pos)
    {
        if(this instanceof IMultiBlock mb)
            pos = mb.getBlockEntityPosition(pos,state);
        if(level.getBlockEntity(pos) instanceof TraderBlockEntity be)
            return be;
        return null;
    }

    @Override
    public final boolean canBreakBlock(Level level,BlockState state,BlockPos pos,Player player) {
        TraderBlockEntity be = this.getBlockEntity(level,state,pos);
        return be == null || be.canBreakTrader(player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide())
            return InteractionResult.SUCCESS;
        TraderBlockEntity be = this.getBlockEntity(level,state,pos);
        if(be != null)
        {
            TraderData trader = be.getTrader();
            if(trader == null)
                LightmansCurrency.LogError("Trader at " + WorldPosition.ofLevel(level,pos) + " is missing its trader!");
            else
                trader.openStorageMenu(player,new BlockValidator(this,pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest, FluidState fluid) {
        if(this.canBreakBlock(level,state,pos,player))
        {
            if(level.isClientSide())
                return super.onDestroyedByPlayer(state,level,pos,player,toolStack,willHarvest,fluid);
            //Spawn the trader drops
            TraderBlockEntity be = this.getBlockEntity(level,state,pos);
            if(be != null)
            {
                //Flag this as a legal break
                be.flagAsLegalBreak();
                TraderData trader = be.getTrader();
                //Give the trader to the player
                ItemStack drop = new ItemStack(state.getBlock());
                if(trader != null)
                {
                    //Change the traders state to make it inaccessible to other players
                    trader.ifNodePresent(WorldNode.TYPE,node -> node.setState(TraderState.heldItemState(player)));
                    //Put the traders data into the item stack
                    drop.set(LCDataComponents.STORED_TRADER,new StoredTrader(trader));
                }
                player.getInventory().placeItemBackInInventory(drop);
            }
            return super.onDestroyedByPlayer(state,level,pos,player,toolStack,willHarvest,fluid);
        }
        else
            return false;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        TraderBlockEntity be = this.getBlockEntity(level,state,pos);
        if(be != null)
        {
            //End the tracking regardless of whether the break is legal
            be.tracking.clearAll();
            //Do nothing else if the break is legal
            if(be.isLegalBreak())
                return;
            TraderData trader = be.getTrader();
            if(trader != null)
            {
                //TODO eject the trader
            }
        }
    }

    @Override
    protected BlockState initializeDefaultState(BlockState state) {
        return super.initializeDefaultState(state);
    }
}
