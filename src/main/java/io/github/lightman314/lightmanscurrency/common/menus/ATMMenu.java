package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.common.core.ModMenus;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountType;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ATMMenu extends AbstractContainerMenu implements IBankAccountAdvancedMenu{
	
	private Player player;
	public Player getPlayer() { return this.player; }
	
	private final Container coinInput = new SimpleContainer(9);
	public Container getCoinInput() { return this.coinInput; }
	
	private MutableComponent transferMessage = null;
	
	public ATMMenu(int windowId, Inventory inventory)
	{
		super(ModMenus.ATM.get(), windowId);
		
		this.player = inventory.player;
		
		//Coinslots
		for(int x = 0; x < coinInput.getContainerSize(); x++)
		{
			this.addSlot(new CoinSlot(this.coinInput, x, 8 + x * 18, 129, false));
		}
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 161 + y * 18));
			}
		}
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, 8 + x * 18, 219));
		}
	}
	
	@Override
	public boolean stillValid(Player playerIn) {
		//Run get bank account code during valid check so that it auto-validates the account access and updates the client as necessary.
		this.getBankAccountReference();
		return true;
	}
	
	@Override
	public void removed(Player playerIn)
	{
		super.removed(playerIn);
		this.clearContainer(playerIn,  this.coinInput);
		if(!this.isClient())
		{
			AccountReference account = this.getBankAccountReference();
			if(account.accountType == AccountType.Player)
			{
				if(!account.playerID.equals(this.player.getUUID()))
				{
					//Switch back to their personal bank account when closing the ATM if they're accessing another players bank account.
					BankSaveData.SetSelectedBankAccount(this.player, BankAccount.GenerateReference(this.player));
				}
			}
		}
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
			if(index < this.coinInput.getContainerSize())
			{
				if(MoneyUtil.isCoin(slotStack.getItem()))
				{
					if(!this.moveItemStackTo(slotStack,  this.coinInput.getContainerSize(), this.slots.size(), true))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			else if(!this.moveItemStackTo(slotStack, 0, this.coinInput.getContainerSize(), false))
			{
				return ItemStack.EMPTY;
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
	
	public void ConvertCoins(String command)
	{
		///Converting Upwards
		//Converting All Upwards
		if(command.contentEquals("convertAllUp"))
		{
			MoneyUtil.ConvertAllCoinsUp(this.coinInput);
		}
		//Convert defined coin upwards
		else if(command.startsWith("convertUp-"))
		{
			ResourceLocation coinID = null;
			String id = "";
			try {
				id = command.substring("convertUp-".length());
				coinID = new ResourceLocation(id);
				Item coinItem = ForgeRegistries.ITEMS.getValue(coinID);
				if(coinItem == null)
				{
					LightmansCurrency.LogError("Error handling ATM Conversion command '" + command + "'.\n'" + coinID.toString() + "' is not a registered item.");
					return;
				}
				if(!MoneyUtil.isCoin(coinItem))
				{
					LightmansCurrency.LogError("Error handling ATM Conversion command '" + command + "'.\n'" + coinID.toString() + "' is not a coin.");
					return;
				}
				if(MoneyUtil.getUpwardConversion(coinItem) == null)
				{
					LightmansCurrency.LogError("Error handling ATM Conversion command '" + command + "'.\n'" + coinID.toString() + "' is the largest visible coin in its chain, and thus cannot be converted any larger.");
					return;
				}
				MoneyUtil.ConvertCoinsUp(this.coinInput, coinItem);
			} catch(Exception e) { LightmansCurrency.LogError("Error handling ATM Conversion command '" + command + "'.\n'" + id + "' could not be parsed as an item id.", e);}
		}
		else if(command.contentEquals("convertAllDown"))
		{
			MoneyUtil.ConvertAllCoinsDown(this.coinInput);
		}
		else if(command.startsWith("convertDown-"))
		{
			String id = "";
			try {
				id = command.substring("convertDown-".length());
				ResourceLocation coinID = new ResourceLocation(id);
				Item coinItem = ForgeRegistries.ITEMS.getValue(coinID);
				if(coinItem == null)
				{
					LightmansCurrency.LogError("Error handling ATM Conversion command '" + command + "'.\n'" + coinID.toString() + "' is not a registered item.");
					return;
				}
				if(!MoneyUtil.isCoin(coinItem))
				{
					LightmansCurrency.LogError("Error handling ATM Conversion command '" + command + "'.\n'" + coinID.toString() + "' is not a coin.");
					return;
				}
				if(MoneyUtil.getDownwardConversion(coinItem) == null)
				{
					LightmansCurrency.LogError("Error handling ATM Conversion command '" + command + "'.\n'" + coinID.toString() + "' is the smallest known coin, and thus cannot be converted any smaller.");
					return;
				}
				MoneyUtil.ConvertCoinsDown(this.coinInput, coinItem);
			} catch(Exception e) { LightmansCurrency.LogError("Error handling ATM Conversion command '" + command + "'.\n'" + id + "' could not be parsed as an item id.", e);}
		}
		else
			LightmansCurrency.LogError("'" + command + "' is not a valid ATM Conversion command.");
		
	}

	
	public MutableComponent SetPlayerAccount(String playerName) {
		
		if(CommandLCAdmin.isAdminPlayer(this.player))
		{
			PlayerReference accountPlayer = PlayerReference.of(false, playerName);
			if(accountPlayer != null)
			{
				BankSaveData.SetSelectedBankAccount(this.player, BankAccount.GenerateReference(false, accountPlayer));
				return Component.translatable("gui.bank.select.player.success", accountPlayer.getName(false));
			}
			else
				return Component.translatable("gui.bank.transfer.error.null.to");
		}
		return Component.literal("ERROR");
		
	}
	
	public boolean hasTransferMessage() { return this.transferMessage != null; }
	
	public MutableComponent getTransferMessage() { return this.transferMessage; }
	
	@Override
	public void setTransferMessage(MutableComponent message) { this.transferMessage = message; }
	
	public void clearMessage() { this.transferMessage = null; }

	@Override
	public boolean isClient() { return this.player.level.isClientSide; }
	
}
