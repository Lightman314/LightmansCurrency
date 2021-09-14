package io.github.lightman314.lightmanscurrency.containers;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces.ITradeButtonContainer;
import io.github.lightman314.lightmanscurrency.common.ItemTraderUtil;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderContainer;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.containers.slots.DisplaySlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.WalletUtil;
import io.github.lightman314.lightmanscurrency.util.WalletUtil.PlayerWallets;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.ItemTradeData.TradeDirection;
import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.ItemTradeData;

public class ItemTraderContainer extends AbstractContainerMenu implements ITraderContainer, ITradeButtonContainer{
	
	public final Player player;
	
	//protected static final MenuType<?> type = ModContainers.ITEMTRADER;
	
	protected final Container coinSlots = new SimpleContainer(5);
	protected final Container itemSlots = new SimpleContainer(3);
	protected final Container tradeDisplays;
	public final ItemTraderBlockEntity blockEntity;
	
	public ItemTraderContainer(int windowId, Inventory inventory, ItemTraderBlockEntity blockEntity)
	{
		this(ModContainers.ITEMTRADER, windowId, inventory, blockEntity);
	}
	
	protected ItemTraderContainer(MenuType<?> type, int windowId, Inventory inventory, ItemTraderBlockEntity blockEntity)
	{
		super(type, windowId);
		this.blockEntity = blockEntity;
		
		this.player = inventory.player;
		
		this.blockEntity.userOpen(this.player);
		
		int tradeCount = this.getTradeCount();
		
		//Coin Slots
		for(int x = 0; x < coinSlots.getContainerSize(); x++)
		{
			this.addSlot(new CoinSlot(this.coinSlots, x, ItemTraderUtil.getInventoryDisplayOffset(tradeCount) + 8 + (x + 4) * 18, getCoinSlotHeight()));
		}
		
		//Item Slots
		for(int x = 0; x < itemSlots.getContainerSize(); x++)
		{
			this.addSlot(new Slot(this.itemSlots, x, ItemTraderUtil.getInventoryDisplayOffset(tradeCount) + 8 + x * 18, getCoinSlotHeight()));
		}
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, ItemTraderUtil.getInventoryDisplayOffset(tradeCount) + 8 + x * 18, getPlayerInventoryStartHeight() + y * 18));
			}
		}
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, ItemTraderUtil.getInventoryDisplayOffset(tradeCount) + 8 + x * 18, getPlayerInventoryStartHeight() + 58));
		}
		
		tradeDisplays = new SimpleContainer(blockEntity.getTradeCount());
		UpdateTradeDisplays();
		//Trade displays
		for(int i = 0; i < tradeCount; i++)
		{
			this.addSlot(new DisplaySlot(tradeDisplays, i, ItemTraderUtil.getSlotPosX(tradeCount, i), ItemTraderUtil.getSlotPosY(tradeCount, i)));
		}
		
	}
	
	public int getTradeCount()
	{
		return blockEntity.getTradeCount();
	}
	
	protected int getTradeButtonBottom()
	{
		return ItemTraderUtil.getTradeDisplayHeight(this.getTradeCount());
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
	public boolean stillValid(Player playerIn)
	{
		//return this.callable.applyOrElse((world,pos) -> playerIn.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0, true);
		return !this.blockEntity.isRemoved();
	}
	
	@Override
	public void removed(Player playerIn)
	{
		//CurrencyMod.LOGGER.info("Closing a Trader Container");
		super.removed(playerIn);
		this.clearContainer(playerIn,  this.coinSlots);
		this.clearContainer(playerIn, this.itemSlots);
		
		this.blockEntity.userClose(this.player);
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
				if(!this.moveItemStackTo(slotStack,  this.coinSlots.getContainerSize() + this.itemSlots.getContainerSize(), this.slots.size() - tradeDisplays.getContainerSize(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(index < this.slots.size() - tradeDisplays.getContainerSize())
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
		value += WalletUtil.getWallets(this.player).getStoredMoney();
		//CurrencyMod.LOGGER.info("Coin value of the open trader is " + value);
		return value;
	}
	
	public Container GetItemInventory() { return itemSlots; }
	
	public void ExecuteTrade(int tradeIndex)
	{
		//LightmansCurrency.LOGGER.info("Executing trade at index " + tradeIndex);
		if(this.blockEntity.isRemoved())
		{
			this.player.closeContainer();
			return;
		}
		ItemTradeData trade = blockEntity.getTrade(tradeIndex);
		//Abort if the trade is null
		if(trade == null)
		{
			LightmansCurrency.LogError("Trade at index " + tradeIndex + " is null. Cannot execute trade!");
			return;
		}
		
		//Abort if the trade is not valid
		if(!trade.isValid())
		{
			LightmansCurrency.LogWarning("Trade at index " + tradeIndex + " is not a valid trade. Cannot execute trade.");
			return;
		}
		//Process a sale
		if(trade.getTradeDirection() == TradeDirection.SALE)
		{
			//Abort if not enough items in inventory
			if(InventoryUtil.GetItemCount(this.blockEntity.getStorage(), trade.getSellItem()) < trade.getSellItem().getCount() && !this.blockEntity.isCreative())
			{
				LightmansCurrency.LogDebug("Not enough items in storage to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
				return;
			}
			
			//Abort if not enough room to put the sold item
			if(!InventoryUtil.CanPutItemStack(this.itemSlots, trade.getSellItem()))
			{
				LightmansCurrency.LogInfo("Not enough room for the output item. Aborting trade!");
				return;
			}
			
			if(!MoneyUtil.ProcessPayment(this.coinSlots, this.player, trade.getCost()))
			{
				LightmansCurrency.LogDebug("Not enough money is present for the trade at index " + tradeIndex + ". Cannot execute trade.");
				return;
			}
			
			//We have enough money, and the trade is valid. Execute the trade
			//Get the trade itemStack
			ItemStack giveStack = trade.getSellItem();
			//Give the trade item
			if(!InventoryUtil.PutItemStack(this.itemSlots, giveStack))//If there's not enough room to give the item to the output item, abort the trade
			{
				LightmansCurrency.LogError("Not enough room for the output item. Giving refund & aborting Trade!");
				//Give a refund
				List<ItemStack> refundCoins = MoneyUtil.getCoinsOfValue(trade.getCost());
				PlayerWallets wallet = WalletUtil.getWallets(this.player);
				
				for(int i = 0; i < refundCoins.size(); i++)
				{
					ItemStack coins = refundCoins.get(i);
					if(wallet.hasWallet())
					{
						coins = wallet.PlaceCoin(coins);
					}
					if(!coins.isEmpty())
					{
						coins = InventoryUtil.TryPutItemStack(this.coinSlots, coins);
						if(!coins.isEmpty())
						{
							Container temp = new SimpleContainer(1);
							temp.setItem(0, coins);
							this.clearContainer(this.player, temp);
						}
					}
				}
				return;
			}
			
			//Add the log data
			this.blockEntity.logger.AddLog(player, trade, this.blockEntity.isCreative());
			this.blockEntity.markLoggerDirty();
			
			//Ignore editing internal storage if this is flagged as creative.
			if(!this.blockEntity.isCreative())
			{
				//Remove the sold items from storage
				InventoryUtil.RemoveItemCount(this.blockEntity.getStorage(), trade.getSellItem());
				//Give the payed cost to storage
				blockEntity.addStoredMoney(trade.getCost());
			}
		}
		//Process a purchase
		else if(trade.getTradeDirection() == TradeDirection.PURCHASE)
		{
			//Abort if not enough items in the item slots
			if(InventoryUtil.GetItemCount(this.itemSlots, trade.getSellItem()) < trade.getSellItem().getCount())
			{
				LightmansCurrency.LogDebug("Not enough items in the item slots to make the purchase.");
				return;
			}
			//Abort if not enough room to store the purchased items (unless we're creative)
			if(!InventoryUtil.CanPutItemStack(this.blockEntity.getStorage(), trade.getSellItem()) && !this.blockEntity.isCreative())
			{
				LightmansCurrency.LogDebug("Not enough room in storage to store the purchased items.");
			}
			//Abort if not enough money to pay them back
			if(!trade.hasEnoughMoney(this.blockEntity.getStoredMoney()) && !this.blockEntity.isCreative())
			{
				LightmansCurrency.LogDebug("Not enough money in storage to pay for the purchased items.");
				return;
			}
			//Passed the checks. Take the item(s) from the input slot
			InventoryUtil.RemoveItemCount(this.itemSlots, trade.getSellItem());
			//Put the payment in the purchasers wallet, coin slot, etc.
			MoneyUtil.ProcessChange(this.coinSlots, this.player, trade.getCost());
			
			//Add the log data
			this.blockEntity.logger.AddLog(player, trade, this.blockEntity.isCreative());
			this.blockEntity.markLoggerDirty();
			
			//Ignore editing internal storage if this is flagged as creative.
			if(!this.blockEntity.isCreative())
			{
				//Put the item in storage
				InventoryUtil.TryPutItemStack(this.blockEntity.getStorage(), trade.getSellItem());
				//Remove the coins from storage
				this.blockEntity.removeStoredMoney(trade.getCost());
			}
			
		}
		
		
		
		
	}
	
	public void CollectCoinStorage()
	{
		if(this.blockEntity.isRemoved())
		{
			this.player.closeContainer();
			return;
		}
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(blockEntity.getStoredMoney());
		PlayerWallets wallet = WalletUtil.getWallets(this.player);
		if(wallet.hasWallet())
		{
			List<ItemStack> spareCoins = new ArrayList<>();
			for(int i = 0; i < coinList.size(); i++)
			{
				ItemStack extraCoins = wallet.PlaceCoin(coinList.get(i));
				if(!extraCoins.isEmpty())
					spareCoins.add(extraCoins);
			}
			coinList = spareCoins;
			//if(!LightmansCurrency.isCuriosLoaded())
			//	LightmansCurrencyPacketHandler.instance.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)this.player), new MessageUpdateWallet(player.getId(), wallet));
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
		blockEntity.clearStoredMoney();
		
	}
	
	public void tick()
	{
		if(this.blockEntity.isRemoved())
		{
			this.player.closeContainer();
			return;
		}
		UpdateTradeDisplays();
	}
	
	public void UpdateTradeDisplays()
	{
		for(int i = 0; i < tradeDisplays.getContainerSize(); i++)
		{
			ItemTradeData trade = blockEntity.getTrade(i);
			if(trade != null)
				tradeDisplays.setItem(i, trade.getDisplayItem());
			else
				tradeDisplays.setItem(i, ItemStack.EMPTY);
		}
	}
	
	public boolean isOwner()
	{
		return blockEntity.isOwner(player);
	}
	
}
