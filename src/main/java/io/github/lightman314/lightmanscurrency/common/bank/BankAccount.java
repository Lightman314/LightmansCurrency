package io.github.lightman314.lightmanscurrency.common.bank;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.common.bank.interfaces.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.common.bank.interfaces.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.common.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationSaveData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.BankTransferNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.DepositWithdrawNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.LowBalanceNotification;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.NonNullSupplier;

public class BankAccount {
	
	private final Runnable markDirty;
	
	private CoinValue coinStorage = CoinValue.EMPTY;
	public CoinValue getCoinStorage() { return this.coinStorage; }
	
	private CoinValue notificationLevel = CoinValue.EMPTY;
	public CoinValue getNotificationValue() { return this.notificationLevel; }
	public long getNotificationLevel() { return this.notificationLevel.getValueNumber(); }
	public void setNotificationValue(CoinValue value) { this.notificationLevel = value; this.markDirty(); }
	
	private Consumer<NonNullSupplier<Notification>> notificationSender;
	public void setNotificationConsumer(Consumer<NonNullSupplier<Notification>> notificationSender) { this.notificationSender = notificationSender; }
	public void pushLocalNotification(Notification notification) {
		this.logger.addNotification(notification);
		this.markDirty();
	}
	public void pushNotification(NonNullSupplier<Notification> notification) {
		this.pushLocalNotification(notification.get());
		if(this.notificationSender != null)
			this.notificationSender.accept(notification);
	}
	
	public static Consumer<NonNullSupplier<Notification>> generateNotificationAcceptor(UUID playerID) {
		return (notification) -> NotificationSaveData.PushNotification(playerID, notification.get());
	}
	
	private final NotificationData logger = new NotificationData();
	public List<Notification> getNotifications() { return this.logger.getNotifications(); }
	
	private String ownerName = "Unknown";
	public String getOwnersName() { return this.ownerName; }
	public void updateOwnersName(String ownerName) { this.ownerName = ownerName; }
	public MutableComponent getName() { return Component.translatable("lightmanscurrency.bankaccount", this.ownerName); }
	
	public void depositCoins(CoinValue depositAmount) {
		this.coinStorage = this.coinStorage.plusValue(depositAmount);
		this.markDirty();
	}
	
	public CoinValue withdrawCoins(CoinValue withdrawAmount) {
		long oldValue = this.coinStorage.getValueNumber();
		if(withdrawAmount.getValueNumber() > this.coinStorage.getValueNumber())
			withdrawAmount = this.coinStorage;
		//Cannot withdraw no money
		if(withdrawAmount.getValueNumber() <= 0)
			return CoinValue.EMPTY;
		this.coinStorage = this.coinStorage.minusValue(withdrawAmount);
		this.markDirty();
		//Check if we should push the notification
		if(oldValue >= this.getNotificationLevel() && this.coinStorage.getValueNumber() < this.getNotificationLevel())
			this.pushNotification(() -> new LowBalanceNotification(this.getName(), this.notificationLevel));
		return withdrawAmount;
	}
	
	public void LogInteraction(Player player, CoinValue amount, boolean isDeposit) {
		this.pushLocalNotification(new DepositWithdrawNotification.Player(PlayerReference.of(player), this.getName(), isDeposit, amount));
		this.markDirty();
	}
	
	public void LogInteraction(TaxEntry tax, CoinValue amount) {
		this.pushLocalNotification(new DepositWithdrawNotification.Trader(tax.getName(), this.getName(), true, amount));
		this.markDirty();
	}

	public void LogInteraction(TraderData trader, CoinValue amount, boolean isDeposit) {
		this.pushLocalNotification(new DepositWithdrawNotification.Trader(trader.getName(), this.getName(), isDeposit, amount));
		this.markDirty();
	}
	
	public void LogTransfer(Player player, CoinValue amount, MutableComponent otherAccount, boolean wasReceived) {
		this.pushLocalNotification(new BankTransferNotification(PlayerReference.of(player), amount, this.getName(), otherAccount, wasReceived));
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
		if(amount.getValueNumber() > actualAmount.getValueNumber() || amount.getValueNumber() <= 0)
			amount = actualAmount;
		//Handle deposit removal the same as a payment
		MoneyUtil.ProcessPayment(coinInput, player, amount, true);
		//Add the deposit amount to the account
		account.depositCoins(amount);
		account.LogInteraction(player, amount, true);
		
	}

	public static boolean ServerGiveCoins(BankAccount account, CoinValue amount)
	{
		if(account == null || !amount.hasAny())
			return false;

		account.depositCoins(amount);
		account.pushNotification(() -> new DepositWithdrawNotification.Server(account.getName(), true, amount));
		return true;
	}

	public static Pair<Boolean,CoinValue> ServerTakeCoins(BankAccount account, CoinValue amount)
	{
		if(account == null || !amount.hasAny())
			return Pair.of(false, CoinValue.EMPTY);

		CoinValue taken = account.withdrawCoins(amount);
		account.pushNotification(() -> new DepositWithdrawNotification.Server(account.getName(), false, taken));
		return Pair.of(true, taken);
	}
	
	public static void WithdrawCoins(IBankAccountMenu menu, CoinValue amount)
	{
		if(menu == null)
			return;
		WithdrawCoins(menu.getPlayer(), menu.getCoinInput(), menu.getBankAccount(), amount);
	}
	
	public static void WithdrawCoins(Player player, Container coinOutput, BankAccount account, CoinValue amount)
	{
		if(account == null || amount.getValueNumber() <= 0)
			return;
		
		CoinValue withdrawnAmount = account.withdrawCoins(amount);
		
		List<ItemStack> coins = MoneyUtil.getCoinsOfValue(withdrawnAmount);
		//Attempt to fill the coins into the coin slots
		for (ItemStack coin : coins) {
			ItemStack remainder = InventoryUtil.TryPutItemStack(coinOutput, coin);
			if (!remainder.isEmpty()) {
				//Attempt to give it to the player directly
				if (!player.addItem(remainder)) {
					//Drop the remainder on the ground
					InventoryUtil.dumpContents(player.level(), player.blockPosition(), remainder);
				}
			}
		}
		account.LogInteraction(player, withdrawnAmount, false);
	}
	
	public static MutableComponent TransferCoins(IBankAccountAdvancedMenu menu, CoinValue amount, BankReference destination)
	{
		return TransferCoins(menu.getPlayer(), menu.getBankAccount(), amount, destination == null ? null : destination.get());
	}
	
	public static MutableComponent TransferCoins(Player player, BankAccount fromAccount, CoinValue amount, BankAccount destinationAccount)
	{
		if(fromAccount == null)
			return Component.translatable("gui.bank.transfer.error.null.from");
		if(destinationAccount == null)
			return Component.translatable("gui.bank.transfer.error.null.to");
		if(amount.getValueNumber() <= 0)
			return Component.translatable("gui.bank.transfer.error.amount", amount.getString("nothing"));
		if(fromAccount == destinationAccount)
			return Component.translatable("gui.bank.transfer.error.same");
		
		CoinValue withdrawnAmount = fromAccount.withdrawCoins(amount);
		if(withdrawnAmount.getValueNumber() <= 0)
			return Component.translatable("gui.bank.transfer.error.nobalance", amount.getString());
		
		destinationAccount.depositCoins(withdrawnAmount);
		fromAccount.LogTransfer(player, withdrawnAmount, destinationAccount.getName(), false);
		destinationAccount.LogTransfer(player, withdrawnAmount, fromAccount.getName(), true);
		
		return Component.translatable("gui.bank.transfer.success", withdrawnAmount.getString(), destinationAccount.getName());
		
	}
	
	public BankAccount() { this((Runnable)null); }
	public BankAccount(Runnable markDirty) { this.markDirty = markDirty; }
	
	public BankAccount(CompoundTag compound) { this(null, compound); }
	public BankAccount(Runnable markDirty, CompoundTag compound) {
		this.markDirty = markDirty;
		this.coinStorage = CoinValue.safeLoad(compound, "CoinStorage");
		this.logger.load(compound.getCompound("AccountLogs"));
		this.ownerName = compound.getString("OwnerName");
		this.notificationLevel = CoinValue.safeLoad(compound, "NotificationLevel");
	}
	
	public void markDirty()
	{
		if(this.markDirty != null)
			this.markDirty.run();
	}
	
	public final CompoundTag save() {
		CompoundTag compound = new CompoundTag();
		compound.put("CoinStorage", this.coinStorage.save());
		compound.put("AccountLogs", this.logger.save());
		compound.putString("OwnerName", this.ownerName);
		compound.put("NotificationLevel", this.notificationLevel.save());
		return compound;
	}
	
}
