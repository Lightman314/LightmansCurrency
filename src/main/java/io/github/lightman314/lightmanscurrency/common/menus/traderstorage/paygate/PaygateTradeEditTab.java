package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.paygate;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.paygate.PaygateTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class PaygateTradeEditTab extends TraderStorageTab {

	public PaygateTradeEditTab(@Nonnull ITraderStorageMenu menu) { super(menu); }

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(@Nonnull Object screen) { return new PaygateTradeEditClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.EDIT_TRADES); }

	private int tradeIndex = -1;
	public int getTradeIndex() { return this.tradeIndex; }
	public PaygateTradeData getTrade() {
		if(this.menu.getTrader() instanceof PaygateTraderData paygate)
		{
			if(this.tradeIndex >= paygate.getTradeCount() || this.tradeIndex < 0)
			{
				this.menu.ChangeTab(TraderStorageTab.TAB_TRADE_BASIC);
				return null;
			}
			return paygate.getTrade(this.tradeIndex);
		}
		return null;
	}

	public void setPrice(MoneyValue price) {
		PaygateTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setCost(price);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setMoneyValue("NewPrice", price));
		}
	}

	public void setTicket(ItemStack ticket) {
		PaygateTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setTicket(ticket);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
			{
				this.menu.SendMessage(this.builder()
						.setBoolean("NewTicket", true)
						.setItem("Ticket", ticket));
			}
		}
	}

	public void setDuration(int duration) {
		PaygateTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setDuration(duration);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setInt("NewDuration", duration));
		}
	}

	public void setLevel(int level) {
		PaygateTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setRedstoneLevel(level);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setInt("NewLevel",level));
		}
	}

	public void setDescription(String description) {
		PaygateTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setDescription(description);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setString("NewDescription",description));
		}
	}

	public void setTicketStubHandling(boolean storeTicketStubs)
	{
		PaygateTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setStoreTicketStubs(storeTicketStubs);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setBoolean("StoreTicketStubs", storeTicketStubs));
		}
	}

	@Override
	public void OpenMessage(@Nonnull LazyPacketData message) {
		if(message.contains("TradeIndex"))
			this.tradeIndex = message.getInt("TradeIndex");
	}

	@Override
	public void receiveMessage(LazyPacketData message) {
		if(message.contains("NewPrice"))
		{
			this.setPrice(message.getMoneyValue("NewPrice"));
		}
		else if(message.contains("NewTicket"))
		{
			ItemStack ticket = ItemStack.EMPTY;
			if(message.contains("Ticket"))
				ticket = message.getItem("Ticket");
			this.setTicket(ticket);
		}
		else if(message.contains("NewDuration"))
		{
			this.setDuration(message.getInt("NewDuration"));
		}
		else if(message.contains("NewLevel"))
		{
			this.setLevel(message.getInt("NewLevel"));
		}
		else if(message.contains("NewDescription"))
		{
			this.setDescription(message.getString("NewDescription"));
		}
		else if(message.contains("StoreTicketStubs"))
		{
			this.setTicketStubHandling(message.getBoolean("StoreTicketStubs"));
		}
	}

}