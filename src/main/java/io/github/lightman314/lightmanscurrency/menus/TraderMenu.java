package io.github.lightman314.lightmanscurrency.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.core.ModMenus;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.menus.slots.InteractionSlot;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.ITraderSource;
import io.github.lightman314.lightmanscurrency.trader.common.InteractionSlotData;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TraderMenu extends AbstractContainerMenu implements ITraderMenu{

	public final Supplier<ITraderSource> traderSource;
	public final Player player;
	
	public static final int SLOT_OFFSET = 15;
	
	InteractionSlot interactionSlot;
	public InteractionSlot getInteractionSlot() { return this.interactionSlot; }
	Container coins = new SimpleContainer(5);
	public Container getCoinInventory() { return this.coins; }
	
	List<Slot> coinSlots = new ArrayList<>();
	public List<Slot> getCoinSlots() { return this.coinSlots; }
	
	public TraderMenu(int windowID, Inventory inventory, BlockPos sourcePosition) {
		this(ModMenus.TRADER.get(), windowID, inventory, () -> {
			BlockEntity be = inventory.player.level.getBlockEntity(sourcePosition);
			if(be instanceof ITraderSource)
				return (ITraderSource)be;
			return null;
		});
	}
	
	protected TraderMenu(MenuType<?> type, int windowID, Inventory inventory, Supplier<ITraderSource> traderSource) {
		super(type, windowID);
		this.player = inventory.player;
		this.traderSource = traderSource;
		this.init(this.player, inventory);
		for(ITrader trader : this.traderSource.get().getTraders())
			trader.userOpen(this.player);
	}
	
	public TradeContext getContext(ITrader trader) { 
		return TradeContext.create(trader, this.player).withCoinSlots(this.coins).withInteractionSlot(this.interactionSlot).build();
	}

	protected void init(Player player, Inventory inventory) {
		
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
		for(int x = 0; x < coins.getContainerSize(); x++)
		{
			this.coinSlots.add(this.addSlot(new CoinSlot(this.coins, x, SLOT_OFFSET + 8 + (x + 4) * 18, 122)));
		}
		
		//Interaction Slots
		List<InteractionSlotData> slotData = new ArrayList<>();
		for(ITrader trader : this.traderSource.get().getTraders())
			trader.addInteractionSlots(slotData);
		this.interactionSlot = new InteractionSlot(slotData, SLOT_OFFSET + 8, 122);
		this.addSlot(this.interactionSlot);
		
	}

	@Override
	public boolean stillValid(Player player) { return this.traderSource != null && this.traderSource.get() != null && this.traderSource.get().getTraders() != null && this.traderSource.get().getTraders().size() > 0; }
	
	@Override
	public void removed(Player player) {
		super.removed(player);
		this.clearContainer(player, this.coins);
		this.clearContainer(player, this.interactionSlot.container);
		for(ITrader trader : this.traderSource.get().getTraders())
			trader.userClose(this.player);
	}
	
	@Override
	public void ExecuteTrade(int traderIndex, int tradeIndex) {
		//LightmansCurrency.LogInfo("Executing trade " + traderIndex + "/" + tradeIndex);
		ITraderSource traderSource = this.traderSource.get();
		if(traderSource == null)
		{
			this.player.closeContainer();
			return;
		}
		List<ITrader> traderList = traderSource.getTraders();
		if(traderIndex >= 0 && traderIndex < traderList.size())
		{
			ITrader trader = traderSource.getTraders().get(traderIndex);
			if(trader == null)
			{
				LightmansCurrency.LogWarning("Trader at index " + traderIndex + " is null.");
				return;
			}
			TradeResult result = trader.ExecuteTrade(this.getContext(trader), tradeIndex);
			if(result.hasMessage())
				LightmansCurrency.LogInfo(result.failMessage.getString());
		}
		else
			LightmansCurrency.LogWarning("Trader " + traderIndex + " is not a valid trader index.");
	}
	
	public boolean isSingleTrader() {
		ITraderSource tradeSource = this.traderSource.get();
		if(tradeSource == null)
		{
			this.player.closeContainer();
			return false;
		}
		return tradeSource.isSingleTrader() && tradeSource.getTraders().size() == 1;
	}
	
	public ITrader getSingleTrader() {
		if(this.isSingleTrader())
			return this.traderSource.get().getSingleTrader();
		return null;
	}
	
	public boolean isUniversalTrader() { return false; }
	
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
				//Move from inventory to coin/interaction slots
				if(!this.moveItemStackTo(slotStack, 36, this.slots.size(), false))
				{
					return ItemStack.EMPTY;
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
	
	@Override
	public void CollectCoinStorage() {
		if(this.isSingleTrader())
		{
			LightmansCurrency.LogInfo("Attempting to collect coins from trader.");
			ITrader trader = this.getSingleTrader();
			if(trader.hasPermission(this.player, Permissions.COLLECT_COINS))
			{
				CoinValue payment = trader.getInternalStoredMoney();
				if(this.getContext(trader).givePayment(payment))
					trader.clearStoredMoney();
			}
			else
				Settings.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
		}
	}
	
	public static class TraderMenuUniversal extends TraderMenu
	{
		public TraderMenuUniversal(int windowID, Inventory inventory, UUID traderID) {
			super(ModMenus.TRADER_UNIVERSAL.get(), windowID, inventory, () ->{
				if(inventory.player.level.isClientSide)
					return ClientTradingOffice.getData(traderID);
				else
					return TradingOffice.getData(traderID);
			});
		}
		
		@Override
		public boolean isUniversalTrader() { return true; }
		
	}

	public static class TraderMenuAllUniversal extends TraderMenu
	{
		public TraderMenuAllUniversal(int windowID, Inventory inventory) {
			super(ModMenus.TRADER_UNIVERSAL_ALL.get(), windowID, inventory, ITraderSource.UniversalTraderSource(inventory.player.level.isClientSide));
		}
		
		@Override
		public boolean isUniversalTrader() { return true; }
	}
	
	
}
