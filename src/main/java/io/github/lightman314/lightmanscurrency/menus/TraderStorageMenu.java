package io.github.lightman314.lightmanscurrency.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.core.ModMenus;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStorageInteraction;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TraderStorageMenu extends AbstractContainerMenu implements ITraderStorageMenu {

	public final Supplier<ITrader> traderSource;
	public final ITrader getTrader() { return this.traderSource.get(); }
	public final Player player;
	
	public static final int SLOT_OFFSET = 15;
	
	Container coinSlotContainer = new SimpleContainer(5);
	List<CoinSlot> coinSlots = new ArrayList<>();
	public List<CoinSlot> getCoinSlots() { return this.coinSlots; }
	public boolean coinSlotsActive() { return this.coinSlots.get(0).isActive(); }
	
	private boolean canEditTabs = true;
	Map<Integer,TraderStorageTab> availableTabs = new HashMap<>();
	public Map<Integer,TraderStorageTab> getAllTabs() { return this.availableTabs; }
	public void setTab(int key, TraderStorageTab tab) { if(canEditTabs && tab != null) this.availableTabs.put(key, tab); else if(tab == null) LightmansCurrency.LogError("Attempted to set a null storage tab in slot " + key); else LightmansCurrency.LogError("Attempted to define the tab in " + key + " but the tabs have been locked."); }
	int currentTab = TraderStorageTab.TAB_TRADE_BASIC;
	public int getCurrentTabIndex() { return this.currentTab; }
	public TraderStorageTab getCurrentTab() { return this.availableTabs.get(this.currentTab); }
	
	public TradeContext getContext() { return TradeContext.createStorageMode(this.traderSource.get()); }
	
	public boolean isClient() { return this.player.level.isClientSide; }
	
	public TraderStorageMenu(int windowID, Inventory inventory, BlockPos traderPos) {
		this(ModMenus.TRADER_STORAGE, windowID, inventory, () ->{
			BlockEntity be = inventory.player.level.getBlockEntity(traderPos);
			if(be instanceof ITrader)
				return (ITrader)be;
			return null;
		});
	}
	
	protected TraderStorageMenu(MenuType<?> type, int windowID, Inventory inventory, Supplier<ITrader> traderSource) {
		super(type, windowID);
		this.traderSource = traderSource;
		this.player = inventory.player;
		
		ITrader trader = this.traderSource.get();
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
	public void removed(Player player) {
		super.removed(player);
		this.clearContainer(player, this.coinSlotContainer);
		this.availableTabs.forEach((key, tab) -> tab.onMenuClose());
		ITrader trader = this.getTrader();
		if(trader != null) trader.userClose(player);
	}
	
	/**
	 * Public access to the AbstractContainerMenu.clearContainer(Player,Container) function.
	 */
	public void clearContainer(Container container) {
		this.clearContainer(this.player, container);
	}

	@Override
	public boolean stillValid(Player player) { return this.traderSource != null && this.traderSource.get() != null && this.traderSource.get().hasPermission(player, Permissions.OPEN_STORAGE); }
	
	@Override
	public ItemStack quickMoveStack(Player playerEntity, int index)
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
	
	public boolean hasPermission(String permission) { 
		ITrader trader = this.getTrader();
		if(trader != null)
			return trader.hasPermission(this.player, permission);
		return false;
	}
	
	public int getPermissionLevel(String permission) {
		ITrader trader = this.getTrader();
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
	
	public CompoundTag createTabChangeMessage(int newTab, @Nullable CompoundTag extraData) {
		CompoundTag message = extraData == null ? new CompoundTag() : extraData;
		message.putInt("ChangeTab", newTab);
		return message;
	}
	
	public CompoundTag createCoinSlotActiveMessage(boolean nowActive, @Nullable CompoundTag extraData) {
		CompoundTag message = extraData == null ? new CompoundTag() : extraData;
		message.putBoolean("SetCoinSlotsActive", nowActive);
		return message;
	}
	
	public void sendMessage(CompoundTag message) {
		if(this.isClient())
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageStorageInteraction(message));
			//LightmansCurrency.LogInfo("Sending message:\n" + message.getAsString());
		}
	}
	
	public void receiveMessage(CompoundTag message) {
		//LightmansCurrency.LogInfo("Received nessage:\n" + message.getAsString());
		if(message.contains("ChangeTab", Tag.TAG_INT))
			this.changeTab(message.getInt("ChangeTab"));
		if(message.contains("SetCoinSlotsActive"))
			SimpleSlot.SetActive(this.coinSlots, message.getBoolean("SetCoinSlotsActive"));
		try { this.getCurrentTab().receiveMessage(message); }
		catch(Throwable t) { }
	}
	
	public interface IClientMessage {
		public void selfMessage(CompoundTag message);
	}
	
	public static class TraderStorageMenuUniversal extends TraderStorageMenu {

		public TraderStorageMenuUniversal(int windowID, Inventory inventory, UUID traderID) {
			super(ModMenus.TRADER_STORAGE_UNIVERSAL, windowID, inventory, () ->{
				if(inventory.player.level.isClientSide)
					return ClientTradingOffice.getData(traderID);
				else
					return TradingOffice.getData(traderID);
			});
		}
		
	}
	
	public boolean HasCoinsToAdd() { return MoneyUtil.getValue(this.coinSlotContainer) > 0; }

	@Override
	public void CollectCoinStorage() {
		
		ITrader trader = this.getTrader();
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
			else
				Settings.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
		}
	}
	
	@Override
	public void AddCoins() {
		
		ITrader trader = this.getTrader();
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
			Settings.PermissionWarning(this.player, "store coins", Permissions.STORE_COINS);
		
	}
	
}
