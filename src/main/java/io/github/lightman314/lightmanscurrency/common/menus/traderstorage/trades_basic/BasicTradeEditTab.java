package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.BasicTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class BasicTradeEditTab extends TraderStorageTab implements IClientTracker {

	public BasicTradeEditTab(TraderStorageMenu menu) { super(menu); }

	public static final int INTERACTION_INPUT = 0;
	public static final int INTERACTION_OUTPUT = 1;
	public static final int INTERACTION_OTHER = 2;

	private final List<Integer> selectedTrades = new ArrayList<>();
	public int selectedCount() { return this.selectedTrades.size(); }
	public boolean isSelected(@Nonnull TraderData trader, @Nonnull TradeData trade) { return this.menu.hasPermission(Permissions.EDIT_TRADES) && this.selectedTrades.contains(trader.indexOfTrade(trade)); }
	public boolean allTradesSelected()
	{
		TraderData trader = this.menu.getTrader();
		return trader != null && trader.getTradeCount() == this.selectedTrades.size();
	}

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(@Nonnull Object screen) { return new BasicTradeEditClientTab<>(screen, this); }

	@Override
	public void onTabClose() { this.selectedTrades.clear(); }

	@Override
	public boolean canOpen(Player player) { return true; }

	public void sendOpenTabMessage(int newTab, @Nullable LazyPacketData.Builder additionalData) {
		this.menu.ChangeTab(newTab,additionalData);
	}

	public void SendInputInteractionMessage(int tradeIndex, int interactionIndex, @Nonnull TradeInteractionData data, ItemStack heldItem) {
		//LightmansCurrency.LogDebug("Trade Input Interaction sent.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + BuiltInRegistries.ITEM.getKey(heldItem.getItem()));
		this.menu.SendMessage(data.encode(this.builder()
				.setInt("TradeIndex",tradeIndex)
				.setInt("InteractionType", INTERACTION_INPUT)
				.setInt("InteractionIndex",interactionIndex)
				.setItem("HeldItem", heldItem)
		));
	}

	public void SendOutputInteractionMessage(int tradeIndex, int interactionIndex, @Nonnull TradeInteractionData data, ItemStack heldItem) {
		//LightmansCurrency.LogDebug("Trade Output Interaction sent.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + BuiltInRegistries.ITEM.getKey(heldItem.getItem()));
		this.menu.SendMessage(data.encode(this.builder()
				.setInt("TradeIndex",tradeIndex)
				.setInt("InteractionType", INTERACTION_OUTPUT)
				.setInt("InteractionIndex",interactionIndex)
				.setItem("HeldItem", heldItem)
		));
	}

	public void SendOtherInteractionMessage(int tradeIndex, @Nonnull TradeInteractionData data, @Nonnull ItemStack heldItem) {
		//LightmansCurrency.LogDebug("Trade Misc Interaction sent.\nIndex: " + tradeIndex + "\nMouse: " + mouseX + "," + mouseY + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + BuiltInRegistries.ITEM.getKey(heldItem.getItem()));
		this.menu.SendMessage(data.encode(this.builder()
				.setInt("TradeIndex",tradeIndex)
				.setInt("InteractionType", INTERACTION_OTHER)
				.setItem("HeldItem", heldItem)
		));
	}

	public void SelectAllTrades()
	{
		TraderData trader = this.menu.getTrader();
		if(trader == null)
			return;
		int tradeCount = trader.getTradeCount();
		//If all trades are already selected, deselect all trades
		if(this.selectedTrades.size() == tradeCount)
			this.selectedTrades.clear();
		else
		{
			this.selectedTrades.clear();
			for(int i = 0; i < tradeCount; ++i)
				this.selectedTrades.add(i);
		}
		if(this.isClient())
			this.menu.SendMessage(this.builder().setFlag("SelectAllTrades"));
	}

	public void ToggleTradeSelection(int tradeIndex)
	{
		//Cannot toggle an invalid index
		if(tradeIndex < 0)
			return;
		if(this.selectedTrades.contains(tradeIndex))
			this.selectedTrades.remove((Object)tradeIndex);
		else
			this.selectedTrades.add(tradeIndex);
		if(this.isClient())
			this.menu.SendMessage(this.builder().setInt("SelectTradeForEdit",tradeIndex));
	}

	public boolean canOpenMultiEdit() { return !this.selectedTrades.isEmpty(); }

	public void OpenMultiEditTab()
	{
		if(!this.canOpenMultiEdit())
			return;
		if(this.isClient())
			this.menu.SendMessage(this.builder().setFlag("OpenMultiEdit"));
		else
			this.menu.ChangeTab(TraderStorageTab.TAB_TRADE_MULTI_PRICE, this.builder().setList("Selected",this.selectedTrades,LazyPacketData.Builder::setInt));
	}
	
	public void addTrade() {
		
		if(this.menu.getTrader() != null)
		{
			this.menu.getTrader().addTrade(this.menu.getPlayer());
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setFlag("AddTrade"));
		}
		
	}
	
	public void removeTrade() {
		
		if(this.menu.getTrader() != null)
		{
			this.menu.getTrader().removeTrade(this.menu.getPlayer());
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setFlag("RemoveTrade"));
		}
		
	}

	@Override
	public void receiveMessage(LazyPacketData message) {
		if(message.contains("TradeIndex", LazyPacketData.TYPE_INT))
		{
			if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
				return;
			int tradeIndex = message.getInt("TradeIndex");
			int interaction = message.getInt("InteractionType");
			int interactionIndex = message.getInt("InteractionIndex");
			ItemStack heldItem = message.getItem("HeldItem");

			TradeData trade = this.menu.getTrader().getTrade(tradeIndex);
			TradeInteractionData data = TradeInteractionData.decode(message);

			switch (interaction) {
				case INTERACTION_INPUT ->
						trade.OnInputDisplayInteraction(this, interactionIndex, data, heldItem);
				//LightmansCurrency.LogInfo("Trade Input Interaction received.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
				case INTERACTION_OUTPUT ->
					trade.OnOutputDisplayInteraction(this, interactionIndex, data, heldItem);
				//LightmansCurrency.LogInfo("Trade Output Interaction received.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
				case INTERACTION_OTHER ->
						trade.OnInteraction(this, data, heldItem);
				//LightmansCurrency.LogInfo("Trade Misc Interaction received.\nIndex: " + tradeIndex + "\nMouse: " + mouseX + "," + mouseY + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
				default ->
						LightmansCurrency.LogWarning("Interaction Type " + interaction + " is not a valid interaction.");
			}
			this.menu.getTrader().markTradesDirty();
		}
		if(message.contains("AddTrade"))
			this.addTrade();
		if(message.contains("RemoveTrade"))
			this.removeTrade();
		if(message.contains("SelectTradeForEdit"))
			this.ToggleTradeSelection(message.getInt("SelectTradeForEdit"));
		if(message.contains("SelectAllTrades"))
			this.SelectAllTrades();
		if(message.contains("OpenMultiEdit"))
			this.OpenMultiEditTab();
	}
	
}
