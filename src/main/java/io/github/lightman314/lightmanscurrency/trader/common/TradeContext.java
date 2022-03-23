package io.github.lightman314.lightmanscurrency.trader.common;

import java.util.List;

import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.menus.slots.InteractionSlot;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TradeContext {
	
	public enum RemoteTradeResult {
		/**
		 * Remote trade was successfully executed
		 */
		SUCCESS(null),
		/**
		 * Trade failed as the trader is out of stock
		 */
		FAIL_OUT_OF_STOCK("lightmanscurrency.remotetrade.fail.nostock"),
		
		/**
		 * Trade failed as the player could not afford the trade
		 */
		FAIL_CANNOT_AFFORD("lightmanscurrency.remotetrade.fail.cantafford"),
		/**
		 * Trade failed as there's no room for the output items
		 */
		FAIL_NO_OUTPUT_SPACE("lightmanscurrency.remotetrade.fail.nospace.output"),
		/**
		 * Trade failed as there's no room for the input items
		 */
		FAIL_NO_INPUT_SPACE("lightmanscurrency.remotetrade.fail.nospace.input"),
		/**
		 * Trade failed as the trade rules denied the trade
		 */
		FAIL_TRADE_RULE_DENIAL("lightmanscurrency.remotetrade.fail.traderule"),
		/**
		 * Trade failed as the trade is no longer valid
		 */
		FAIL_INVALID_TRADE("lightmanscurrency.remotetrade.fail.invalid"),
		/**
		 * Trade failed as this trader does not support remote trades
		 */
		FAIL_NOT_SUPPORTED("lightmanscurrency.remotetrade.fail.notsupported"),
		/**
		 * Trade failed as the trader was null
		 */
		FAIL_NULL("lightmanscurrency.remotetrade.fail.null");
		public boolean hasMessage() { return this.failMessage != null; }
		public final Component failMessage;
		RemoteTradeResult(String message) { this.failMessage = message == null ? null : new TranslatableComponent(message); }
	}
	
	public final boolean isStorageMode;
	
	//Player Data
	private final Player player;
	public boolean hasPlayer() { return this.player != null; }
	public Player getPlayer() { return this.player; }
	
	private final PlayerReference playerSource;
	public final PlayerReference getPlayerSource() { if(this.hasPlayer()) return PlayerReference.of(player); return this.playerSource; }
	
	//Money/Payment related data
	private final AccountReference bankAccount;
	public boolean hasBankAccount() { return this.bankAccount != null && this.bankAccount.get() != null; }
	public BankAccount getBankAccount() { return this.bankAccount.get(); }
	
	private final Container coinSlots;
	public boolean hasCoinSlots() { return this.coinSlots != null; }
	public final Container getCoinSlots() { return this.coinSlots; }
	
	private final CoinValue storedMoney;
	public boolean hasStoredMoney() { return this.storedMoney != null; }
	public CoinValue getStoredMoney() { return this.storedMoney; }
	
	//Interaction Slots (bucket/battery slot, etc.)
	private final List<InteractionSlot> interactionSlots;
	public boolean hasInteractionSlot(String type) { return this.getInteractionSlot(type) != null; }
	public InteractionSlot getInteractionSlot(String type) { if(this.interactionSlots == null) return null; return InteractionSlot.getSlot(this.interactionSlots, type); }
	
	//Item related data
	private final IItemHandler itemHandler;
	public boolean hasItemHandler() { return this.itemHandler != null; }
	public IItemHandler getItemHandler() { return this.itemHandler; }
	
	//Fluid related data
	private final IFluidHandler fluidTank;
	public boolean hasFluidTank() { return this.fluidTank != null; }
	public IFluidHandler getFluidTank() { return this.fluidTank; }
	
	//Energy related data
	private final IEnergyStorage energyTank;
	public boolean hasEnergyTank() { return this.energyTank != null; }
	public IEnergyStorage getEnergyTank() { return this.energyTank; }
	
	private TradeContext(Builder builder) {
		this.isStorageMode = builder.storageMode;
		this.player = builder.player;
		this.playerSource = builder.playerReference;
		this.bankAccount = builder.bankAccount;
		this.coinSlots = builder.coinSlots;
		this.storedMoney = builder.storedCoins;
		this.interactionSlots = builder.interactionSlots;
		this.itemHandler = builder.itemHandler;
		this.fluidTank = builder.fluidHandler;
		this.energyTank = builder.energyHandler;
	}
	
	public boolean hasPaymentMethod() { return this.hasPlayer() || this.hasCoinSlots() || this.hasBankAccount() || this.hasStoredMoney(); }
	
	public boolean hasFunds(CoinValue price)
	{
		long funds = 0;
		if(this.hasBankAccount())
			funds += this.getBankAccount().getCoinStorage().getRawValue();
		if(this.hasStoredMoney())
			funds += this.getStoredMoney().getRawValue();
		return funds > price.getRawValue();
	}
	
	public boolean getPayment(CoinValue price)
	{
		
		if(this.hasFunds(price))
		{
			long amountToWithdraw = price.getRawValue();
			if(this.hasStoredMoney())
			{
				CoinValue storedMoney = this.getStoredMoney();
				long removeAmount = Math.min(amountToWithdraw, storedMoney.getRawValue());
				amountToWithdraw -= removeAmount;
				storedMoney.readFromOldValue(storedMoney.getRawValue() - removeAmount);
			}
			if(this.hasBankAccount() && amountToWithdraw > 0)
			{
				this.getBankAccount().withdrawCoins(new CoinValue(amountToWithdraw));
			}
			return true;
		}
		return false;
	}
	
	public boolean givePayment(CoinValue price)
	{
		if(this.hasBankAccount())
		{
			this.getBankAccount().depositCoins(price);
			return true;
		}
		else if(this.hasStoredMoney())
		{
			CoinValue storedMoney = this.getStoredMoney();
			storedMoney.addValue(price);
		}
		return false;
	}
	
	/**
	 * Whether the given item stack is present in the item handler, and can be successfully removed without issue.
	 */
	public boolean hasItem(ItemStack item)
	{
		if(this.hasItemHandler())
			return InventoryUtil.CanExtractItem(this.getItemHandler(), item);
		return false;
	}
	
	/**
	 * Removes the given item stack from the item handler.
	 * @return Whether the extraction was successfully. Will return false if it could not be extracted correctly.
	 */
	public boolean collectItem(ItemStack item)
	{
		if(this.hasItem(item) && this.hasItemHandler())
		{
			InventoryUtil.RemoveItemCount(this.getItemHandler(), item);
			return true;
		}
		return false;
	}
	
	public boolean canFitItem(ItemStack item)
	{
		if(this.hasItemHandler())
			return ItemHandlerHelper.insertItemStacked(this.getItemHandler(), item, true).isEmpty();
		return false;
	}
	
	public boolean putItem(ItemStack item)
	{
		if(this.canFitItem(item) && this.hasItemHandler())
		{
			ItemStack leftovers = ItemHandlerHelper.insertItemStacked(this.getItemHandler(), item, false);
			if(leftovers.isEmpty())
				return true;
			else
			{
				//Failed to place the items in the item handler, so take what few were placed back.
				ItemStack placedStack = item.copy();
				placedStack.setCount(item.getCount() - leftovers.getCount());
				if(!item.isEmpty())
					this.collectItem(placedStack);
				return false;
			}	
		}
		return false;
	}
	
	public boolean hasFluid(FluidStack fluid)
	{
		if(this.hasFluidTank())
		{
			FluidStack result = this.getFluidTank().drain(fluid, FluidAction.SIMULATE);
			if(result.isEmpty() || result.getAmount() < fluid.getAmount())
				return false;
			return true;
		}
		return false;
	}
	
	public FluidStack drainFluid(FluidStack fluid)
	{
		if(this.hasFluid(fluid) && this.hasFluidTank())
			return this.getFluidTank().drain(fluid, FluidAction.EXECUTE);
		return FluidStack.EMPTY;
	}
	
	public boolean canFitFluid(FluidStack fluid)
	{
		if(this.hasFluidTank())
			return this.getFluidTank().fill(fluid, FluidAction.SIMULATE) == fluid.getAmount();
		return false;
	}
	
	public boolean fillFluid(FluidStack fluid)
	{
		if(this.canFitFluid(fluid) && this.hasFluidTank())
		{
			this.getFluidTank().fill(fluid, FluidAction.EXECUTE);
			return true;
		}
		return false;
	}
	
	public boolean hasEnergy(int amount)
	{
		if(this.hasEnergyTank())
			return this.getEnergyTank().extractEnergy(amount, true) == amount;
		return false;
	}
	
	public boolean drainEnergy(int amount)
	{
		if(this.hasEnergy(amount) && this.hasEnergyTank())
		{
			this.getEnergyTank().extractEnergy(amount, false);
			return true;
		}
		return false;
	}
	
	public boolean canFitEnergy(int amount)
	{
		if(this.hasEnergyTank())
			return this.getEnergyTank().receiveEnergy(amount, true) == amount;
		return false;
	}
	
	public boolean fillEnergy(int amount)
	{
		if(this.canFitEnergy(amount) && this.hasEnergyTank())
		{
			this.getEnergyTank().receiveEnergy(amount, true);
			return true;
		}
		return false;
	}
	
	public static final TradeContext STORAGE_MODE = new Builder().build();
	public static Builder create(Player player) { return new Builder(player); }
	public static Builder create(PlayerReference player) { return new Builder(player); }
	
	public static class Builder
	{
		
		//Core
		private final boolean storageMode;
		private final Player player;
		private final PlayerReference playerReference;
		
		//Money
		private AccountReference bankAccount;
		private Container coinSlots;
		private CoinValue storedCoins;
		
		//Interaction Slots
		private List<InteractionSlot> interactionSlots;
		
		//Item
		private IItemHandler itemHandler;
		//Fluid
		private IFluidHandler fluidHandler;
		//Energy
		private IEnergyStorage energyHandler;
		
		private Builder() { this.storageMode = true; this.player = null; this.playerReference = null; }
		private Builder(Player player) { this.player = player; this.playerReference = null; this.storageMode = false; }
		private Builder(PlayerReference player) { this.playerReference = player; this.player = null; this.storageMode = false; }
		
		public Builder withBankAccount(AccountReference bankAccount) { this.bankAccount = bankAccount; return this; }
		public Builder withCoinSlots(Container coinSlots) { this.coinSlots = coinSlots; return this; }
		public Builder withStoredCoins(CoinValue storedCoins) { this.storedCoins = storedCoins; return this; }
		
		public Builder withInteractionSlots(List<InteractionSlot> interactionSlots) { this.interactionSlots = interactionSlots; return this; }
		
		public Builder withItemHandler(IItemHandler itemHandler) { this.itemHandler = itemHandler; return this; }
		public Builder withFluidHandler(IFluidHandler fluidHandler) { this.fluidHandler = fluidHandler; return this; }
		public Builder withEnergyHandler(IEnergyStorage energyHandler) { this.energyHandler = energyHandler; return this; }
		
		
		public TradeContext build() { return new TradeContext(this); }
		
	}
	
}
