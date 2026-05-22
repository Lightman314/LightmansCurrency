package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity.ActiveMode;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.tracking.OwnerTraderTrackingHolder;
import io.github.lightman314.lightmanscurrency.api.traders.tracking.TrackingLevel;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyTabbedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base.*;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.BlockEntityValidator;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.Set;

public class TraderInterfaceMenu extends EasyTabbedMenu<TraderInterfaceMenu,TraderInterfaceTab> {

	private final TraderInterfaceBlockEntity<?> blockEntity;
	public final TraderInterfaceBlockEntity<?> getBE() { return this.blockEntity; }

    private final OwnerTraderTrackingHolder trackingHolder = new OwnerTraderTrackingHolder(this,TrackingLevel.CUSTOMER);
    private Set<Long> trackingCache = new HashSet<>();


    public static final int SLOT_OFFSET = 15;

    public TraderInterfaceMenu(int windowID,Inventory inventory,TraderInterfaceBlockEntity<?> blockEntity) {
		super(ModMenus.TRADER_INTERFACE.get(), windowID, inventory);
		this.blockEntity = blockEntity;

		this.addValidator(BlockEntityValidator.of(this.blockEntity));
		this.addValidator(this.blockEntity::canAccess);
		this.addValidator(() -> !QuarantineAPI.IsDimensionQuarantined(this.blockEntity));
		
		//Player items
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

		this.initializeTabs();

        //Initialize the tracking
        NeoForge.EVENT_BUS.register(this);
		
	}

	@Override
	protected void registerTabs() {
		this.setTab(TraderInterfaceTab.TAB_INFO, new InfoTab(this));
		this.setTab(TraderInterfaceTab.TAB_TRADER_SELECT, new TraderSelectTab(this));
		this.setTab(TraderInterfaceTab.TAB_TRADE_SELECT, new TradeSelectTab(this));
		this.setTab(TraderInterfaceTab.TAB_STATS, new InterfaceStatsTab(this));
		this.setTab(TraderInterfaceTab.TAB_OWNERSHIP, new OwnershipTab(this));
		if(this.blockEntity != null)
			this.blockEntity.initMenuTabs(this);
	}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void serverTick(ServerTickEvent.Post event)
    {
        if(this.isClient())
            return;
        Set<Long> found = new HashSet<>();
        OwnerData owner = this.blockEntity.owner;
        for(TraderData trader : this.blockEntity.targets.getTraders())
        {
            this.trackingHolder.requestTracking(trader,this.player,owner);
            found.add(trader.getID());
        }
        for(long wasTracking : this.trackingCache)
        {
            if(!found.contains(wasTracking))
                this.trackingHolder.endTracking(wasTracking,this.player);
        }
        this.trackingCache = found;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        NeoForge.EVENT_BUS.unregister(this);
    }

    public TradeContext getTradeContext(TraderData trader) {
		return this.blockEntity.getTradeContext(trader);
	}

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
				//Move from items to current tab
				if(!this.currentTab().quickMoveStack(slotStack))
				{
					//Else, move from items to additional slots
					if(!this.moveItemStackTo(slotStack, 36, this.slots.size(), false))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			else if(index < this.slots.size())
			{
				//Move from coin/interaction slots to items
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
	
	public void changeMode(ActiveMode newMode) {
		this.blockEntity.setMode(newMode);
		if(this.isClient())
			this.SendMessage(this.builder().setInt("ModeChange", newMode.index));
	}
	
	public void setOnlineMode(boolean newMode) {
		this.blockEntity.setOnlineMode(newMode);
		if(this.isClient())
			this.SendMessage(this.builder().setBoolean("OnlineModeChange", newMode));
	}

	@Override
	public void HandleMessages(LazyPacketData message) {
		if(message.contains("ModeChange"))
			this.changeMode(ActiveMode.fromIndex(message.getInt("ModeChange")));
		if(message.contains("OnlineModeChange"))
			this.setOnlineMode(message.getBoolean("OnlineModeChange"));
	}
}
