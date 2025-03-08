package io.github.lightman314.lightmanscurrency.common.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.api.misc.menus.MoneySlot;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyTabbedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core.*;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TraderStorageMenu extends EasyTabbedMenu<ITraderStorageMenu,TraderStorageTab> implements IValidatedMenu, ITraderStorageMenu {

	@Nonnull
	@Override
	public Player getPlayer() { return this.player; }

	private final Supplier<TraderData> traderSource;
	public final TraderData getTrader() {
		TraderData trader = this.traderSource.get();
		return trader != null && trader.allowAccess() ? trader : null;
	}

	public static final int SLOT_OFFSET = 15;

	@Deprecated(since = "2.2.4.4")
	public List<MoneySlot> getCoinSlots() { return new ArrayList<>(); }

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
		this(ModMenus.TRADER_STORAGE.get(), windowID, inventory, () -> TraderAPI.API.GetTrader(inventory.player.level().isClientSide, traderID), validator);
	}
	
	protected TraderStorageMenu(MenuType<?> type, int windowID, Inventory inventory, Supplier<TraderData> traderSource, @Nonnull MenuValidator validator) {
		super(type, windowID, inventory);
		this.validator = validator;
		this.traderSource = traderSource;

		this.addValidator(() -> this.hasPermission(Permissions.OPEN_STORAGE));
		this.addValidator(this.validator);
		
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

		//Initialize tabs *after* adding normal slots so that the slot indexes line up as expected
		this.initializeTabs();
		
		this.getTrader().userOpen(this.player);
		
	}

	@Override
	protected void registerTabs() {
		TraderData trader = this.traderSource.get();
		this.setTab(TraderStorageTab.TAB_TRADE_BASIC, new BasicTradeEditTab(this));
		this.setTab(TraderStorageTab.TAB_TRADE_MONEY_STORAGE, new TraderMoneyStorageTab(this));
		this.setTab(TraderStorageTab.TAB_TRADE_MULTI_PRICE, new MultiPriceTab(this));
		this.setTab(TraderStorageTab.TAB_TRADER_LOGS, new TraderLogTab(this));
		this.setTab(TraderStorageTab.TAB_TRADER_SETTINGS, new TraderSettingsTab(this));
		this.setTab(TraderStorageTab.TAB_TRADER_STATS, new TraderStatsTab(this));
		this.setTab(TraderStorageTab.TAB_RULES_TRADER, new TradeRulesTab.Trader(this));
		this.setTab(TraderStorageTab.TAB_RULES_TRADE, new TradeRulesTab.Trade(this));
		this.setTab(TraderStorageTab.TAB_TAX_INFO, new TaxInfoTab(this));
		if(trader != null)
			trader.initStorageTabs(this);
	}

	@Override
	public void removed(@Nonnull Player player) {
		super.removed(player);
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
	
	@Override
	@Nonnull
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
				if(!this.currentTab().quickMoveStack(slotStack))
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

	@Deprecated(since = "2.2.4.4")
	public void SetCoinSlotsActive(boolean nowActive) { }
	
}
