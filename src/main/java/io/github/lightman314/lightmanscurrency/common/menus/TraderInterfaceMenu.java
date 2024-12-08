package io.github.lightman314.lightmanscurrency.common.menus;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity.ActiveMode;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyTabbedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base.*;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.BlockEntityValidator;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TraderInterfaceMenu extends EasyTabbedMenu<TraderInterfaceMenu,TraderInterfaceTab> {

	private final TraderInterfaceBlockEntity blockEntity;
	public final TraderInterfaceBlockEntity getBE() { return this.blockEntity; }
	
	public static final int SLOT_OFFSET = 15;

    public TraderInterfaceMenu(int windowID, Inventory inventory, TraderInterfaceBlockEntity blockEntity) {
		super(ModMenus.TRADER_INTERFACE.get(), windowID, inventory);
		this.blockEntity = blockEntity;

		this.addValidator(BlockEntityValidator.of(this.blockEntity));
		this.addValidator(this.blockEntity::canAccess);
		this.addValidator(() -> !QuarantineAPI.IsDimensionQuarantined(this.blockEntity));
		
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

		this.initializeTabs();
		
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

	public TradeContext getTradeContext(@Nonnull TraderData trader) {
		return this.blockEntity.getTradeContext(trader);
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
	public void HandleMessages(@Nonnull LazyPacketData message) {
		if(message.contains("ModeChange"))
			this.changeMode(ActiveMode.fromIndex(message.getInt("ModeChange")));
		if(message.contains("OnlineModeChange"))
			this.setOnlineMode(message.getBoolean("OnlineModeChange"));
	}
}
