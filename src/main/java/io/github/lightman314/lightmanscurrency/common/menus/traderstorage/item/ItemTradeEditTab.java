package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.item;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.item.ItemTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class ItemTradeEditTab extends TraderStorageTab{

	public ItemTradeEditTab(@Nonnull ITraderStorageMenu menu) { super(menu); }

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(@Nonnull Object screen) { return new ItemTradeEditClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.EDIT_TRADES); }
	
	private int tradeIndex = -1;
	public int getTradeIndex() { return this.tradeIndex; }
	public ItemTradeData getTrade() {
		if(this.menu.getTrader() instanceof ItemTraderData trader)
		{
			if(this.tradeIndex >= trader.getTradeCount() || this.tradeIndex < 0)
			{
				this.menu.ChangeTab(TraderStorageTab.TAB_TRADE_BASIC);
				return null;
			}
			return trader.getTrade(this.tradeIndex);
		}
		return null;
	}
	public int selection = -1;
	
	public void setType(TradeDirection type) {
		ItemTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setTradeType(type);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setInt("NewType", type.index));
		}
	}
	
	public void setCustomName(int selectedSlot, String customName) {
		ItemTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setCustomName(selectedSlot, customName);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
			{
				this.menu.SendMessage(this.builder()
						.setInt("Slot", selectedSlot)
						.setString("CustomName", customName));
			}
		}
	}
	
	public void setPrice(MoneyValue price) {
		ItemTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setCost(price);
			this.menu.getTrader().markTradesDirty();
			LightmansCurrency.LogDebug("Setting price on the " + DebugUtil.getSideText(this.menu) + " as " + price.getText("Empty").getString());
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setMoneyValue("NewPrice", price));
		}
	}
	
	public void setSelectedItem(int selectedSlot, ItemStack stack) {
		ItemTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setItem(stack, selectedSlot);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
			{
				this.menu.SendMessage(this.builder()
						.setInt("Slot", selectedSlot)
						.setItem("NewItem", stack));
			}
		}	
	}

	public void setNBTEnforced(int selectedSlot, boolean newValue) {
		ItemTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setEnforceNBT(selectedSlot, newValue);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
			{
				this.menu.SendMessage(this.builder()
						.setInt("Slot", selectedSlot)
						.setBoolean("EnforceNBT", newValue));
			}
		}
	}
	
	public void defaultInteraction(int slotIndex, ItemStack heldStack, int mouseButton) {
		ItemTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.onSlotInteraction(slotIndex, heldStack, mouseButton);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
			{
				this.menu.SendMessage(this.builder()
						.setInt("Interaction", slotIndex)
						.setInt("Button", mouseButton)
						.setItem("Item", heldStack));
			}
		}
	}

	@Override
	public void OpenMessage(@Nonnull LazyPacketData message) {
		if(message.contains("TradeIndex"))
			this.tradeIndex = message.getInt("TradeIndex");
		if(message.contains("StartingSlot"))
			this.selection = message.getInt("StartingSlot");
	}

	@Override
	public void receiveMessage(LazyPacketData message) {
		if(message.contains("Slot"))
		{
			int slot = message.getInt("Slot");
			if(message.contains("CustomName"))
			{
				this.setCustomName(slot, message.getString("CustomName"));
			}
			else if(message.contains("NewItem"))
			{
				this.setSelectedItem(slot, message.getItem("NewItem"));
			}
			else if(message.contains("EnforceNBT"))
			{
				this.setNBTEnforced(slot, message.getBoolean("EnforceNBT"));
			}
		}
		else if(message.contains("NewPrice"))
		{
			this.setPrice(message.getMoneyValue("NewPrice"));
		}
		else if(message.contains("NewType"))
		{
			this.setType(TradeDirection.fromIndex(message.getInt("NewType")));
		}
		else if(message.contains("Interaction"))
		{
			int index = message.getInt("Interaction");
			int button = message.getInt("Button");
			this.defaultInteraction(index, message.getItem("Item"), button);
		}
	}

}
