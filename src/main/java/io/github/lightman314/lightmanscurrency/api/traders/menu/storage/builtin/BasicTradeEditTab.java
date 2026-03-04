package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.TradeOfferSourceNode;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.BasicTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BasicTradeEditTab extends TraderStorageTab implements IClientTracker {

    public static final ResourceLocation KEY = LightmansCurrency.id("trades_basic");

	public BasicTradeEditTab(ITraderStorageMenu menu) { super(menu); }

    @Override
    public ResourceLocation tabKey() { return KEY; }
    @Override
    protected boolean isDefaultTab() { return true; }

    public static final int INTERACTION_INPUT = 0;
	public static final int INTERACTION_OUTPUT = 1;
	public static final int INTERACTION_OTHER = 2;

	private final List<Integer> selectedTrades = new ArrayList<>();
	public int selectedCount() { return this.selectedTrades.size(); }
	public boolean isSelected(TraderData trader, TradeData trade) { return this.menu.hasPermission(Permissions.EDIT_TRADES) && this.selectedTrades.contains(trader.indexOfTrade(trade)); }
	public boolean allTradesSelected()
	{
        TradeOfferSourceNode<?> node = this.menu.getTradeOfferNode();
		return node != null && node.getTradeCount() == this.selectedTrades.size();
	}

	public boolean allowTradeSelection()
	{
        TradeOfferSourceNode<?> node = this.menu.getTradeOfferNode();
		if(node == null)
			return false;
		return node.getTradeCount() > 1 && this.menu.hasPermission(Permissions.EDIT_TRADES) && node.supportsMultiPriceEditing();
	}

	@Override
	public Object createClientTab(Object screen) { return new BasicTradeEditClientTab<>(screen, this); }

	@Override
	public void onTabClose() { this.selectedTrades.clear(); }

	@Override
	public boolean canOpen(Player player) { return true; }

	public void sendOpenTabMessage(ResourceLocation newTab, @Nullable LazyPacketData.Builder additionalData) {
		this.menu.ChangeTab(newTab,additionalData);
	}

	public void SendInputInteractionMessage(int tradeIndex, int interactionIndex, TradeInteractionData data, ItemStack heldItem) {
		//LightmansCurrency.LogDebug("Trade Input Interaction sent.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + BuiltInRegistries.ITEM.getKey(heldItem.getItem()));
		this.menu.SendMessage(data.encode(this.builder()
				.setInt("TradeIndex",tradeIndex)
				.setInt("InteractionType", INTERACTION_INPUT)
				.setInt("InteractionIndex",interactionIndex)
				.setItem("HeldItem", heldItem)
		));
	}

	public void SendOutputInteractionMessage(int tradeIndex, int interactionIndex, TradeInteractionData data, ItemStack heldItem) {
		//LightmansCurrency.LogDebug("Trade Output Interaction sent.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + BuiltInRegistries.ITEM.getKey(heldItem.getItem()));
		this.menu.SendMessage(data.encode(this.builder()
				.setInt("TradeIndex",tradeIndex)
				.setInt("InteractionType", INTERACTION_OUTPUT)
				.setInt("InteractionIndex",interactionIndex)
				.setItem("HeldItem", heldItem)
		));
	}

	public void SendOtherInteractionMessage(int tradeIndex, TradeInteractionData data, ItemStack heldItem) {
		//LightmansCurrency.LogDebug("Trade Misc Interaction sent.\nIndex: " + tradeIndex + "\nMouse: " + mouseX + "," + mouseY + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + BuiltInRegistries.ITEM.getKey(heldItem.getItem()));
		this.menu.SendMessage(data.encode(this.builder()
				.setInt("TradeIndex",tradeIndex)
				.setInt("InteractionType", INTERACTION_OTHER)
				.setItem("HeldItem", heldItem)
		));
	}

	public void SelectAllTrades()
	{
        TradeOfferSourceNode<?> node = this.menu.getTradeOfferNode();
		if(node == null)
			return;
		if(!this.allowTradeSelection())
			return;
		int tradeCount = node.getTradeCount();
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
		if(!this.allowTradeSelection())
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
			this.menu.ChangeTab(MultiPriceTab.KEY, this.builder().setList("Selected",this.selectedTrades,LazyPacketData.INT_FACTORY));
	}
	
	public void addTrade() {

        TradeOfferSourceNode<?> node = this.menu.getTradeOfferNode();
		if(node != null)
		{
            node.addTrade(this.menu.getPlayer());
			if(this.isClient())
				this.menu.SendMessage(this.builder().setFlag("AddTrade"));
		}
		
	}
	
	public void removeTrade() {
        TradeOfferSourceNode<?> node = this.menu.getTradeOfferNode();
		if(node != null)
		{
            node.removeTrade(this.menu.getPlayer());
			if(this.isClient())
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

            TradeOfferSourceNode<?> node = this.menu.getTradeOfferNode();
			TradeData trade = node.getTrade(tradeIndex);
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
