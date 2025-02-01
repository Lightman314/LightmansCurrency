package io.github.lightman314.lightmanscurrency.common.menus;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;

import io.github.lightman314.lightmanscurrency.common.data.types.BankDataCache;
import io.github.lightman314.lightmanscurrency.common.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.ATMAPI;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ATMMenu extends LazyMessageMenu implements IBankAccountAdvancedMenu {

	public Player getPlayer() { return this.player; }
	
	private final SimpleContainer coinInput = new SimpleContainer(9);
	private final IMoneyHandler moneyHandler = MoneyAPI.API.GetATMMoneyHandler(this.player, this.coinInput);
	public SimpleContainer getCoinInput() { return this.coinInput; }
	public IMoneyHandler getMoneyHandler() { return this.moneyHandler; }
	private final List<CoinSlot> coinSlots;
	public List<CoinSlot> getCoinSlots() { return this.coinSlots; }
	
	private MutableComponent transferMessage = null;
	
	public ATMMenu(int windowId, Inventory inventory, MenuValidator validator)
	{
		super(ModMenus.ATM.get(), windowId, inventory, validator);

		this.addValidator(p -> !QuarantineAPI.IsDimensionQuarantined(p));

		List<CoinSlot> temp = new ArrayList<>();
		//Coinslots
		for(int x = 0; x < coinInput.getContainerSize(); x++)
		{
			CoinSlot slot = new CoinSlot(this.coinInput, x, 8 + x * 18, 129, false);
			this.addSlot(slot);
			temp.add(slot);
		}
		this.coinSlots = ImmutableList.copyOf(temp);
		
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
	protected void onValidationTick(@Nonnull Player player) {
		//Run get bank account code during valid check so that it auto-validates the account access and updates the client as necessary.
		this.getBankAccountReference();
	}

	@Override
	public void removed(@Nonnull Player player)
	{
		super.removed(player);
		this.clearContainer(player,  this.coinInput);
		if(!this.isClient())
		{
			BankReference account = this.getBankAccountReference();
			if(!account.canPersist(player))
			{
				//Switch back to their personal bank account when closing the ATM if they're accessing another players bank account.
				BankDataCache data = BankDataCache.TYPE.get(this);
				if(data != null)
					data.setSelectedAccount(this.player,PlayerBankReference.of(this.player));
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
		ATMAPI.ExecuteATMExchangeCommand(this.coinInput, command);
	}

	
	public MutableComponent SetPlayerAccount(String playerName) {
		
		if(LCAdminMode.isAdminPlayer(this.player))
		{
			PlayerReference accountPlayer = PlayerReference.of(false, playerName);
			if(accountPlayer != null)
			{
				BankDataCache data = BankDataCache.TYPE.get(this);
				if(data != null)
					data.setSelectedAccount(this.player,PlayerBankReference.of(accountPlayer));
				return LCText.GUI_BANK_SELECT_PLAYER_SUCCESS.get(accountPlayer.getName(false));
			}
			else
				return LCText.GUI_BANK_TRANSFER_ERROR_NULL_TARGET.get();
		}
		return EasyText.literal("ERROR");
		
	}
	
	public boolean hasTransferMessage() { return this.transferMessage != null; }
	
	public MutableComponent getTransferMessage() { return this.transferMessage; }
	
	@Override
	public void setTransferMessage(MutableComponent message) { this.transferMessage = message; }
	
	public void clearMessage() { this.transferMessage = null; }

	public void SetNotificationValueAndUpdate(@Nonnull String type, @Nonnull MoneyValue newValue)
	{
		IBankAccount ba = this.getBankAccount();
		if(ba != null)
			ba.setNotificationLevel(type, newValue);
		this.SendMessageToServer(LazyPacketData.builder().setString("NotificationValueType", type).setMoneyValue("NotificationValueChange", newValue));
	}

	@Override
	public void HandleMessage(@Nonnull LazyPacketData message) {
		if(message.contains("ExchangeCoinCommand"))
			this.ExchangeCoins(message.getString("ExchangeCoinCommand"));
		if(message.contains("NotificationValueChange"))
		{
			IBankAccount ba = this.getBankAccount();
			if(ba != null)
				ba.setNotificationLevel(message.getString("NotificationValueType"), message.getMoneyValue("NotificationValueChange"));
		}
		if(message.contains("ResetCards"))
		{
			IBankAccount ba = this.getBankAccount();
			if(ba != null)
				ba.resetCards();
		}
	}

}
