package io.github.lightman314.lightmanscurrency.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.ItemTraderUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.CashRegisterBlockEntity;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
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

/**
 * @deprecated Use TraderMenu as of v1.1.0.0
 */
@Deprecated
public class ItemTraderMenu extends AbstractContainerMenu implements ITraderMenu {
	
	public final Player player;
	
	protected final Container coinSlots = new SimpleContainer(5);
	protected final Container itemSlots = new SimpleContainer(3);
	
	private final Supplier<IItemTrader> traderSource;
	public final IItemTrader getTrader() { return this.traderSource == null? null : this.traderSource.get();  }
	
	public ItemTraderMenu(int windowId, Inventory inventory, BlockPos traderPos)
	{
		this(/*ModMenus.ITEM_TRADER*/ null, windowId, inventory, traderPos);
	}
	
	protected ItemTraderMenu(MenuType<?> type, int windowId, Inventory inventory, BlockPos traderPos)
	{
		this(type, windowId, inventory, () -> {
			BlockEntity be = inventory.player.level.getBlockEntity(traderPos);
			if(be instanceof IItemTrader)
				return (IItemTrader)be;
			return null;
		});
	}
	
	protected ItemTraderMenu(MenuType<?> type, int windowId, Inventory inventory, Supplier<IItemTrader> traderSource)
	{
		super(type, windowId);
		this.traderSource = traderSource;
		
		this.player = inventory.player;
		
		this.getTrader().userOpen(this.player);
		
		//Coin Slots
		for(int x = 0; x < coinSlots.getContainerSize(); x++)
		{
			this.addSlot(new CoinSlot(this.coinSlots, x, ItemTraderUtil.getInventoryDisplayOffset(this.getTrader()) + 8 + (x + 4) * 18, getCoinSlotHeight()));
		}
		
		//Item Slots
		for(int x = 0; x < itemSlots.getContainerSize(); x++)
		{
			this.addSlot(new Slot(this.itemSlots, x, ItemTraderUtil.getInventoryDisplayOffset(this.getTrader()) + 8 + x * 18, getCoinSlotHeight()));
		}
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, ItemTraderUtil.getInventoryDisplayOffset(this.getTrader()) + 8 + x * 18, getPlayerInventoryStartHeight() + y * 18));
			}
		}
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, ItemTraderUtil.getInventoryDisplayOffset(this.getTrader()) + 8 + x * 18, getPlayerInventoryStartHeight() + 58));
		}
		
	}
	
	public int getTradeCount()
	{
		return this.getTrader().getTradeCount();
	}
	
	protected int getTradeButtonBottom()
	{
		return ItemTraderUtil.getTradeDisplayHeight(this.getTrader());
	}
	
	protected int getCoinSlotHeight()
	{
		return getTradeButtonBottom() + 19;
	}
	
	protected int getPlayerInventoryStartHeight()
	{
		return getCoinSlotHeight() + 32;
	}
	
	@Override
	public boolean stillValid(Player playerIn) { return this.getTrader() != null; }
	
	@Override
	public void removed(Player playerIn)
	{
		//CurrencyMod.LOGGER.info("Closing a Trader Container");
		super.removed(playerIn);
		this.clearContainer(playerIn,  this.coinSlots);
		this.clearContainer(playerIn, this.itemSlots);
		
		if(this.getTrader() != null)
			this.getTrader().userClose(playerIn);
		
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
			if(index < this.coinSlots.getContainerSize() + this.itemSlots.getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack,  this.coinSlots.getContainerSize() + this.itemSlots.getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(index < this.slots.size())
			{
				if(MoneyUtil.isCoin(slotStack.getItem()))
				{
					//Merge coins into coin slots
					if(!this.moveItemStackTo(slotStack, 0, this.coinSlots.getContainerSize(), false))
					{
						return ItemStack.EMPTY;
					}
				}
				else
				{
					//Merge non-coins into item slots
					if(!this.moveItemStackTo(slotStack, this.coinSlots.getContainerSize(), this.coinSlots.getContainerSize() + this.itemSlots.getContainerSize(), false))
					{
						return ItemStack.EMPTY;
					}
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
	
	public long GetCoinValue()
	{
		long value = 0;
		for(int i = 0; i < coinSlots.getContainerSize(); i++)
		{
			value += MoneyUtil.getValue(coinSlots.getItem(i));
		}
		ItemStack wallet = LightmansCurrency.getWalletStack(this.player);
		if(!wallet.isEmpty())
		{
			value += MoneyUtil.getValue(WalletItem.getWalletInventory(wallet));
		}
		//CurrencyMod.LOGGER.info("Coin value of the open trader is " + value);
		return value;
	}
	
	public Container GetItemInventory() { return itemSlots; }
	
	public ItemTradeData GetTrade(int tradeIndex)
	{
		return this.getTrader().getTrade(tradeIndex);
	}
	
	public void ExecuteTrade(int trader, int tradeIndex)
	{
		
		//LightmansCurrency.LOGGER.info("Executing trade at index " + tradeIndex);
		if(this.getTrader() == null)
		{
			this.player.closeContainer();
			return;
		}
		
		this.getTrader().ExecuteTrade(TradeContext.create(this.getTrader(), this.player).withCoinSlots(this.coinSlots).build(), tradeIndex);
		
	}
	
	public void CollectCoinStorage()
	{
		if(this.getTrader() == null)
		{
			this.player.closeContainer();
			return;
		}
		if(!this.getTrader().hasPermission(this.player, Permissions.COLLECT_COINS))
		{
			Settings.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
			return;
		}
		if(this.getTrader().getCoreSettings().hasBankAccount())
			return;
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(this.getTrader().getInternalStoredMoney());
		ItemStack wallet = LightmansCurrency.getWalletStack(this.player);
		if(!wallet.isEmpty())
		{
			List<ItemStack> spareCoins = new ArrayList<>();
			for(int i = 0; i < coinList.size(); i++)
			{
				ItemStack extraCoins = WalletItem.PickupCoin(wallet, coinList.get(i));
				if(!extraCoins.isEmpty())
					spareCoins.add(extraCoins);
			}
			coinList = spareCoins;
		}
		for(int i = 0; i < coinList.size(); i++)
		{
			if(!InventoryUtil.PutItemStack(this.coinSlots, coinList.get(i)))
			{
				Container inventory = new SimpleContainer(1);
				inventory.setItem(0, coinList.get(i));
				this.clearContainer(player, inventory);
			}
		}
		//Clear the coin storage
		this.getTrader().clearStoredMoney();
		
	}
	
	public boolean hasPermission(String permission)
	{
		if(this.getTrader() != null)
			return this.getTrader().hasPermission(this.player, permission);
		return false;
	}
	
	public int getPermissionLevel(String permission)
	{
		if(this.getTrader() != null)
			return this.getTrader().getPermissionLevel(this.player, permission);
		return 0;
	}
	
	//Menu Combination Functions/Types
	public static class ItemTraderMenuUniversal extends ItemTraderMenu
	{
		public ItemTraderMenuUniversal(int windowID, Inventory inventory, UUID traderID) {
			super(/*ModMenus.ITEM_TRADER_UNIVERSAL*/ null, windowID, inventory, () ->{
				UniversalTraderData data = null;
				if(inventory.player.level.isClientSide)
					data = ClientTradingOffice.getData(traderID);
				else
					data = TradingOffice.getData(traderID);
				if(data instanceof IItemTrader)
					return (IItemTrader)data;
				return null;
			});
		}
		
		@Override
		public boolean isUniversal() { return true; }
		
	}
	
	public boolean isUniversal() { return false; }
	
	public static class ItemTraderMenuCR extends ItemTraderMenu
	{
		
		private CashRegisterBlockEntity cashRegister;
		
		public ItemTraderMenuCR(int windowID, Inventory inventory, BlockPos traderPos, CashRegisterBlockEntity cashRegister)
		{
			super(/*ModMenus.ITEM_TRADER_CR*/ null, windowID, inventory, traderPos);
			this.cashRegister = cashRegister;
		}
		
		@Override
		public boolean isCashRegister() { return true; }
		
		@Override
		public CashRegisterBlockEntity getCashRegister() { return this.cashRegister; }
		
		/*private TraderBlockEntity getTraderBE()
		{
			IItemTrader trader = super.getTrader();
			if(trader instanceof TraderBlockEntity)
				return (TraderBlockEntity) trader;
			return null;
		}
		
		@Override
		public int getThisCRIndex() { return this.cashRegister.getTraderIndex(this.getTraderBE()); }
		
		@Override
		public int getTotalCRSize() { return this.cashRegister.getPairedTraderSize(); }
		
		@Override
		public void OpenNextContainer(int direction) {
			int thisIndex = this.cashRegister.getTraderIndex((TraderBlockEntity)this.getTrader());
			this.cashRegister.OpenContainer(thisIndex, thisIndex + direction, direction, this.player);
		}
		
		@Override
		public void OpenContainerIndex(int index) {
			int previousIndex = index-1;
			if(previousIndex < 0)
				previousIndex = this.cashRegister.getPairedTraderSize() - 1;
			this.cashRegister.OpenContainer(previousIndex, index, 1, this.player);
		}*/
		
	}
	
	public boolean isCashRegister() { return false; }
	
	public CashRegisterBlockEntity getCashRegister() { return null; }

	public int getThisCRIndex() { return 0; }
	
	public int getTotalCRSize() { return 0; }
	
}
