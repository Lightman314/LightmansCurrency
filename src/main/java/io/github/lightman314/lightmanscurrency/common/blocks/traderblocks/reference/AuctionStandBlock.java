package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.reference;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.common.blockentity.AuctionStandBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IEasyEntityBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.blocks.EasyBlock;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.BlockValidator;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
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

import javax.annotation.Nonnull;
import java.util.Collection;

public class AuctionStandBlock extends EasyBlock implements IEasyEntityBlock {

    public AuctionStandBlock(Properties properties) { super(properties); }

    @Override
    protected boolean isBlockOpaque() { return false; }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) { return LazyShapes.BOX; }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult result) {
        if(!level.isClientSide && AuctionHouseTrader.isEnabled())
        {
            if(QuarantineAPI.IsDimensionQuarantined(level))
                EasyText.sendMessage(player, LCText.MESSAGE_DIMENSION_QUARANTINED_TERMINAL.getWithStyle(ChatFormatting.GOLD));
            else
            {
                TraderData ah = TraderSaveData.GetAuctionHouse(false);
                if(ah != null)
                    ah.openTraderMenu(player, BlockValidator.of(pos, this));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nonnull
    @Override
    public Collection<BlockEntityType<?>> getAllowedTypes() { return Lists.newArrayList(ModBlockEntities.AUCTION_STAND.get()); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) { return new AuctionStandBlockEntity(pos, state); }

    @Override
    public void playerWillDestroy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player player) {
        super.playerWillDestroy(level, pos, state, player);
        //Flag it to not drop if the player was in creative mode
        if(player.isCreative() && level.getBlockEntity(pos) instanceof AuctionStandBlockEntity be)
            be.dropItem = false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, BlockState newState, boolean flag) {
        if(state.is(newState.getBlock()))
        {
            super.onRemove(state, level, pos, newState, flag);
            return;
        }
        //Drop myself
        if(level.getBlockEntity(pos) instanceof AuctionStandBlockEntity be && be.dropItem)
            InventoryUtil.dumpContents(level, pos, new ItemStack(this));

    }
}