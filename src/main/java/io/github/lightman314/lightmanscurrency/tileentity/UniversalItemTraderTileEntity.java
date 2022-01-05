package io.github.lightman314.lightmanscurrency.tileentity;

import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.entity.player.PlayerEntity;

public class UniversalItemTraderTileEntity extends UniversalTraderTileEntity{

	int tradeCount = 1;
	
	public UniversalItemTraderTileEntity()
	{
		super(ModTileEntities.UNIVERSAL_ITEM_TRADER);
	}
	
	public UniversalItemTraderTileEntity(int tradeCount)
	{
		this();
		this.tradeCount = tradeCount;
	}

	@Override
	protected UniversalTraderData createInitialData(PlayerEntity owner) {
		return new UniversalItemTraderData(PlayerReference.of(owner), this.pos, this.world.getDimensionKey(), this.traderID, this.tradeCount);
	}
	
	@Override
	protected void dumpContents(UniversalTraderData data)
	{
		super.dumpContents(data);
		if(data instanceof UniversalItemTraderData)
		{
			UniversalItemTraderData itemData = (UniversalItemTraderData)data;
			InventoryUtil.dumpContents(world, pos, itemData.getStorage());
		}
	}
	
}
