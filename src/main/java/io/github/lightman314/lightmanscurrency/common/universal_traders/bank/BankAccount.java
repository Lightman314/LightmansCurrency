package io.github.lightman314.lightmanscurrency.common.universal_traders.bank;

import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.api.BankAccountLogger;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BankAccount {
	
	public enum AccountType { Player(0), Team(1);
		
		public final int id;
		
		private AccountType(int id) { this.id = id; }
		
		public static final AccountType fromID(int id) {
			for(AccountType type : AccountType.values())
				if(type.id == id)
					return type;
			return AccountType.Player;
		}
		
	}
	
	private final IMarkDirty markDirty;
	
	private CoinValue coinStorage = new CoinValue();
	public CoinValue getCoinStorage() { return this.coinStorage; }
	
	private BankAccountLogger logger = new BankAccountLogger();
	public BankAccountLogger getLogs() { return this.logger; }
	
	private String ownerName = "Unknown";
	public String getOwnersName() { return this.ownerName; }
	public void updateOwnersName(String ownerName) { this.ownerName = ownerName; }
	public MutableComponent getName() { return new TranslatableComponent("lightmanscurrency.bankaccount", this.ownerName); }
	
	public void depositCoins(CoinValue depositAmount) {
		this.coinStorage = new CoinValue(this.coinStorage.getRawValue() + depositAmount.getRawValue());
		this.markDirty();
	}
	
	public CoinValue withdrawCoins(CoinValue withdrawAmount) {
		if(withdrawAmount.getRawValue() > this.coinStorage.getRawValue())
			withdrawAmount = this.coinStorage.copy();
		this.coinStorage = new CoinValue(this.coinStorage.getRawValue() - withdrawAmount.getRawValue());
		this.markDirty();
		return withdrawAmount;
	}
	
	public void LogInteraction(Player player, CoinValue amount, boolean isDeposit) {
		this.logger.AddLog(player, amount, isDeposit);
		this.markDirty();
	}
	
	public void LogInteraction(ITrader trader, CoinValue amount, boolean isDeposit) {
		this.logger.AddLog(trader, amount, isDeposit);
		this.markDirty();
	}
	
	public void LogTransfer(Player player, CoinValue amount, Component destination, boolean wasReceived) {
		this.logger.AddLog(player, amount, destination, wasReceived);
		this.markDirty();
	}
	
	public static void DepositCoins(IBankAccountMenu menu, CoinValue amount)
	{
		if(menu == null)
			return;
		DepositCoins(menu.getPlayer(), menu.getCoinInput(), menu.getAccount(), amount);
	}
	
	public static void DepositCoins(Player player, Container coinInput, BankAccount account, CoinValue amount)
	{
		if(account == null)
			return;
		
		CoinValue actualAmount = MoneyUtil.getCoinValue(coinInput);
		//If amount is not defined, or the amount is more than the amount available, set the amount to deposit to the actual amount
		if(amount.getRawValue() > actualAmount.getRawValue() || amount.getRawValue() <= 0)
			amount = actualAmount;
		//Handle deposit removal the same as a payment
		MoneyUtil.ProcessPayment(coinInput, player, amount, true);
		//Add the deposit amount to the account
		account.depositCoins(amount);
		account.LogInteraction(player, amount, true);
		
	}
	
	public static void WithdrawCoins(IBankAccountMenu menu, CoinValue amount)
	{
		if(menu == null)
			return;
		WithdrawCoins(menu.getPlayer(), menu.getCoinInput(), menu.getAccount(), amount);
	}
	
	public static void WithdrawCoins(Player player, Container coinOutput, BankAccount account, CoinValue amount)
	{
		if(account == null || amount.getRawValue() <= 0)
			return;
		
		CoinValue withdrawnAmount = account.withdrawCoins(amount);
		
		List<ItemStack> coins = MoneyUtil.getCoinsOfValue(withdrawnAmount);
		//Attempt to fill the coins into the coin slots
		for(int i = 0; i < coins.size(); ++i)
		{
			ItemStack remainder = InventoryUtil.TryPutItemStack(coinOutput, coins.get(i));
			if(!remainder.isEmpty())
			{
				//Attempt to give it to the player directly
				if(!player.addItem(remainder))
				{
					//Drop the remainder on the ground
					InventoryUtil.dumpContents(player.level, player.blockPosition(), remainder);
				}
			}
		}
		account.LogInteraction(player, withdrawnAmount, false);
	}
	
	public static Component TransferCoins(IBankAccountTransferMenu menu, CoinValue amount, AccountReference destination)
	{
		return TransferCoins(menu.getPlayer(), menu.getAccount(), amount, destination.get());
	}
	
	public static Component TransferCoins(Player player, BankAccount fromAccount, CoinValue amount, BankAccount destinationAccount)
	{
		if(fromAccount == null)
			return new TranslatableComponent("gui.bank.transfer.error.null.from");
		if(destinationAccount == null)
			return new TranslatableComponent("gui.bank.transfer.error.null.to");
		if(amount.getRawValue() <= 0)
			return new TranslatableComponent("gui.bank.transfer.error.amount", amount.getString("nothing"));
		if(fromAccount == destinationAccount)
			return new TranslatableComponent("gui.bank.transfer.error.same");
		
		CoinValue withdrawnAmount = fromAccount.withdrawCoins(amount);
		destinationAccount.depositCoins(withdrawnAmount);
		fromAccount.LogTransfer(player, withdrawnAmount, destinationAccount.getName().withStyle(ChatFormatting.GOLD), false);
		destinationAccount.LogTransfer(player, withdrawnAmount, fromAccount.getName().withStyle(ChatFormatting.GOLD), true);
		
		return new TranslatableComponent("gui.bank.transfer.success", amount.getString(), destinationAccount.getName());
		
	}
	
	public BankAccount() { this((IMarkDirty)null); }
	public BankAccount(IMarkDirty markDirty) { this.markDirty = markDirty; }
	
	public BankAccount(CompoundTag compound) { this(null, compound); }
	public BankAccount(IMarkDirty markDirty, CompoundTag compound) {
		this.markDirty = markDirty;
		this.coinStorage.readFromNBT(compound, "CoinStorage");
		this.logger.read(compound);
		if(compound.contains("OwnerName"))
			this.ownerName = compound.getString("OwnerName");
	}
	
	public void markDirty()
	{
		if(this.markDirty != null)
			this.markDirty.markDirty();
	}
	
	public final CompoundTag save() {
		CompoundTag compound = new CompoundTag();
		this.coinStorage.writeToNBT(compound, "CoinStorage");
		this.logger.write(compound);
		compound.putString("OwnerName", this.ownerName);
		return compound;
	}
	
	public static AccountReference GenerateReference(Player player) { return GenerateReference(player.level.isClientSide, AccountType.Player, player.getUUID()); }
	public static AccountReference GenerateReference(boolean isClient, PlayerReference player) { return GenerateReference(isClient, AccountType.Player, player.id); }
	public static AccountReference GenerateReference(boolean isClient, Team team) { return GenerateReference(isClient, AccountType.Team, team.getID()); }
	
	public static AccountReference GenerateReference(boolean isClient, AccountType accountType, UUID id) { return new AccountReference(isClient, accountType, id); }
	
	public static AccountReference LoadReference(boolean isClient, CompoundTag compound) {
		AccountType accountType = AccountType.fromID(compound.getInt("Type"));
		UUID id = compound.getUUID("ID");
		return GenerateReference(isClient, accountType, id);
	}
	
	public static AccountReference LoadReference(boolean isClient, FriendlyByteBuf buffer) {
		try {
			AccountType accountType = AccountType.fromID(buffer.readInt());
			UUID id = buffer.readUUID();
			return GenerateReference(isClient, accountType, id);
		} catch(Exception e) { e.printStackTrace(); return null; }
	}
	
	public static class AccountReference {
		
		private final boolean isClient;
		public final AccountType accountType;
		public final UUID id;
		
		private AccountReference(boolean isClient, AccountType accountType, UUID id) { this.isClient = isClient; this.accountType = accountType; this.id = id; }
		
		public CompoundTag save() {
			CompoundTag compound = new CompoundTag();
			compound.putInt("Type", this.accountType.id);
			compound.putUUID("ID", this.id);
			return compound;
		}
		
		public void writeToBuffer(FriendlyByteBuf buffer) {
			buffer.writeInt(this.accountType.id);
			buffer.writeUUID(this.id);
		}
		
		public BankAccount get() {
			if(isClient)
			{
				switch(this.accountType) {
				case Player:
					return ClientTradingOffice.getPlayerBankAccount(this.id);
				case Team:
					Team team = ClientTradingOffice.getTeam(this.id);
					if(team != null && team.hasBankAccount())
						return team.getBankAccount();
				default:
					return null;
				}
			}
			else
			{
				switch(this.accountType) {
				case Player:
					return TradingOffice.getBankAccount(this.id);
				case Team:
					Team team = TradingOffice.getTeam(this.id);
					if(team != null && team.hasBankAccount())
						return team.getBankAccount();
				default:
					return null;
				}
			}
		}
		
	}
	
	public interface IMarkDirty { public void markDirty(); }
	
	public interface IBankAccountMenu
	{
		public Player getPlayer();
		public Container getCoinInput();
		public BankAccount getAccount();
		public default void onDepositOrWithdraw() {}
	}
	
	public interface IBankAccountTransferMenu extends IBankAccountMenu
	{
		public AccountReference getAccountSource();
		public Component getLastMessage();
		public void setMessage(Component component);
	}
	
}
