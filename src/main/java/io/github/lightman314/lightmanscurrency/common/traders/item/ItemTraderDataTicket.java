package io.github.lightman314.lightmanscurrency.common.traders.item;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.TraderType;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.TicketKioskRestriction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ItemTraderDataTicket extends ItemTraderData {

	public static final TraderType<ItemTraderDataTicket> TYPE = new TraderType<>(new ResourceLocation(LightmansCurrency.MODID, "item_trader_ticket"), ItemTraderDataTicket::new);
	
	private ItemTraderDataTicket() { super(TYPE); }
	
	public ItemTraderDataTicket(int tradeCount, Level level, BlockPos pos) { super(TYPE, tradeCount, level, pos); }
	
	@Override
	protected ItemTradeRestriction getTradeRestriction(int tradeIndex) { return TicketKioskRestriction.INSTANCE; }
	
}
