package io.github.lightman314.lightmanscurrency.common.traders.item;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.item.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.item.restrictions.TicketKioskRestriction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ItemTraderDataTicket extends ItemTraderData {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "item_trader_ticket");
	
	public ItemTraderDataTicket() { super(TYPE); }
	
	public ItemTraderDataTicket(int tradeCount, Level level, BlockPos pos) { super(TYPE, tradeCount, level, pos); }
	
	@Override
	protected ItemTradeRestriction getTradeRestriction(int tradeIndex) { return TicketKioskRestriction.INSTANCE; }
	
}