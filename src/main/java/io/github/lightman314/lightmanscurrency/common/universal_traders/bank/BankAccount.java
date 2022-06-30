package io.github.lightman314.lightmanscurrency.common.universal_traders.bank;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import io.github.lightman314.lightmanscurrency.api.BankAccountLogger;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.notifications.types.LowBalanceNotification;
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
	
	private CoinValue notificationLevel = new CoinValue();
	public CoinValue getNotificationValue() { return this.notificationLevel; }
	public long getNotificationLevel() { return this.notificationLevel.getRawValue(); }
	public void setNotificationValue(CoinValue value) { this.notificationLevel = value.copy(); this.markDirty(); }
	
	private BiConsumer<MutableComponent,CoinValue> notificationSender;
	public void setNotificationConsumer(BiConsumer<MutableComponent,CoinValue> notificationSender) { this.notificationSender = notificationSender; }
	public void pushNotification() {
		if(this.notificationSender != null)
			this.notificationSender.accept(this.getName(), this.getNotificationValue());
	}
	
	public static BiConsumer<MutableComponent,CoinValue> generateNotificationAcceptor(UUID playerID) {
		return (accountName,value) -> {
			TradingOffice.pushNotification(playerID, new LowBalanceNotification(accountName, value));
		};
	}
	
	private BankAccountLogger logger = new BankAccountLogger();
	public BankAccountLogger getLogs() { return this.logger; }
	
	private String ownerName = "Unknown";
	public String getOwnersName() { return this.ownerName; }
	public void updateOwnersName(String ownerName) { this.ownerName = ownerName; }
	public MutableComponent getName() { return Component.translatable("lightmanscurrency.bankaccount", this.ownerName); }
	
	public void depositCoins(CoinValue depositAmount) {
		this.coinStorage = new CoinValue(this.coinStorage.getRawValue() + depositAmount.getRawValue());
		this.markDirty();
	}
	
	public CoinValue withdrawCoins(CoinValue withdrawAmount) {
		long oldValue = this.coinStorage.getRawValue();
		if(withdrawAmount.getRawValue() > this.coinStorage.getRawValue())
			withdrawAmount = this.coinStorage.copy();
		//Cannot withdraw no money
		if(withdrawAmount.getRawValue() <= 0)
			return CoinValue.EMPTY;
		this.coinStorage.readFromOldValue(this.coinStorage.getRawValue() - withdrawAmount.getRawValue());
		this.markDirty();
		//Check if we should push the notification
		if(oldValue >= this.getNotificationLevel() && this.coinStorage.getRawValue() < this.getNotificationLevel())
			this.pushNotification();
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
		DepositCoins(menu.getPlayer(), menu.getCoinInput(), menu.getBankAccount(), amount);
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
		WithdrawCoins(menu.getPlayer(), menu.getCoinInput(), menu.getBankAccount(), amount);
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
	
	public static MutableComponent TransferCoins(IBankAccountAdvancedMenu menu, CoinValue amount, AccountReference destination)
	{
		return TransferCoins(menu.getPlayer(), menu.getBankAccount(), amount, destination.get());
	}
	
	public static MutableComponent TransferCoins(Player player, BankAccount fromAccount, CoinValue amount, BankAccount destinationAccount)
	{
		if(fromAccount == null)
			return Component.translatable("gui.bank.transfer.error.null.from");
		if(destinationAccount == null)
			return Component.translatable("gui.bank.transfer.error.null.to");
		if(amount.getRawValue() <= 0)
			return Component.translatable("gui.bank.transfer.error.amount", amount.getString("nothing"));
		if(fromAccount == destinationAccount)
			return Component.translatable("gui.bank.transfer.error.same");
		
		CoinValue withdrawnAmount = fromAccount.withdrawCoins(amount);
		if(withdrawnAmount.getRawValue() <= 0)
			return Component.translatable("gui.bank.transfer.error.nobalance", amount.getString());
		
		destinationAccount.depositCoins(withdrawnAmount);
		fromAccount.LogTransfer(player, withdrawnAmount, destinationAccount.getName().withStyle(ChatFormatting.GOLD), false);
		destinationAccount.LogTransfer(player, withdrawnAmount, fromAccount.getName().withStyle(ChatFormatting.GOLD), true);
		
		return Component.translatable("gui.bank.transfer.success", withdrawnAmount.getString(), destinationAccount.getName());
		
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
		if(compound.contains("NotificationLevel"))
			this.notificationLevel.readFromNBT(compound, "NotificationLevel");
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
		this.notificationLevel.writeToNBT(compound, "NotificationLevel");
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
			switch(this.accountType) {
			case Player:
				return this.isClient ? ClientTradingOffice.getPlayerBankAccount(this.id) : TradingOffice.getBankAccount(this.id);
			case Team:
				Team team = this.isClient ? ClientTradingOffice.getTeam(this.id) : TradingOffice.getTeam(this.id);
				if(team != null && team.hasBankAccount())
					return team.getBankAccount();
			default:
				return null;
			}
		}
		
		public boolean allowedAccess(Player player) {
			switch(this.accountType)
			{
			case Player:
				return player.getUUID().equals(this.id) || TradingOffice.isAdminPlayer(player);
			case Team:
				Team team = this.isClient ? ClientTradingOffice.getTeam(this.id) : TradingOffice.getTeam(this.id);
				if(team != null && team.hasBankAccount())
					return team.canAccessBankAccount(player);
			default:
				return false;
			}
		}
		
	}
	
	public interface IMarkDirty { public void markDirty(); }
	
	public interface IBankAccountMenu
	{
		public Player getPlayer();
		public Container getCoinInput();
		public default void onDepositOrWithdraw() {}
		public boolean isClient();
		public default AccountReference getBankAccountReference() {
			return this.isClient() ? ClientTradingOffice.getLastSelectedAccount() : TradingOffice.getSelectedBankAccount(this.getPlayer());
		}
		public default BankAccount getBankAccount() {
			AccountReference reference = this.getBankAccountReference();
			return reference == null ? null : reference.get();
		}
	}
	
	public interface IBankAccountAdvancedMenu extends IBankAccountMenu
	{
		public void setTransferMessage(MutableComponent component);
		public default void setNotificationLevel(CoinValue amount) {
			BankAccount account = this.getBankAccount();
			if(account != null)
				account.setNotificationValue(amount);
		}
	}
	
}
