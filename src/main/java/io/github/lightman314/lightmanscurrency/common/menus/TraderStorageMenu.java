package io.github.lightman314.lightmanscurrency.common.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyTabbedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;

public class TraderStorageMenu extends EasyTabbedMenu<ITraderStorageMenu,TraderStorageTab> implements IValidatedMenu, ITraderStorageMenu {

	@Override
	public Player getPlayer() { return this.player; }

	private final Supplier<TraderData> traderSource;
	public final TraderData getTrader() {
		TraderData trader = this.traderSource.get();
		return trader != null && trader.allowAccess() ? trader : null;
	}

	public static final int SLOT_OFFSET = 15;

	private final List<Consumer<LazyPacketData>> listeners = new ArrayList<>();

	private TradeContext context = null;

	@Override
	public TradeContext getContext() {
		TraderData trader = this.traderSource.get();
		if(this.context == null || this.context.getTrader() != trader)
			this.context = TradeContext.createStorageMode(trader);
		return this.context;
	}

	@Override
	public ItemStack getHeldItem() { return this.getCarried(); }
	@Override
	public void setHeldItem(ItemStack stack) { this.setCarried(stack); }

	private final MenuValidator validator;
	
	@Override
	public MenuValidator getValidator() { return this.validator; }

	public TraderStorageMenu(int windowID, Inventory inventory, long traderID,  MenuValidator validator) {
		this(ModMenus.TRADER_STORAGE.get(), windowID, inventory, () -> TraderAPI.getApi().GetTrader(inventory.player.level().isClientSide, traderID), validator);
	}
	
	protected TraderStorageMenu(MenuType<?> type, int windowID, Inventory inventory, Supplier<TraderData> traderSource, MenuValidator validator) {
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
    public void addTab(TraderStorageTab tab) { this.setTab(tab.getTabSlot(),tab); }
    @Override
    public void clearTab(ResourceLocation tabKey) { this.clearTab(this.getTabSlot(tabKey)); }

    public final int getTabSlot(ResourceLocation tabKey) {
        Map<Integer,TraderStorageTab> tabs = this.getAllTabs();
        //If we have a tab with the hash of the key (the default slot id), then assume it's the correct key
        int hash = tabKey.hashCode();
        if(tabs.containsKey(hash))
            return hash;
        //Attempt to find the tab with the given key, and locate its slot
        AtomicInteger result = new AtomicInteger(-1);
        tabs.forEach((slot,tab) -> {
            if(tab.tabKey().equals(tabKey))
                result.set(slot);
        });
        return result.get();
    }

    @Override
    public void ChangeTab(ResourceLocation tabKey) { this.ChangeTab(this.getTabSlot(tabKey)); }
    @Override
    public void ChangeTab(ResourceLocation tabKey, @Nullable LazyPacketData.Builder data) { this.ChangeTab(this.getTabSlot(tabKey),data); }
    @Override
    public void ChangeTab(ResourceLocation tabKey, @Nullable LazyPacketData data) { this.ChangeTab(this.getTabSlot(tabKey),data); }

    @Override
	protected void registerTabs() {
		TraderData trader = this.traderSource.get();
		if(trader != null)
			trader.initStorageTabs(this);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		TraderData trader = this.getTrader();
		if(trader != null)
			trader.userClose(player);
	}
	
	/**
	 * Public access to the AbstractContainerMenu.clearContainer(Player,Container) function.
	 */
    @Override
	public void clearContainer(Container container) { this.clearContainer(this.player, container); }
    @Override
    public void clearContainer(IItemHandler container) { this.clearContainer(this.player,container); }

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
	public int getPermissionLevel(String permission) {
		TraderData trader = this.getTrader();
		if(trader != null)
			return trader.getPermissionLevel(this.player, permission);
		return 0;
	}

    public final void openTrades()
    {
        if(this.isClient())
        {
            this.SendMessage(this.builder().setFlag("OpenTrades"));
            return;
        }
        TraderData trader = this.getTrader();
        if(trader != null)
            trader.openTraderMenu(player,this.validator);
        else
            this.player.closeContainer();
    }

    @Override
    protected void HandleMessages(LazyPacketData message) {
        super.HandleMessages(message);
        if(message.contains("OpenTrades"))
            this.openTrades();
    }
}
