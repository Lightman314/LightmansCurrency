package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.reference;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.common.blockentity.AuctionStandBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IEasyEntityBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.blocks.EasyBlock;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.data.types.TraderDataCache;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.BlockValidator;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class AuctionStandBlock extends EasyBlock implements IEasyEntityBlock, IVariantBlock {

    public AuctionStandBlock(Properties properties) { super(properties); }

    @Override
    protected boolean isBlockOpaque() { return false; }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return LazyShapes.BOX; }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result) {
        if(!level.isClientSide && AuctionHouseTrader.isEnabled())
        {
            if(QuarantineAPI.IsDimensionQuarantined(level))
                EasyText.sendMessage(player, LCText.MESSAGE_DIMENSION_QUARANTINED_TERMINAL.getWithStyle(ChatFormatting.GOLD));
            else
            {
                TraderDataCache data = TraderDataCache.TYPE.get(level.isClientSide);
                if(data != null)
                {
                    TraderData ah = data.getAuctionHouse();
                    if(ah != null)
                        ah.openTraderMenu(player, BlockValidator.of(pos, this));
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    
    @Override
    public Collection<BlockEntityType<?>> getAllowedTypes() { return Lists.newArrayList(ModBlockEntities.AUCTION_STAND.get()); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AuctionStandBlockEntity(pos, state); }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        //Flag it to not drop if the player was in creative mode
        if(player.isCreative() && level.getBlockEntity(pos) instanceof AuctionStandBlockEntity be)
            be.dropItem = false;
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean flag) {
        if(state.is(newState.getBlock()))
        {
            super.onRemove(state, level, pos, newState, flag);
            return;
        }
        //Drop myself
        if(level.getBlockEntity(pos) instanceof AuctionStandBlockEntity be && be.dropItem)
            InventoryUtil.dropContents(level, pos, new ItemStack(this));

    }
}