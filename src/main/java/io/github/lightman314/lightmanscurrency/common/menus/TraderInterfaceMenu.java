package io.github.lightman314.lightmanscurrency.common.menus;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity.ActiveMode;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base.*;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.BlockEntityValidator;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TraderInterfaceMenu extends LazyMessageMenu {

	private final TraderInterfaceBlockEntity blockEntity;
	public final TraderInterfaceBlockEntity getBE() { return this.blockEntity; }
	
	public static final int SLOT_OFFSET = 15;
	
	private boolean canEditTabs;
	Map<Integer,TraderInterfaceTab> availableTabs = new HashMap<>();
	public Map<Integer,TraderInterfaceTab> getAllTabs() { return this.availableTabs; }
	public void setTab(int key, TraderInterfaceTab tab) { if(canEditTabs && tab != null) this.availableTabs.put(key, tab); else if(tab == null) LightmansCurrency.LogError("Attempted to set a null storage tab in slot " + key); else LightmansCurrency.LogError("Attempted to define the tab in " + key + " but the tabs have been locked."); }
	int currentTab = TraderInterfaceTab.TAB_INFO;
	public int getCurrentTabIndex() { return this.currentTab; }
	public TraderInterfaceTab getCurrentTab() { return this.availableTabs.get(this.currentTab); }

    public TraderInterfaceMenu(int windowID, Inventory inventory, TraderInterfaceBlockEntity blockEntity) {
		super(ModMenus.TRADER_INTERFACE.get(), windowID, inventory);
		this.blockEntity = blockEntity;
		this.canEditTabs = true;

		this.addValidator(BlockEntityValidator.of(this.blockEntity));
		this.addValidator(this.blockEntity::canAccess);

		this.setTab(TraderInterfaceTab.TAB_INFO, new InfoTab(this));
		this.setTab(TraderInterfaceTab.TAB_TRADER_SELECT, new TraderSelectTab(this));
		this.setTab(TraderInterfaceTab.TAB_TRADE_SELECT, new TradeSelectTab(this));
		this.setTab(TraderInterfaceTab.TAB_STATS, new InterfaceStatsTab(this));
		this.setTab(TraderInterfaceTab.TAB_OWNERSHIP, new OwnershipTab(this));
		if(this.blockEntity != null)
			this.blockEntity.initMenuTabs(this);
		this.canEditTabs = false;
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, SLOT_OFFSET + 8 + x * 18, 154 + y * 18));
			}
		}
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, SLOT_OFFSET + 8 + x * 18, 212));
		}
		
		this.availableTabs.forEach((key, tab) -> tab.addStorageMenuSlots(this::addSlot));
		
		//Run the tab open code for the current tab
		try {
			this.getCurrentTab().onTabOpen();
		} catch(Throwable t) { LightmansCurrency.LogError("Error opening tab!",t); }
		
	}
	
	@Override
	public void removed(@Nonnull Player player) {
		super.removed(player);
		this.availableTabs.forEach((key, tab) -> tab.onMenuClose());
	}
	

	public TradeContext getTradeContext() {
		return this.blockEntity.getTradeContext();
	}
	
	@Nonnull
	@Override
	public ItemStack quickMoveStack(@Nonnull Player playerEntity, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.slots.get(index);
		
		if(slot != null && slot.hasItem())
		{
			ItemStack slotStack = slot.getItem();
			clickedStack = slotStack.copy();
			if(index < 36)
			{
				//Move from inventory to current tab
				if(!this.getCurrentTab().quickMoveStack(slotStack))
				{
					//Else, move from inventory to additional slots
					if(!this.moveItemStackTo(slotStack, 36, this.slots.size(), false))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			else if(index < this.slots.size())
			{
				//Move from coin/interaction slots to inventory
				if(!this.moveItemStackTo(slotStack, 0, 36, false))
				{
					return ItemStack.EMPTY;
				}
			}
			
			if(slotStack.isEmpty())
			{
				slot.set(ItemStack.EMPTY);
			}
			else
			{
				slot.setChanged();
			}
		}
		
		return clickedStack;
		
	}
	
	public void changeTab(int key) {
		if(this.currentTab == key)
			return;
		if(this.availableTabs.containsKey(key))
		{
			if(this.availableTabs.get(key).canOpen(this.player))
			{
				//Close the old tab
				this.getCurrentTab().onTabClose();
				//Change the tab
				this.currentTab = key;
				//Open the new tab
				this.getCurrentTab().onTabOpen();
			}
		}
		else
			LightmansCurrency.LogWarning("Trader Storage Menu doesn't have a tab defined for " + key);
	}
	
	public void changeMode(ActiveMode newMode) {
		this.blockEntity.setMode(newMode);
		if(this.isClient())
		{
			CompoundTag message = new CompoundTag();
			message.putInt("ModeChange", newMode.index);
			this.sendMessage(message);
		}
	}
	
	public void setOnlineMode(boolean newMode) {
		this.blockEntity.setOnlineMode(newMode);
		if(this.isClient())
		{
			CompoundTag message = new CompoundTag();
			message.putBoolean("OnlineModeChange", newMode);
			this.sendMessage(message);
		}
	}
	
	public CompoundTag createTabChangeMessage(int newTab, @Nullable CompoundTag extraData) {
		CompoundTag message = extraData == null ? new CompoundTag() : extraData;
		message.putInt("ChangeTab", newTab);
		return message;
	}

	@Deprecated(since = "2.2.1.4")
	public void sendMessage(CompoundTag message) {
		if(this.isClient())
		{
			this.SendMessageToServer(LazyPacketData.simpleTag("OldMessage", message));
			//LightmansCurrency.LogInfo("Sending message:\n" + message.getAsString());
		}
	}

	@Deprecated(since = "2.2.1.4")
	private void receiveMessage(CompoundTag message) {
		//LightmansCurrency.LogInfo("Received nessage:\n" + message.getAsString());
		if(message.contains("ChangeTab", Tag.TAG_INT))
			this.changeTab(message.getInt("ChangeTab"));
		if(message.contains("ModeChange"))
			this.changeMode(ActiveMode.fromIndex(message.getInt("ModeChange")));
		if(message.contains("OnlineModeChange"))
			this.setOnlineMode(message.getBoolean("OnlineModeChange"));
		try { this.getCurrentTab().receiveMessage(message); }
		catch(Throwable ignored) { }
	}

	@Override
	public void HandleMessage(@Nonnull LazyPacketData message) {
		if(message.contains("ChangeTab"))
			this.changeTab(message.getInt("ChangeTab"));
		if(message.contains("ModeChange"))
			this.changeMode(ActiveMode.fromIndex(message.getInt("ModeChange")));
		if(message.contains("OnlineModeChange"))
			this.setOnlineMode(message.getBoolean("OnlineModeChange"));
		if(message.contains("OldMessage"))
			this.receiveMessage(message.getNBT("OldMessage"));
		try {
			this.getCurrentTab().handleMessage(message);
		} catch (Throwable t) { LightmansCurrency.LogError("Error handling Trader Interface Message!",t); }
	}
}
