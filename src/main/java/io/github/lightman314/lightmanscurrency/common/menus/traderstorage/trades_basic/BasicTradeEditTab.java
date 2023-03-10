package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic;

import java.util.function.Function;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.BasicTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu.IClientMessage;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public class BasicTradeEditTab extends TraderStorageTab {

	public BasicTradeEditTab(TraderStorageMenu menu) { super(menu); }

	public static final int INTERACTION_INPUT = 0;
	public static final int INTERACTION_OUTPUT = 1;
	public static final int INTERACTION_OTHER = 2;
	
	IClientMessage clientHandler = null;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderStorageClientTab<?> createClientTab(TraderStorageScreen screen) { return new BasicTradeEditClientTab<>(screen, this); }
	
	public void setClientHandler(IClientMessage clientHandler) { this.clientHandler = clientHandler; }
	
	@Override
	public boolean canOpen(PlayerEntity player) { return true; }

	@Override
	public void onTabOpen() { }

	@Override
	public void onTabClose() { }

	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

	public void sendOpenTabMessage(int newTab, @Nullable CompoundNBT additionalData) {
		CompoundNBT message = this.menu.createTabChangeMessage(newTab, additionalData);
		if(this.clientHandler != null)
			this.clientHandler.selfMessage(message);
		this.menu.sendMessage(message);
	}
	
	public void sendInputInteractionMessage(int tradeIndex, int interactionIndex, int button, ItemStack heldItem) {
		CompoundNBT message = new CompoundNBT();
		message.putInt("TradeIndex", tradeIndex);
		message.putInt("InteractionType", INTERACTION_INPUT);
		message.putInt("InteractionIndex", interactionIndex);
		message.putInt("Button", button);
		CompoundNBT itemTag = new CompoundNBT();
		heldItem.save(itemTag);
		message.put("HeldItem", itemTag);
		//LightmansCurrency.LogInfo("Trade Input Interaction sent.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
		this.menu.sendMessage(message);
	}
	
	public void sendOutputInteractionMessage(int tradeIndex, int interactionIndex, int button, ItemStack heldItem) {
		CompoundNBT message = new CompoundNBT();
		message.putInt("TradeIndex", tradeIndex);
		message.putInt("InteractionType", INTERACTION_OUTPUT);
		message.putInt("InteractionIndex", interactionIndex);
		message.putInt("Button", button);
		CompoundNBT itemTag = new CompoundNBT();
		heldItem.save(itemTag);
		message.put("HeldItem", itemTag);
		//LightmansCurrency.LogInfo("Trade Output Interaction sent.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
		this.menu.sendMessage(message);
	}
	
	public void sendOtherInteractionMessage(int tradeIndex, int mouseX, int mouseY, int button, ItemStack heldItem) {
		CompoundNBT message = new CompoundNBT();
		message.putInt("TradeIndex", tradeIndex);
		message.putInt("InteractionType", INTERACTION_OTHER);
		message.putInt("Button", button);
		message.putInt("MouseX", mouseX);
		message.putInt("MouseY", mouseY);
		CompoundNBT itemTag = new CompoundNBT();
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
				CompoundNBT message = new CompoundNBT();
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
				CompoundNBT message = new CompoundNBT();
				message.putBoolean("RemoveTrade", true);
				this.menu.sendMessage(message);
			}
		}
		
	}
	
	@Override
	public void receiveMessage(CompoundNBT message) {
		if(message.contains("TradeIndex", Constants.NBT.TAG_INT))
		{
			if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
				return;
			int tradeIndex = message.getInt("TradeIndex");
			int interaction = message.getInt("InteractionType");
			int interactionIndex = message.contains("InteractionIndex", Constants.NBT.TAG_INT) ? message.getInt("InteractionIndex") : 0;
			int button = message.getInt("Button");
			int mouseX = message.contains("MouseX", Constants.NBT.TAG_INT) ? message.getInt("MouseX") : 0;
			int mouseY = message.contains("MouseY", Constants.NBT.TAG_INT) ? message.getInt("MouseY") : 0;
			ItemStack heldItem = ItemStack.of(message.getCompound("HeldItem"));
			TradeData trade = this.menu.getTrader().getTradeData().get(tradeIndex);
			switch (interaction) {
				case INTERACTION_INPUT:
						trade.onInputDisplayInteraction(this, this.clientHandler, interactionIndex, button, heldItem);
						break;

				//LightmansCurrency.LogInfo("Trade Input Interaction received.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
				case INTERACTION_OUTPUT:
						trade.onOutputDisplayInteraction(this, this.clientHandler, interactionIndex, button, heldItem);
						break;

				//LightmansCurrency.LogInfo("Trade Output Interaction received.\nIndex: " + tradeIndex + "\nInteractionIndex: " + interactionIndex + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
				case INTERACTION_OTHER:
						trade.onInteraction(this, this.clientHandler, mouseX, mouseY, button, heldItem);
						break;

				//LightmansCurrency.LogInfo("Trade Misc Interaction received.\nIndex: " + tradeIndex + "\nMouse: " + mouseX + "," + mouseY + "\nButton: " + button + "\nHeld Item: " + heldItem.getCount() + "x " + heldItem.getItem().getRegistryName().toString());
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