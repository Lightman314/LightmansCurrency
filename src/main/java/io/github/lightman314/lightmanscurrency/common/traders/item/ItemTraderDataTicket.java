package io.github.lightman314.lightmanscurrency.common.traders.item;

import io.github.lightman314.lightmanscurrency.api.traders.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.item.ticket.ItemTradeTicketEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.item.ticket.TicketItemTrade;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemTraderDataTicket extends ItemTraderData {

	public static final TraderType<ItemTraderDataTicket> TYPE = new TraderType<>(VersionUtil.lcResource("item_trader_ticket"), ItemTraderDataTicket::new);
	
	private ItemTraderDataTicket() { super(TYPE); }
	
	public ItemTraderDataTicket(int tradeCount, Level level, BlockPos pos) { super(TYPE, tradeCount, level, pos); }

	@Override
	protected Supplier<ItemTradeData> tradeBuilder(boolean validateRules) { return () -> new TicketItemTrade(validateRules); }

	@Override
	public void initStorageTabs(ITraderStorageMenu menu) {
		super.initStorageTabs(menu);
		//Override the trade edit tab with a custom one with a bonus code & recipe inputs
		menu.setTab(TraderStorageTab.TAB_TRADE_ADVANCED,new ItemTradeTicketEditTab(menu));
	}
}
