package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.reference;

import io.github.lightman314.lightmanscurrency.common.blockentity.AuctionStandBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AuctionStandBlock extends Block {

    public AuctionStandBlock(Properties properties) { super(properties); }

    @Override
    public @Nonnull VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader level, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) { return LazyShapes.BOX_T;}

    @Override
    public @Nonnull ActionResultType use(@Nonnull BlockState state, @Nonnull World level, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult result) {
        if(!level.isClientSide && AuctionHouseTrader.isEnabled())
        {
            TraderData ah = TraderSaveData.GetAuctionHouse(false);
            if(ah != null)
                ah.openTraderMenu(player);
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean hasTileEntity(BlockState state) { return true; }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, IBlockReader world) { return new AuctionStandBlockEntity(); }

}