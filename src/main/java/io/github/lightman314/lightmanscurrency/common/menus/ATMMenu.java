package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.common.core.ModMenus;

import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountType;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.money.ATMUtil;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ATMMenu extends LazyMessageMenu implements IBankAccountAdvancedMenu {
	
	private final Player player;
	public Player getPlayer() { return this.player; }
	
	private final Container coinInput = new SimpleContainer(9);
	public Container getCoinInput() { return this.coinInput; }
	
	private MutableComponent transferMessage = null;
	
	public ATMMenu(int windowId, Inventory inventory)
	{
		super(ModMenus.ATM.get(), windowId, inventory);
		
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
	public boolean stillValid(@Nonnull Player player) {
		//Run get bank account code during valid check so that it auto-validates the account access and updates the client as necessary.
		this.getBankAccountReference();
		return true;
	}
	
	@Override
	public void removed(@Nonnull Player player)
	{
		super.removed(player);
		this.clearContainer(player,  this.coinInput);
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
	
	@Nonnull
	@Override
	public ItemStack quickMoveStack(@Nonnull Player player, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.slots.get(index);
		
		if(slot != null && slot.hasItem())
		{
			ItemStack slotStack = slot.getItem();
			clickedStack = slotStack.copy();
			//Move items from the coin slots into the inventory
			if(index < this.coinInput.getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack,  this.coinInput.getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			//Move items from the inventory into the coin slots
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

	public void SendCoinExchangeMessage(String command) {
		this.SendMessageToServer(LazyPacketData.builder().setString("ExchangeCoinCommand", command));
	}

	public void ExchangeCoins(String command)
	{
		ATMUtil.ExecuteATMExchangeCommand(this.coinInput, command);
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

	public void SetNotificationValueAndUpdate(CoinValue newValue)
	{
		BankAccount ba = this.getBankAccount();
		if(ba != null)
			ba.setNotificationValue(newValue);
		this.SendMessageToServer(LazyPacketData.builder().setCoinValue("NotificationValueChange", newValue));
	}

	@Override
	public void HandleMessage(LazyPacketData message) {
		if(message.contains("ExchangeCoinCommand"))
			this.ExchangeCoins(message.getString("ExchangeCoinCommand"));
		if(message.contains("NotificationValueChange"))
		{
			BankAccount ba = this.getBankAccount();
			if(ba != null)
				ba.setNotificationValue(message.getCoinValue("NotificationValueChange"));
		}
	}



}
