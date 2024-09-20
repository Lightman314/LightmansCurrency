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
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.menus.MoneySlot;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.traders.menu.IMoneyCollectionMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStatsTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TaxInfoTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.logs.TraderLogTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.settings.TraderSettingsTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trade_rules.TradeRulesTab;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TraderStorageMenu extends LazyMessageMenu implements IValidatedMenu, ITraderStorageMenu, IMoneyCollectionMenu {

	@Nonnull
	@Override
	public Player getPlayer() { return this.player; }

	private final Supplier<TraderData> traderSource;
	public final TraderData getTrader() {
		TraderData trader = this.traderSource.get();
		return trader != null && trader.allowAccess() ? trader : null;
	}

	public static final int SLOT_OFFSET = 15;

	private final IMoneyHandler coinSlotHandler;
	private final Container coinSlotContainer;

	private boolean coinSlotsVisible = true;
	public boolean areCoinSlotsVisible() { return this.coinSlotsVisible; }
	List<MoneySlot> coinSlots = new ArrayList<>();
	public List<MoneySlot> getCoinSlots() { return this.coinSlots; }
	
	private boolean canEditTabs;
	Map<Integer,TraderStorageTab> availableTabs = new HashMap<>();
	public Map<Integer,TraderStorageTab> getAllTabs() { return this.availableTabs; }
	@Override
	public void setTab(int key, @Nonnull TraderStorageTab tab) { if(this.canEditTabs) this.availableTabs.put(key, tab); else LightmansCurrency.LogError("Attempted to define the tab in " + key + " but the tabs have been locked."); }
	@Override
	public void clearTab(int key)
	{
		if(this.canEditTabs)
		{
			if(key == TraderStorageTab.TAB_TRADE_BASIC)
				LightmansCurrency.LogError("Attempted to clear the basic trade tab!\nTabs at this index cannot be removed, as there must be one present at all times.\nIf you wish to replace this tab with your own use setTab!");
			else
				this.availableTabs.remove(key);
		}
		else
			LightmansCurrency.LogError("Attempted to clear the tab in " + key + " but the tabs have been locked.");
	}

	int currentTab = TraderStorageTab.TAB_TRADE_BASIC;
	public int getCurrentTabIndex() { return this.currentTab; }
	public TraderStorageTab getCurrentTab() { return this.availableTabs.get(this.currentTab); }

	private final List<Consumer<LazyPacketData>> listeners = new ArrayList<>();

	private TradeContext context = null;

	@Nonnull
	@Override
	public TradeContext getContext() {
		TraderData trader = this.traderSource.get();
		if(this.context == null || this.context.getTrader() != trader)
			this.context = TradeContext.createStorageMode(trader);
		return this.context;
	}

	@Override
	@Nonnull
	public ItemStack getHeldItem() { return this.getCarried(); }
	@Override
	public void setHeldItem(@Nonnull ItemStack stack) { this.setCarried(stack); }

	private final MenuValidator validator;
	@Nonnull
	@Override
	public MenuValidator getValidator() { return this.validator; }

	public TraderStorageMenu(int windowID, Inventory inventory, long traderID, @Nonnull  MenuValidator validator) {
		this(ModMenus.TRADER_STORAGE.get(), windowID, inventory, () -> TraderSaveData.GetTrader(inventory.player.level().isClientSide, traderID), validator);
	}
	
	protected TraderStorageMenu(MenuType<?> type, int windowID, Inventory inventory, Supplier<TraderData> traderSource, @Nonnull MenuValidator validator) {
		super(type, windowID, inventory);
		this.validator = validator;
		this.traderSource = traderSource;
		this.coinSlotContainer = new SimpleContainer(5);
		this.coinSlotHandler = MoneyAPI.API.GetContainersMoneyHandler(this.coinSlotContainer, inventory.player);

		this.addValidator(() -> this.hasPermission(Permissions.OPEN_STORAGE));
		this.addValidator(this.validator);

		this.canEditTabs = true;
		TraderData trader = this.traderSource.get();
		this.setTab(TraderStorageTab.TAB_TRADE_BASIC, new BasicTradeEditTab(this));
		this.setTab(TraderStorageTab.TAB_TRADER_LOGS, new TraderLogTab(this));
		this.setTab(TraderStorageTab.TAB_TRADER_SETTINGS, new TraderSettingsTab(this));
		this.setTab(TraderStorageTab.TAB_TRADER_STATS, new TraderStatsTab(this));
		this.setTab(TraderStorageTab.TAB_RULES_TRADER, new TradeRulesTab.Trader(this));
		this.setTab(TraderStorageTab.TAB_RULES_TRADE, new TradeRulesTab.Trade(this));
		this.setTab(TraderStorageTab.TAB_TAX_INFO, new TaxInfoTab(this));
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
		for(int x = 0; x < this.coinSlotContainer.getContainerSize(); x++)
		{
			MoneySlot newSlot  = new MoneySlot(this.coinSlotContainer, x, SLOT_OFFSET + 8 + (x + 4) * 18, 122, this.getPlayer());
			this.coinSlots.add(newSlot);
			this.addSlot(newSlot);
		}
		
		this.availableTabs.forEach((key, tab) -> tab.addStorageMenuSlots(this::addSlot));
		
		//Run the tab open code for the current tab
		try {
			this.getCurrentTab().onTabOpen();
		} catch(Throwable t) { LightmansCurrency.LogError("Error opening storage tab.",t); }
		
		this.getTrader().userOpen(this.player);
		
	}
	
	@Override
	public void removed(@Nonnull Player player) {
		super.removed(player);
		this.clearContainer(player, this.coinSlotContainer);
		this.availableTabs.forEach((key, tab) -> tab.onMenuClose());
		TraderData trader = this.getTrader();
		if(trader != null)
			trader.userClose(player);
	}
	
	/**
	 * Public access to the AbstractContainerMenu.clearContainer(Player,Container) function.
	 */
	public void clearContainer(@Nonnull Container container) {
		this.clearContainer(this.player, container);
	}

	public void validateCoinSlots() {
		boolean canAddCoins = this.hasCoinSlotAccess();
		for(MoneySlot slot : this.coinSlots) slot.active = canAddCoins && this.coinSlotsVisible;
	}
	
	private boolean hasCoinSlotAccess() {
		TraderData trader = this.getTrader();
		return trader != null && trader.hasPermission(this.player, Permissions.STORE_COINS) && !trader.hasBankAccount();
	}
	
	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player playerEntity, int index)
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

	@Override
	public int getPermissionLevel(@Nonnull String permission) {
		TraderData trader = this.getTrader();
		if(trader != null)
			return trader.getPermissionLevel(this.player, permission);
		return 0;
	}

	@Override
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

	public void changeTab(int key, @Nullable LazyPacketData.Builder message)
	{
		this.changeTab(key);
		if(message != null)
			this.HandleMessage(message.build());
		this.SendMessage(this.createTabChangeMessage(key,message));
	}

	@Nonnull
	public LazyPacketData.Builder createTabChangeMessage(int newTab) { return this.createTabChangeMessage(newTab, null); }
	@Nonnull
	public LazyPacketData.Builder createTabChangeMessage(int newTab, @Nullable LazyPacketData.Builder extraData) {
		LazyPacketData.Builder message = extraData == null ? LazyPacketData.builder() : extraData;
		message.setInt("ChangeTab", newTab);
		return message;
	}


	public void SetCoinSlotsActive(boolean nowActive)
	{
		this.coinSlotsVisible = nowActive;
		MoneySlot.SetActive(this.coinSlots, nowActive);
		if(this.isClient())
			this.SendMessage(this.createCoinSlotActiveMessage(nowActive, null));
	}

	public LazyPacketData.Builder createCoinSlotActiveMessage(boolean nowActive, @Nullable LazyPacketData.Builder extraData) {
		LazyPacketData.Builder message = extraData == null ? LazyPacketData.builder() : extraData;
		message.setBoolean("SetCoinSlotsActive", nowActive);
		return message;
	}

	@Override
	public void HandleMessage(@Nonnull LazyPacketData message) {
		//Change Tab
		if(message.contains("ChangeTab", LazyPacketData.TYPE_INT))
			this.changeTab(message.getInt("ChangeTab"));
		//Set Coin Slots Active/Inactive
		if(message.contains("SetCoinSlotsActive", LazyPacketData.TYPE_BOOLEAN))
			this.SetCoinSlotsActive(message.getBoolean("SetCoinSlotsActive"));
		//Tab Listener
		try { this.getCurrentTab().receiveMessage(message); }
		catch (Throwable ignored) {}
		for(Consumer<LazyPacketData> listener : this.listeners)
			try { listener.accept(message); } catch (Throwable ignored) {}

	}

	public void addListener(Consumer<LazyPacketData> listener) {
		if(!this.listeners.contains(listener) && listener != null)
			this.listeners.add(listener);
	}
	
	public boolean HasCoinsToAdd() { return !this.coinSlotHandler.getStoredMoney().isEmpty(); }

	public void CollectStoredMoney() {
		
		TraderData trader = this.getTrader();
		if(trader == null)
		{
			this.player.closeContainer();
			return;
		}
		trader.CollectStoredMoney(this.player);
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
			MoneyView addAmount = this.coinSlotHandler.getStoredMoney();
			for(MoneyValue value : addAmount.allValues())
				trader.addStoredMoney(value, false);
			this.coinSlotContainer.clearContent();
		}
		else
			Permissions.PermissionWarning(this.player, "store coins", Permissions.STORE_COINS);
	}
	
}
