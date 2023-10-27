package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.item;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.item.ItemTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData.ItemTradeType;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemTradeEditTab extends TraderStorageTab{

	public ItemTradeEditTab(TraderStorageMenu menu) { super(menu); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(Object screen) { return new ItemTradeEditClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.EDIT_TRADES); }
	
	private int tradeIndex = -1;
	public int getTradeIndex() { return this.tradeIndex; }
	public ItemTradeData getTrade() { 
		if(this.menu.getTrader() instanceof ItemTraderData trader)
		{
			if(this.tradeIndex >= trader.getTradeCount() || this.tradeIndex < 0)
			{
				this.menu.changeTab(TraderStorageTab.TAB_TRADE_BASIC);
				this.menu.SendMessage(this.menu.createTabChangeMessage(TraderStorageTab.TAB_TRADE_BASIC));
				return null;
			}
			return ((ItemTraderData)this.menu.getTrader()).getTrade(this.tradeIndex);
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
	
	public void setType(ItemTradeData.ItemTradeType type) {
		ItemTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setTradeType(type);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
				this.menu.SendMessage(LazyPacketData.simpleInt("NewType", type.index));
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
				this.menu.SendMessage(LazyPacketData.builder()
						.setInt("Slot", selectedSlot)
						.setString("CustomName", customName));
			}
		}
	}
	
	public void setPrice(CoinValue price) {
		ItemTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setCost(price);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
				this.menu.SendMessage(LazyPacketData.simpleCoinValue("NewPrice", price));
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
				this.menu.SendMessage(LazyPacketData.builder()
						.setInt("Slot", selectedSlot)
						.setCompound("NewItem", stack.save(new CompoundTag())));
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
				this.menu.SendMessage(LazyPacketData.builder()
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
			if(this.menu.isClient())
			{
				this.menu.SendMessage(LazyPacketData.builder()
						.setInt("Interaction", slotIndex)
						.setInt("Button", mouseButton)
						.setCompound("Item", heldStack.save(new CompoundTag())));
			}
		}
	}

	@Override
	public void receiveMessage(LazyPacketData message) {
		if(message.contains("TradeIndex"))
		{
			this.tradeIndex = message.getInt("TradeIndex");
		}
		else if(message.contains("Slot"))
		{
			int slot = message.getInt("Slot");
			if(message.contains("CustomName"))
			{
				this.setCustomName(slot, message.getString("CustomName"));
			}
			else if(message.contains("NewItem"))
			{
				this.setSelectedItem(slot, ItemStack.of(message.getNBT("NewItem")));
			}
			else if(message.contains("EnforceNBT"))
			{
				this.setNBTEnforced(slot, message.getBoolean("EnforceNBT"));
			}
		}
		else if(message.contains("NewPrice"))
		{
			this.setPrice(message.getCoinValue("NewPrice"));
		}
		else if(message.contains("NewType"))
		{
			this.setType(ItemTradeType.fromIndex(message.getInt("NewType")));
		}
		else if(message.contains("Interaction"))
		{
			int index = message.getInt("Interaction");
			int button = message.getInt("Button");
			this.defaultInteraction(index, ItemStack.of(message.getNBT("Item")), button);
		}
	}
	
}
