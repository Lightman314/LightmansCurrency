package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.ItemPositionData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemTraderBlockEntity extends TraderBlockEntity<ItemTraderData> {

	protected int tradeCount;
	protected boolean networkTrader;
	
	public ItemTraderBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.ITEM_TRADER.get(), pos, state, 1, false); }
	
	public ItemTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount) { this(ModBlockEntities.ITEM_TRADER.get(), pos, state, tradeCount, false); }
	
	public ItemTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount, boolean networkTrader) { this(ModBlockEntities.ITEM_TRADER.get(), pos, state, tradeCount, networkTrader); }
	
	protected ItemTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { this(type, pos, state, 1, false);}
	
	protected ItemTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int tradeCount) { this(type, pos, state, tradeCount, false); }
	
	protected ItemTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int tradeCount, boolean networkTrader)
	{
		super(type, pos, state);
		this.tradeCount = tradeCount;
		this.networkTrader = networkTrader;
	}

	@Nullable
	@Override
	protected ItemTraderData castOrNullify(@Nonnull TraderData trader) {
		if(trader instanceof ItemTraderData it)
			return it;
		return null;
	}

	@Nonnull
    public ItemTraderData buildNewTrader() {
		ItemTraderData trader = new ItemTraderData(this.tradeCount, this.level, this.worldPosition);
		if(this.networkTrader)
			trader.setAlwaysShowOnTerminal();
		return trader;
	}

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public ItemPositionData GetRenderData()
	{
		if(this.getBlockState().getBlock() instanceof IItemTraderBlock traderBlock)
			return traderBlock.getItemPositionData();
		return ItemPositionData.EMPTY;
	}
	
	@Override
	public void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
	{
		super.saveAdditional(compound,lookup);
		compound.putInt("TradeCount", this.tradeCount);
		compound.putBoolean("NetworkTrader", this.networkTrader);
	}
	
	@Override
	public void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
	{
		super.loadAdditional(compound,lookup);
		this.tradeCount = compound.getInt("TradeCount");
		this.networkTrader = compound.getBoolean("NetworkTrader");
	}
	
}
