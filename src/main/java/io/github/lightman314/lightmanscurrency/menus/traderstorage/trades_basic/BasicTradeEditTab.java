package io.github.lightman314.lightmanscurrency.menus.traderstorage.trades_basic;

import java.util.function.Function;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.BasicTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.ITradeData;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu.IClientMessage;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
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
	
	IClientMessage clientHandler = null;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderStorageClientTab<?> createClientTab(TraderStorageScreen screen) { return new BasicTradeEditClientTab<BasicTradeEditTab>(screen, this); }
	
	public void setClientHandler(IClientMessage clientHandler) { this.clientHandler = clientHandler; }
	
	@Override
	public boolean canOpen(Player player) { return true; }

	@Override
	public void onTabOpen() { }

	@Override
	public void onTabClose() { }

	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

	public void sendOpenTabMessage(int newTab, @Nullable CompoundTag additionalData) {
		CompoundTag message = this.menu.createTabChangeMessage(newTab, additionalData);
		if(this.clientHandler != null)
			this.clientHandler.selfMessage(message);
		this.menu.sendMessage(message);
	}
	
	public void sendInputInteractionMessage(int tradeIndex, int interactionIndex, int button, ItemStack heldItem) {
		CompoundTag message = new CompoundTag();
		message.putInt("TradeIndex", tradeIndex);
		message.putInt("InteractionType", INTERACTION_INPUT);
		message.putInt("InteractionIndex", interactionIndex);
		message.putInt("Button", button);
		CompoundTag itemTag = new CompoundTag();
		heldItem.save(itemTag);
		message.put("HeldItem", itemTag);
		//LightmansCurrency.LogInfo("Trade Input Interaction sent.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
		this.menu.sendMessage(message);
	}
	
	public void sendOutputInteractionMessage(int tradeIndex, int interactionIndex, int button, ItemStack heldItem) {
		CompoundTag message = new CompoundTag();
		message.putInt("TradeIndex", tradeIndex);
		message.putInt("InteractionType", INTERACTION_OUTPUT);
		message.putInt("InteractionIndex", interactionIndex);
		message.putInt("Button", button);
		CompoundTag itemTag = new CompoundTag();
		heldItem.save(itemTag);
		message.put("HeldItem", itemTag);
		//LightmansCurrency.LogInfo("Trade Output Interaction sent.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
		this.menu.sendMessage(message);
	}
	
	public void sendOtherInteractionMessage(int tradeIndex, int mouseX, int mouseY, int button, ItemStack heldItem) {
		CompoundTag message = new CompoundTag();
		message.putInt("TradeIndex", tradeIndex);
		message.putInt("InteractionType", INTERACTION_OTHER);
		message.putInt("Button", button);
		message.putInt("MouseX", mouseX);
		message.putInt("MouseY", mouseX);
		CompoundTag itemTag = new CompoundTag();
		heldItem.save(itemTag);
		message.put("HeldItem", itemTag);
		//LightmansCurrency.LogInfo("Trade Misc Interaction sent.\nIndex: " + tradeIndex + "\nMouse: " + mouseX + "," + mouseY + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
		this.menu.sendMessage(message);
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
	public void receiveMessage(CompoundTag message) {
		if(message.contains("TradeIndex",Tag.TAG_INT))
		{
			int tradeIndex = message.getInt("TradeIndex");
			int interaction = message.getInt("InteractionType");
			int interactionIndex = message.contains("InteractionIndex", Tag.TAG_INT) ? message.getInt("InteractionIndex") : 0;
			int button = message.getInt("Button");
			int mouseX = message.contains("MouseX", Tag.TAG_INT) ? message.getInt("MouseX") : 0;
			int mouseY = message.contains("MouseY", Tag.TAG_INT) ? message.getInt("MouseY") : 0;
			ItemStack heldItem = ItemStack.of(message.getCompound("HeldItem"));
			ITradeData trade = this.menu.getTrader().getTradeInfo().get(tradeIndex);
			switch(interaction) {
			case INTERACTION_INPUT:
				trade.onInputDisplayInteraction(this, this.clientHandler, interactionIndex, button, heldItem);
				//LightmansCurrency.LogInfo("Trade Input Interaction received.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
				break;
			case INTERACTION_OUTPUT:
				trade.onOutputDisplayInteraction(this, this.clientHandler, interactionIndex, button, heldItem);
				//LightmansCurrency.LogInfo("Trade Output Interaction received.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
				break;
			case INTERACTION_OTHER:
				trade.onInteraction(this, this.clientHandler, mouseX, mouseY, button, heldItem);
				//LightmansCurrency.LogInfo("Trade Misc Interaction received.\nIndex: " + tradeIndex + "\nMouse: " + mouseX + "," + mouseY + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
				break;
				default:
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
