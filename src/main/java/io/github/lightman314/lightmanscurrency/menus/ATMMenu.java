package io.github.lightman314.lightmanscurrency.menus;

import io.github.lightman314.lightmanscurrency.core.ModMenus;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountType;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
	
	private AccountReference accountSource = null;
	public BankAccount getAccount() { if(this.accountSource == null) return null; return this.accountSource.get(); }
	
	private MutableComponent transferMessage = null;
	
	public ATMMenu(int windowId, Inventory inventory)
	{
		super(ModMenus.ATM, windowId);
		
		this.player = inventory.player;
		//Auto-select the players bank account for now.
		this.accountSource = BankAccount.GenerateReference(this.player.level.isClientSide, AccountType.Player, this.player.getUUID());
		
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
	public boolean stillValid(Player playerIn) { return true; }
	
	@Override
	public void removed(Player playerIn)
	{
		super.removed(playerIn);
		this.clearContainer(playerIn,  this.coinInput);
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
	
	public void SetAccount(AccountReference account)
	{
		this.accountSource = account;
	}
	
	public Pair<AccountReference,MutableComponent> SetPlayerAccount(String playerName) {
		
		if(TradingOffice.isAdminPlayer(this.player))
		{
			PlayerReference accountPlayer = PlayerReference.of(playerName);
			if(accountPlayer != null)
			{
				this.accountSource = BankAccount.GenerateReference(false, accountPlayer);
				return Pair.of(this.accountSource, new TranslatableComponent("gui.bank.select.player.success", accountPlayer.lastKnownName()));
			}
			else
				return Pair.of(null, new TranslatableComponent("gui.bank.transfer.error.null.to"));
		}
		return Pair.of(null, new TextComponent("ERROR"));
	}

	@Override
	public AccountReference getAccountSource() {
		return this.accountSource;
	}
	
	public boolean hasTransferMessage() { return this.transferMessage != null; }
	
	public MutableComponent getTransferMessage() { return this.transferMessage; }
	
	@Override
	public void setTransferMessage(MutableComponent message) { this.transferMessage = message; }
	
	public void clearMessage() { this.transferMessage = null; }
	
	@Override
	public void setNotificationLevel(CoinValue amount) {
		if(this.getAccount() != null)
		{
			this.getAccount().setNotificationValue(amount);
		}
	}
	
}
