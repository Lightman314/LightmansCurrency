package io.github.lightman314.lightmanscurrency.menus.traderstorage.paygate;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.paygate.PaygateTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.paygate.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PaygateTradeEditTab extends TraderStorageTab{

	public PaygateTradeEditTab(TraderStorageMenu menu) { super(menu); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderStorageClientTab<?> createClientTab(TraderStorageScreen screen) { return new PaygateTradeEditClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) { return this.menu.getTrader().hasPermission(player, Permissions.EDIT_TRADES); }
	
	private int tradeIndex = -1;
	public int getTradeIndex() { return this.tradeIndex; }
	public PaygateTradeData getTrade() { 
		if(this.menu.getTrader() instanceof PaygateTraderData paygate)
		{
			if(this.tradeIndex >= paygate.getTradeCount() || this.tradeIndex < 0)
			{
				this.menu.changeTab(TraderStorageTab.TAB_TRADE_BASIC);
				this.menu.sendMessage(this.menu.createTabChangeMessage(TraderStorageTab.TAB_TRADE_BASIC, null));
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
			{
				CompoundTag message = new CompoundTag();
				price.save(message, "NewPrice");
				this.menu.sendMessage(message);
			}
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
				CompoundTag message = new CompoundTag();
				message.putBoolean("NewTicket", true);
				if(ticket != null)
					message.put("Ticket", ticket.save(new CompoundTag()));
				this.menu.sendMessage(message);
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
			{
				CompoundTag message = new CompoundTag();
				message.putInt("NewDuration", duration);
				this.menu.sendMessage(message);
			}
		}
	}

	@Override
	public void receiveMessage(CompoundTag message) {
		if(message.contains("TradeIndex"))
		{
			this.tradeIndex = message.getInt("TradeIndex");
		}
		else if(message.contains("NewPrice"))
		{
			CoinValue price = new CoinValue();
			price.load(message, "NewPrice");
			this.setPrice(price);
		}
		else if(message.contains("NewTicket"))
		{
			ItemStack ticket = ItemStack.EMPTY;
			if(message.contains("Ticket"))
				ticket = ItemStack.of(message.getCompound("Ticket"));
			this.setTicket(ticket);
		}
		else if(message.contains("NewDuration"))
		{
			this.setDuration(message.getInt("NewDuration"));
		}
	}
	
}
