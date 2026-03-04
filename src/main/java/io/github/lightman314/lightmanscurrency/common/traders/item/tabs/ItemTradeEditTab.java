package io.github.lightman314.lightmanscurrency.common.traders.item.tabs;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageNodeTab;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.traders.item.client.tabs.ItemTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemTradeNode;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemTradeEditTab extends TraderStorageNodeTab<ItemTradeNode> {

    public static final ResourceLocation KEY = LightmansCurrency.id("item_trade");

	public ItemTradeEditTab(ITraderStorageMenu menu) { super(ItemTradeNode.TYPE,menu); }

    @Override
    public ResourceLocation tabKey() { return KEY; }

	@Override
	public Object createClientTab(Object screen) { return new ItemTradeEditClientTab(screen, this); }

	@Override
	public boolean canOpenTab(Player player) { return this.menu.hasPermission(Permissions.EDIT_TRADES); }
	
	private int tradeIndex = -1;
	public int getTradeIndex() { return this.tradeIndex; }
	public ItemTradeData getTrade() {
        ItemTradeNode node = this.getNode();
		if(node != null)
		{
			if(this.tradeIndex >= node.getTradeCount() || this.tradeIndex < 0)
			{
				this.menu.ChangeTab(0);
				return null;
			}
			return node.getTrade(this.tradeIndex);
		}
		return null;
	}
	public int selection = -1;
	
	public void setType(TradeDirection type) {
		ItemTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setTradeType(type);
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setInt("NewType", type.index));
		}
	}
	
	public void setCustomName(int selectedSlot, String customName) {
		ItemTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setCustomName(selectedSlot, customName);
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
	public void OpenMessage(LazyPacketData message) {
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
