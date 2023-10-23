package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.BasicTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BasicTradeEditTab extends TraderStorageTab {

	public BasicTradeEditTab(TraderStorageMenu menu) { super(menu); }

	public static final int INTERACTION_INPUT = 0;
	public static final int INTERACTION_OUTPUT = 1;
	public static final int INTERACTION_OTHER = 2;

	Consumer<CompoundTag> oldClientHandler = null;
	Consumer<LazyPacketData.Builder> clientHandler = null;

	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(Object screen) { return new BasicTradeEditClientTab<>(screen, this); }

	@Deprecated(since = "2.1.2.4")
	public void setClientHandler(Consumer<CompoundTag> oldClientHandler) { this.oldClientHandler = oldClientHandler; }
	public void setClient(Consumer<LazyPacketData.Builder> clientHandler)
	{
		this.clientHandler = clientHandler;
		if(this.oldClientHandler == null)
			this.oldClientHandler = c -> this.clientHandler.accept(LazyPacketData.simpleTag("OldMessage",c));
	}

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
	@Deprecated(since = "2.1.2.4")
	public void sendOpenTabMessage(int newTab, @Nullable CompoundTag additionalData) {
		CompoundTag message = this.menu.createTabChangeMessage(newTab, additionalData);
		if(this.oldClientHandler != null)
			this.oldClientHandler.accept(message);
		this.menu.sendMessage(message);
	}

	public void sendInputInteractionMessage(int tradeIndex, int interactionIndex, int button, ItemStack heldItem) {
		//LightmansCurrency.LogInfo("Trade Input Interaction sent.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
		this.menu.SendMessage(LazyPacketData.builder()
				.setInt("TradeIndex", tradeIndex)
				.setInt("InteractionType", INTERACTION_INPUT)
				.setInt("InteractionIndex", interactionIndex)
				.setInt("Button", button)
				.setCompound("HeldItem", heldItem.save(new CompoundTag())));
	}

	public void sendOutputInteractionMessage(int tradeIndex, int interactionIndex, int button, ItemStack heldItem) {
		//LightmansCurrency.LogInfo("Trade Output Interaction sent.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
		this.menu.SendMessage(LazyPacketData.builder()
				.setInt("TradeIndex", tradeIndex)
				.setInt("InteractionType", INTERACTION_OUTPUT)
				.setInt("InteractionIndex", interactionIndex)
				.setInt("Button", button)
				.setCompound("HeldItem", heldItem.save(new CompoundTag())));
	}

	public void sendOtherInteractionMessage(int tradeIndex, int mouseX, int mouseY, int button, ItemStack heldItem) {
		//LightmansCurrency.LogInfo("Trade Misc Interaction sent.\nIndex: " + tradeIndex + "\nMouse: " + mouseX + "," + mouseY + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
		this.menu.SendMessage(LazyPacketData.builder()
				.setInt("TradeIndex", tradeIndex)
				.setInt("InteractionType", INTERACTION_OTHER)
				.setInt("Button", button)
				.setInt("MouseX", mouseX)
				.setInt("MouseY", mouseY)
				.setCompound("HeldItem", heldItem.save(new CompoundTag())));
	}

	public void addTrade() {

		if(this.menu.getTrader() != null)
		{
			this.menu.getTrader().addTrade(this.menu.player);
			if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.putBoolean("AddTrade", true);
				this.menu.sendMessage(message);
			}
		}

	}

	public void removeTrade() {

		if(this.menu.getTrader() != null)
		{
			this.menu.getTrader().removeTrade(this.menu.player);
			if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.putBoolean("RemoveTrade", true);
				this.menu.sendMessage(message);
			}
		}

	}

	@Override
	@SuppressWarnings("deprecation")
	public void receiveMessage(LazyPacketData message) {
		if(message.contains("TradeIndex", LazyPacketData.TYPE_INT))
		{
			if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
				return;
			int tradeIndex = message.getInt("TradeIndex");
			int interaction = message.getInt("InteractionType");
			int interactionIndex = message.contains("InteractionIndex", LazyPacketData.TYPE_INT) ? message.getInt("InteractionIndex") : 0;
			int button = message.getInt("Button");
			int mouseX = message.contains("MouseX", LazyPacketData.TYPE_INT) ? message.getInt("MouseX") : 0;
			int mouseY = message.contains("MouseY", LazyPacketData.TYPE_INT) ? message.getInt("MouseY") : 0;
			ItemStack heldItem = ItemStack.of(message.getNBT("HeldItem"));
			TradeData trade = this.menu.getTrader().getTradeData().get(tradeIndex);
			switch (interaction) {
				case INTERACTION_INPUT ->
				{
					trade.OnInputDisplayInteraction(this, this.clientHandler, interactionIndex, button, heldItem);
					trade.onInputDisplayInteraction(this, this.oldClientHandler, interactionIndex, button, heldItem);
				}
				//LightmansCurrency.LogInfo("Trade Input Interaction received.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
				case INTERACTION_OUTPUT ->
				{
					trade.OnOutputDisplayInteraction(this, this.clientHandler, interactionIndex, button, heldItem);
					trade.onOutputDisplayInteraction(this, this.oldClientHandler, interactionIndex, button, heldItem);
				}
				//LightmansCurrency.LogInfo("Trade Output Interaction received.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
				case INTERACTION_OTHER ->
				{
					trade.OnInteraction(this, this.clientHandler, mouseX, mouseY, button, heldItem);
					trade.onInteraction(this, this.oldClientHandler, mouseX, mouseY, button, heldItem);
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

	@Override
	public void receiveMessage(CompoundTag message) {
		if(message.contains("TradeIndex",Tag.TAG_INT))
		{
			if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
				return;
			int tradeIndex = message.getInt("TradeIndex");
			int interaction = message.getInt("InteractionType");
			int interactionIndex = message.contains("InteractionIndex", Tag.TAG_INT) ? message.getInt("InteractionIndex") : 0;
			int button = message.getInt("Button");
			int mouseX = message.contains("MouseX", Tag.TAG_INT) ? message.getInt("MouseX") : 0;
			int mouseY = message.contains("MouseY", Tag.TAG_INT) ? message.getInt("MouseY") : 0;
			ItemStack heldItem = ItemStack.of(message.getCompound("HeldItem"));
			TradeData trade = this.menu.getTrader().getTradeData().get(tradeIndex);
			switch (interaction) {
				case INTERACTION_INPUT ->
						trade.onInputDisplayInteraction(this, this.oldClientHandler, interactionIndex, button, heldItem);

				//LightmansCurrency.LogInfo("Trade Input Interaction received.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
				case INTERACTION_OUTPUT ->
						trade.onOutputDisplayInteraction(this, this.oldClientHandler, interactionIndex, button, heldItem);

				//LightmansCurrency.LogInfo("Trade Output Interaction received.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
				case INTERACTION_OTHER ->
						trade.onInteraction(this, this.oldClientHandler, mouseX, mouseY, button, heldItem);

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