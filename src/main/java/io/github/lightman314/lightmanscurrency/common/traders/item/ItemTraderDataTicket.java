package io.github.lightman314.lightmanscurrency.common.traders.item;

import io.github.lightman314.lightmanscurrency.api.traders.TraderType;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.TicketKioskRestriction;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class ItemTraderDataTicket extends ItemTraderData {

	public static final TraderType<ItemTraderDataTicket> TYPE = new TraderType<>(VersionUtil.lcResource("item_trader_ticket"), ItemTraderDataTicket::new);
	
	private ItemTraderDataTicket() { super(TYPE); }
	
	public ItemTraderDataTicket(int tradeCount, Level level, BlockPos pos) { super(TYPE, tradeCount, level, pos); }
	
	@Nonnull
	@Override
	protected ItemTradeRestriction getTradeRestriction(int tradeIndex) { return TicketKioskRestriction.INSTANCE; }
	
}
