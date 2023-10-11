package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.paygate;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.paygate.PaygateTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PaygateTradeEditTab extends TraderStorageTab {

	public PaygateTradeEditTab(TraderStorageMenu menu) { super(menu); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(Object screen) { return new PaygateTradeEditClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.EDIT_TRADES); }
	
	private int tradeIndex = -1;
	public int getTradeIndex() { return this.tradeIndex; }
	public PaygateTradeData getTrade() { 
		if(this.menu.getTrader() instanceof PaygateTraderData paygate)
		{
			if(this.tradeIndex >= paygate.getTradeCount() || this.tradeIndex < 0)
			{
				this.menu.changeTab(TraderStorageTab.TAB_TRADE_BASIC);
				this.menu.SendMessage(this.menu.createTabChangeMessage(TraderStorageTab.TAB_TRADE_BASIC));
				return null;
			}
			return paygate.getTrade(this.tradeIndex);
		}
		return null;
	}
	
	@Override
	public void onTabOpen() { }

	@Override
	public void onTabClose() { }

	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }
	
	public void setTradeIndex(int tradeIndex) { this.tradeIndex = tradeIndex; }
	
	public void setPrice(CoinValue price) {
		PaygateTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setCost(price);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
				this.menu.SendMessage(LazyPacketData.simpleTag("NewPrice", price.save()));
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
				this.menu.SendMessage(LazyPacketData.builder()
						.setBoolean("NewTicket", true)
						.setCompound("Ticket", ticket.save(new CompoundTag())));
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
				this.menu.SendMessage(LazyPacketData.simpleInt("NewDuration", duration));
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
				this.menu.SendMessage(LazyPacketData.simpleBoolean("StoreTicketStubs", storeTicketStubs));
		}
	}

	@Override
	public void receiveMessage(LazyPacketData message) {
		if(message.contains("TradeIndex"))
		{
			this.tradeIndex = message.getInt("TradeIndex");
		}
		else if(message.contains("NewPrice"))
		{
			this.setPrice(CoinValue.load(message.getNBT("NewPrice")));
		}
		else if(message.contains("NewTicket"))
		{
			ItemStack ticket = ItemStack.EMPTY;
			if(message.contains("Ticket"))
				ticket = ItemStack.of(message.getNBT("Ticket"));
			this.setTicket(ticket);
		}
		else if(message.contains("NewDuration"))
		{
			this.setDuration(message.getInt("NewDuration"));
		}
		else if(message.contains("StoreTicketStubs"))
		{
			this.setTicketStubHandling(message.getBoolean("StoreTicketStubs"));
		}
	}

}
