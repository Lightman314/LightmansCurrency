package io.github.lightman314.lightmanscurrency.common.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStorageInteraction;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStorageInteractionC;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;

public class TraderStorageMenu extends Container {

	public final Supplier<TraderData> traderSource;
	public final TraderData getTrader() { return this.traderSource.get(); }
	public final PlayerEntity player;
	
	public static final int SLOT_OFFSET = 15;
	
	IInventory coinSlotContainer = new Inventory(5);
	List<CoinSlot> coinSlots = new ArrayList<>();
	public List<CoinSlot> getCoinSlots() { return this.coinSlots; }
	public boolean coinSlotsActive() { return this.coinSlots.get(0).isActive(); }
	
	private boolean canEditTabs;
	Map<Integer, TraderStorageTab> availableTabs = new HashMap<>();
	public Map<Integer,TraderStorageTab> getAllTabs() { return this.availableTabs; }
	public void setTab(int key, TraderStorageTab tab) { if(canEditTabs && tab != null) this.availableTabs.put(key, tab); else if(tab == null) LightmansCurrency.LogError("Attempted to set a null storage tab in slot " + key); else LightmansCurrency.LogError("Attempted to define the tab in " + key + " but the tabs have been locked."); }
	int currentTab = TraderStorageTab.TAB_TRADE_BASIC;
	public int getCurrentTabIndex() { return this.currentTab; }
	public TraderStorageTab getCurrentTab() { return this.availableTabs.get(this.currentTab); }
	
	private final List<Consumer<CompoundNBT>> listeners = new ArrayList<>();
	
	public TradeContext getContext() { return TradeContext.createStorageMode(this.traderSource.get()); }
	
	public boolean isClient() { return this.player.level.isClientSide; }
	
	public TraderStorageMenu(int windowID, PlayerInventory inventory, long traderID) {
		this(ModMenus.TRADER_STORAGE.get(), windowID, inventory, () -> TraderSaveData.GetTrader(inventory.player.level.isClientSide, traderID));
	}
	
	protected TraderStorageMenu(ContainerType<?> type, int windowID, PlayerInventory inventory, Supplier<TraderData> traderSource) {
		super(type, windowID);
		this.traderSource = traderSource;
		this.player = inventory.player;

		this.canEditTabs = true;
		TraderData trader = this.traderSource.get();
		this.setTab(TraderStorageTab.TAB_TRADE_BASIC, new BasicTradeEditTab(this));
		if(trader != null)
			trader.initStorageTabs(this);
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
		
		//Coin Slots
		for(int x = 0; x < coinSlotContainer.getContainerSize(); x++)
		{
			CoinSlot newSlot  = new CoinSlot(this.coinSlotContainer, x, SLOT_OFFSET + 8 + (x + 4) * 18, 122);
			this.coinSlots.add(newSlot);
			this.addSlot(newSlot);
		}
		
		this.availableTabs.forEach((key, tab) -> tab.addStorageMenuSlots(this::addSlot));
		
		//Run the tab open code for the current tab
		try {
			this.getCurrentTab().onTabOpen();
		} catch(Throwable t) { t.printStackTrace(); }
		
		this.getTrader().userOpen(this.player);
		
	}
	
	@Override
	public void removed(@Nonnull PlayerEntity player) {
		super.removed(player);
		this.clearContainer(player, player.level, this.coinSlotContainer);
		this.availableTabs.forEach((key, tab) -> tab.onMenuClose());
		TraderData trader = this.getTrader();
		if(trader != null) trader.userClose(player);
	}
	
	/**
	 * Public access to the AbstractContainerMenu.clearContainer(Player,Container) function.
	 */
	public void clearContainer(IInventory container) {
		this.clearContainer(this.player, this.player.level, container);
	}

	@Override
	public boolean stillValid(@Nonnull PlayerEntity player) { return this.traderSource != null && this.traderSource.get() != null && this.traderSource.get().hasPermission(player, Permissions.OPEN_STORAGE); }
	
	public void validateCoinSlots() {
		boolean canAddCoins = this.hasCoinSlotAccess();
		for(CoinSlot slot : this.coinSlots) slot.active = canAddCoins;
	}
	
	private boolean hasCoinSlotAccess() {
		TraderData trader = this.getTrader();
		return trader != null && trader.hasPermission(this.player, Permissions.STORE_COINS) && !trader.hasBankAccount();
	}
	
	@Override
	public @Nonnull ItemStack quickMoveStack(@Nonnull PlayerEntity playerEntity, int index)
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
					if(this.hasCoinSlotAccess())
					{
						//Move to coin slots
						if(!this.moveItemStackTo(slotStack, 36, 36 + this.coinSlots.size(), false))
						{
							//Else, move from inventory to additional slots
							if(!this.moveItemStackTo(slotStack, 36 + this.coinSlots.size(), this.slots.size(), false))
							{
								return ItemStack.EMPTY;
							}
						}
					}
					//Else, move from inventory to additional slots
					else if(!this.moveItemStackTo(slotStack, 36 + this.coinSlots.size(), this.slots.size(), false))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			else if(index < this.slots.size())
			{
				//Move from coin/additional slots to inventory
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
	
	public boolean hasPermission(String permission) { return this.getPermissionLevel(permission) > 0; }
	
	public int getPermissionLevel(String permission) {
		TraderData trader = this.getTrader();
		if(trader != null)
			return trader.getPermissionLevel(this.player, permission);
		return 0;
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
	
	public CompoundNBT createTabChangeMessage(int newTab, @Nullable CompoundNBT extraData) {
		CompoundNBT message = extraData == null ? new CompoundNBT() : extraData;
		message.putInt("ChangeTab", newTab);
		return message;
	}
	
	public CompoundNBT createCoinSlotActiveMessage(boolean nowActive, @Nullable CompoundNBT extraData) {
		CompoundNBT message = extraData == null ? new CompoundNBT() : extraData;
		message.putBoolean("SetCoinSlotsActive", nowActive);
		return message;
	}
	
	public void sendMessage(CompoundNBT message) {
		if(this.isClient())
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageStorageInteraction(message));
		}
		else
		{
			LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(this.player), new MessageStorageInteractionC(message));
		}
	}
	
	public void receiveMessage(CompoundNBT message) {
		//LightmansCurrency.LogInfo("Received message:\n" + message.getAsString());
		if(message.contains("ChangeTab", Constants.NBT.TAG_INT))
			this.changeTab(message.getInt("ChangeTab"));
		if(message.contains("SetCoinSlotsActive"))
			SimpleSlot.SetActive(this.coinSlots, message.getBoolean("SetCoinSlotsActive"));
		try { this.getCurrentTab().receiveMessage(message); }
		catch(Throwable ignored) { }
		for(Consumer<CompoundNBT> listener : this.listeners)
			try { listener.accept(message); } catch(Throwable ignored) {}
	}
	
	public void addMessageListener(Consumer<CompoundNBT> listener) {
		if(!this.listeners.contains(listener) && listener != null)
			this.listeners.add(listener);
	}
	
	public interface IClientMessage {
		void selfMessage(CompoundNBT message);
	}
	
	public boolean HasCoinsToAdd() { return MoneyUtil.getValue(this.coinSlotContainer) > 0; }

	public void CollectCoinStorage() {
		
		TraderData trader = this.getTrader();
		if(trader == null)
		{
			this.player.closeContainer();
			return;
		}
		if(trader.hasPermission(this.player, Permissions.COLLECT_COINS))
		{
			CoinValue storedMoney = trader.getInternalStoredMoney();
			if(storedMoney.getRawValue() > 0)
			{
				TradeContext tempContext = TradeContext.create(trader, this.player).withCoinSlots(this.coinSlotContainer).build();
				if(tempContext.givePayment(storedMoney))
					trader.clearStoredMoney();
			}
		}
		else
			Permissions.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
	}
	
	public void AddCoins() {
		
		TraderData trader = this.getTrader();
		if(trader == null)
		{
			this.player.closeContainer();
			return;
		}
		if(trader.hasPermission(this.player, Permissions.STORE_COINS))
		{
			CoinValue addAmount = CoinValue.easyBuild2(this.coinSlotContainer);
			trader.addStoredMoney(addAmount);
			this.coinSlotContainer.clearContent();
		}
		else
			Permissions.PermissionWarning(this.player, "store coins", Permissions.STORE_COINS);
	}
	
}