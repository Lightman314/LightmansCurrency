package io.github.lightman314.lightmanscurrency.containers;

import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.IBankAccountTransferMenu;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ATMContainer extends Container implements IBankAccountTransferMenu{
	
	private PlayerEntity player;
	public PlayerEntity getPlayer() { return this.player; }
	
	private final IInventory coinInput = new Inventory(9);
	public IInventory getCoinInput() { return this.coinInput; }
	
	private AccountReference accountSource;
	public BankAccount getAccount() { if(this.accountSource == null) return null; return this.accountSource.get(); }
	
	private ITextComponent transferMessage = new StringTextComponent("");
	
	public ATMContainer(int windowId, PlayerInventory inventory)
	{
		super(ModContainers.ATM, windowId);
		
		this.player = inventory.player;
		
		//Auto-select the players bank account for now
		this.accountSource = BankAccount.GenerateReference(this.player);
		
		//Coinslots
		for(int x = 0; x < coinInput.getSizeInventory(); x++)
		{
			this.addSlot(new CoinSlot(this.coinInput, x, 8 + x * 18, 98, false));
		}
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 130 + y * 18));
			}
		}
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, 8 + x * 18, 188));
		}
	}
	
	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return true;
	}
	
	@Override
	public void onContainerClosed(PlayerEntity playerIn)
	{
		super.onContainerClosed(playerIn);
		this.clearContainer(playerIn, playerIn.world, this.coinInput);
	}

	
	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerEntity, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.inventorySlots.get(index);
		
		if(slot != null && slot.getHasStack())
		{
			ItemStack slotStack = slot.getStack();
			clickedStack = slotStack.copy();
			if(index < this.coinInput.getSizeInventory())
			{
				if(MoneyUtil.isCoin(slotStack.getItem()))
				{
					if(!this.mergeItemStack(slotStack,  this.coinInput.getSizeInventory(), this.inventorySlots.size(), true))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			else if(!this.mergeItemStack(slotStack, 0, this.coinInput.getSizeInventory(), false))
			{
				return ItemStack.EMPTY;
			}
			
			if(slotStack.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
		}
		
		return clickedStack;
		
	}
	
	public void SetAccount(AccountReference account)
	{
		this.accountSource = account;
	}
	
	@Override
	public AccountReference getAccountSource() {
		return this.accountSource;
	}
	
	//Button Input Codes:
	//100:Convert All Up
	//1:Copper -> Iron			-1:Iron -> Copper
	//2:Iron -> Gold			-2:Gold -> Iron
	//3:Gold -> Emerald			-3:Emerald -> Gold
	//4:Emerald -> Diamond		-4:Diamond -> Emerald
	//5:Diamond -> Netherite	-5: Netherite -> Diamond
	//-100: Convert all down
	public void ConvertCoins(int buttonInput)
	{
		///Converting Upwards
		//Converting All Upwards
		if(buttonInput == 100)
		{
			//Run two passes
			MoneyUtil.ConvertAllCoinsUp(this.coinInput);
		}
		//Copper to Iron
		else if(buttonInput == 1)
		{
			MoneyUtil.ConvertCoinsUp(this.coinInput, ModItems.COIN_COPPER);
		}
		//Iron to Gold
		else if(buttonInput == 2)
		{
			MoneyUtil.ConvertCoinsUp(this.coinInput, ModItems.COIN_IRON);
		}
		//Gold to Emerald
		else if(buttonInput == 3)
		{
			MoneyUtil.ConvertCoinsUp(this.coinInput, ModItems.COIN_GOLD);
		}
		//Emerald to Diamond
		else if(buttonInput == 4)
		{
			MoneyUtil.ConvertCoinsUp(this.coinInput, ModItems.COIN_EMERALD);
		}
		//Diamond to Netherite
		else if(buttonInput == 5)
		{
			MoneyUtil.ConvertCoinsUp(this.coinInput, ModItems.COIN_DIAMOND);
		}
		///Converting Downwards
		//Converting All Downwards
		else if(buttonInput == -100)
		{
			MoneyUtil.ConvertAllCoinsDown(this.coinInput);
		}
		//Netherite to Diamond
		else if(buttonInput == -5)
		{
			MoneyUtil.ConvertCoinsDown(this.coinInput, ModItems.COIN_NETHERITE);
		}
		//Netherite to Diamond
		if(buttonInput == -4)
		{
			MoneyUtil.ConvertCoinsDown(this.coinInput, ModItems.COIN_DIAMOND);
		}
		//Netherite to Diamond
		if(buttonInput == -3)
		{
			MoneyUtil.ConvertCoinsDown(this.coinInput, ModItems.COIN_EMERALD);
		}
		//Netherite to Diamond
		if(buttonInput == -2)
		{
			MoneyUtil.ConvertCoinsDown(this.coinInput, ModItems.COIN_GOLD);
		}
		//Netherite to Diamond
		if(buttonInput == -1)
		{
			MoneyUtil.ConvertCoinsDown(this.coinInput, ModItems.COIN_IRON);
		}
		
	}

	@Override
	public ITextComponent getLastMessage() { return this.transferMessage; }

	@Override
	public void setMessage(ITextComponent component) { this.transferMessage = component; }
	
}
