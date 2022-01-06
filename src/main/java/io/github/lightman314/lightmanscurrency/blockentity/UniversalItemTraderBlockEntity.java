package io.github.lightman314.lightmanscurrency.blockentity;

import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public class UniversalItemTraderBlockEntity extends UniversalTraderBlockEntity{

	int tradeCount = 1;
	
	public UniversalItemTraderBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.UNIVERSAL_ITEM_TRADER, pos, state);
	}
	
	public UniversalItemTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount)
	{
		this(pos, state);
		this.tradeCount = tradeCount;
	}

	@Override
	protected UniversalTraderData createInitialData(Entity owner) {
		return new UniversalItemTraderData(PlayerReference.of(owner), this.worldPosition, this.level.dimension(), this.traderID, this.tradeCount);
	}
	
	@Override
	protected void dumpContents(UniversalTraderData data)
	{
		super.dumpContents(data);
		if(data instanceof UniversalItemTraderData)
		{
			UniversalItemTraderData itemData = (UniversalItemTraderData)data;
			InventoryUtil.dumpContents(this.level, this.worldPosition, itemData.getStorage());
			//Removed as the trade inventory no longer consumes items
			//InventoryUtil.dumpContents(world, pos, new TradeInventory(itemData.getAllTrades()));
		}
	}
	
}
