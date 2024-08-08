package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.BasicTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class BasicTradeEditTab extends TraderStorageTab implements IClientTracker {

	public BasicTradeEditTab(TraderStorageMenu menu) { super(menu); }

	public static final int INTERACTION_INPUT = 0;
	public static final int INTERACTION_OUTPUT = 1;
	public static final int INTERACTION_OTHER = 2;

	Consumer<LazyPacketData.Builder> clientHandler = null;

	@Override
	public boolean isClient() { return this.menu.isClient(); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(Object screen) { return new BasicTradeEditClientTab<>(screen, this); }

	public void setClient(Consumer<LazyPacketData.Builder> clientHandler) { this.clientHandler = clientHandler; }
	
	@Override
	public boolean canOpen(Player player) { return true; }

	@Override
	public void onTabOpen() { }

	@Override
	public void onTabClose() { }

	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

	public void sendOpenTabMessage(int newTab, @Nullable LazyPacketData.Builder additionalData) {
		LazyPacketData.Builder message = this.menu.createTabChangeMessage(newTab, additionalData);
		if(this.clientHandler != null)
			this.clientHandler.accept(message);
		this.menu.SendMessage(message);
	}

	@Deprecated(since = "2.2.3.0")
	public void sendInputInteractionMessage(int tradeIndex, int interactionIndex, int button, @Nonnull ItemStack heldItem) { this.SendInputInteractionMessage(tradeIndex, interactionIndex, new TradeInteractionData(0,0,button,false,false,false), heldItem); }
	public void SendInputInteractionMessage(int tradeIndex, int interactionIndex, @Nonnull TradeInteractionData data, ItemStack heldItem) {
		//LightmansCurrency.LogDebug("Trade Input Interaction sent.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + BuiltInRegistries.ITEM.getKey(heldItem.getItem()));
		this.menu.SendMessage(data.encode(this.builder()
				.setInt("TradeIndex",tradeIndex)
				.setInt("InteractionType", INTERACTION_INPUT)
				.setInt("InteractionIndex",interactionIndex)
				.setItem("HeldItem", heldItem)
		));
	}

	@Deprecated(since = "2.2.3.0")
	public void sendOutputInteractionMessage(int tradeIndex, int interactionIndex, int button, @Nonnull ItemStack heldItem) { this.SendOutputInteractionMessage(tradeIndex, interactionIndex, new TradeInteractionData(0,0,button,false,false,false), heldItem); }
	public void SendOutputInteractionMessage(int tradeIndex, int interactionIndex, @Nonnull TradeInteractionData data, ItemStack heldItem) {
		//LightmansCurrency.LogDebug("Trade Output Interaction sent.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + BuiltInRegistries.ITEM.getKey(heldItem.getItem()));
		this.menu.SendMessage(data.encode(this.builder()
				.setInt("TradeIndex",tradeIndex)
				.setInt("InteractionType", INTERACTION_OUTPUT)
				.setInt("InteractionIndex",interactionIndex)
				.setItem("HeldItem", heldItem)
		));
	}
	
	public void sendOtherInteractionMessage(int tradeIndex, int mouseX, int mouseY, int button, @Nonnull ItemStack heldItem) { this.SendOtherInteractionMessage(tradeIndex, new TradeInteractionData(mouseX,mouseY,button,false,false,false), heldItem); }
	public void SendOtherInteractionMessage(int tradeIndex, @Nonnull TradeInteractionData data, @Nonnull ItemStack heldItem) {
		//LightmansCurrency.LogDebug("Trade Misc Interaction sent.\nIndex: " + tradeIndex + "\nMouse: " + mouseX + "," + mouseY + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + BuiltInRegistries.ITEM.getKey(heldItem.getItem()));
		this.menu.SendMessage(data.encode(this.builder()
				.setInt("TradeIndex",tradeIndex)
				.setInt("InteractionType", INTERACTION_OTHER)
				.setItem("HeldItem", heldItem)
		));
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

	@SuppressWarnings("deprecation")
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
				case INTERACTION_INPUT -> {
					try {
						trade.OnInputDisplayInteraction(this, this.clientHandler, interactionIndex, data, heldItem);
					} catch (Throwable t) {
						trade.OnInputDisplayInteraction(this, this.clientHandler, interactionIndex, data.mouseButton(), heldItem);
					}
				}
				//LightmansCurrency.LogInfo("Trade Input Interaction received.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
				case INTERACTION_OUTPUT -> {
					try {
						trade.OnOutputDisplayInteraction(this, this.clientHandler, interactionIndex, data, heldItem);
					}catch (Throwable t) {
						trade.OnOutputDisplayInteraction(this, this.clientHandler, interactionIndex, data.mouseButton(), heldItem);
					}
				}
				//LightmansCurrency.LogInfo("Trade Output Interaction received.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
				case INTERACTION_OTHER -> {
					try {
						trade.OnInteraction(this, this.clientHandler, data, heldItem);
					} catch (Throwable t) {
						trade.OnInteraction(this, this.clientHandler, data.localMouseX(), data.localMouseY(), data.mouseButton(), heldItem);
					}
				}
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
	}
	
}
