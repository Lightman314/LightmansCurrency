package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IBookTraderBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderDataBook;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BookTraderBlockEntity extends ItemTraderBlockEntity {

    public BookTraderBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.BOOK_TRADER.get(), pos, state); }
    public BookTraderBlockEntity(BlockPos pos, BlockState state, int count) { super(ModBlockEntities.BOOK_TRADER.get(), pos, state, count); }

    @Nonnull
    @Override
    public ItemTraderData buildNewTrader() { return new ItemTraderDataBook(this.tradeCount, this.level, this.worldPosition); }

    @OnlyIn(Dist.CLIENT)
    public Vector3f GetBookRenderPos(int tradeSlot)
    {
        if(this.getBlockState().getBlock() instanceof IBookTraderBlock block)
            return block.GetBookRenderPos(tradeSlot, this.getBlockState());
        return new Vector3f(0f,0f,0f);
    }

    @OnlyIn(Dist.CLIENT)
    public List<Quaternionf> GetBookRenderRot(int tradeSlot)
    {
        if(this.getBlockState().getBlock() instanceof IBookTraderBlock block)
            return block.GetBookRenderRot(tradeSlot, this.getBlockState());
        return new ArrayList<>();
    }

    @OnlyIn(Dist.CLIENT)
    public float GetBookRenderScale(int tradeSlot)
    {
        if(this.getBlockState().getBlock() instanceof IBookTraderBlock block)
            return block.GetBookRenderScale(tradeSlot, this.getBlockState());
        return 1f;
    }

    @OnlyIn(Dist.CLIENT)
    public int maxRenderIndex()
    {
        if(this.getBlockState().getBlock() instanceof IBookTraderBlock block)
            return block.maxRenderIndex();
        return 0;
    }

}
