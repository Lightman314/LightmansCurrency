package io.github.lightman314.lightmanscurrency.common.bank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.common.bank.interfaces.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.common.bank.interfaces.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.common.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationSaveData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.BankTransferNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.DepositWithdrawNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.LowBalanceNotification;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BankAccount extends IMoneyHolder.Slave {
	
	private final Runnable markDirty;
	
	private final MoneyStorage coinStorage = new MoneyStorage(this::markDirty);
	public MoneyStorage getMoneyStorage() { return this.coinStorage; }

	@Override
	@Nullable
	protected IMoneyHolder getParent() { return this.coinStorage; }

	private final Map<String,MoneyValue> notificationLevels = new HashMap<>();
	public Map<String,MoneyValue> getNotificationValues() { return ImmutableMap.copyOf(this.notificationLevels); }
	public MoneyValue getNotificationLevelFor(@Nonnull String type) { return this.notificationLevels.getOrDefault(type, MoneyValue.empty()); }
	public void clearNotificationValue(@Nonnull String key) {
		this.notificationLevels.remove(key);
		this.markDirty();
	}
	public void setNotificationValue(@Nonnull MoneyValue value) {
		if(value.isEmpty() || value.isFree())
			return;
		String key = value.getUniqueName();
		this.notificationLevels.put(key, value);
		this.markDirty();
	}
	
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
	public MutableComponent getName() { return EasyText.translatable("lightmanscurrency.bankaccount", this.ownerName); }
	
	public void depositCoins(@Nonnull MoneyValue depositAmount) {
		this.coinStorage.addValue(depositAmount);
	}

	@Nonnull
	public MoneyValue withdrawCoins(@Nonnull MoneyValue withdrawAmount) {
		String type = withdrawAmount.getUniqueName();
		withdrawAmount = this.coinStorage.capValue(withdrawAmount);
		//Cannot withdraw no money
		if(withdrawAmount.isEmpty())
			return MoneyValue.empty();
		long oldValue = this.coinStorage.valueOf(type).getCoreValue();
		this.coinStorage.removeValue(withdrawAmount);
		//Check if we should push the notification
		MoneyValue notificationLevel = this.getNotificationLevelFor(withdrawAmount.getUniqueName());
		long nl = notificationLevel.getCoreValue();
		if(oldValue >= nl && this.coinStorage.valueOf(type).getCoreValue() < nl)
			this.pushNotification(LowBalanceNotification.create(this.getName(), notificationLevel));
		return withdrawAmount;
	}
	
	public void LogInteraction(Player player, MoneyValue amount, boolean isDeposit) {
		this.pushLocalNotification(new DepositWithdrawNotification.Player(PlayerReference.of(player), this.getName(), isDeposit, amount));
		this.markDirty();
	}
	
	public void LogInteraction(TaxEntry tax, MoneyValue amount) {
		this.pushLocalNotification(new DepositWithdrawNotification.Trader(tax.getName(), this.getName(), true, amount));
		this.markDirty();
	}

	public void LogInteraction(TraderData trader, MoneyValue amount, boolean isDeposit) {
		this.pushLocalNotification(new DepositWithdrawNotification.Trader(trader.getName(), this.getName(), isDeposit, amount));
		this.markDirty();
	}
	
	public void LogTransfer(Player player, MoneyValue amount, MutableComponent otherAccount, boolean wasReceived) {
		this.pushLocalNotification(new BankTransferNotification(PlayerReference.of(player), amount, this.getName(), otherAccount, wasReceived));
		this.markDirty();
	}
	
	public static void DepositCoins(IBankAccountMenu menu, MoneyValue amount)
	{
		if(menu == null)
			return;
		DepositCoins(menu.getPlayer(), menu.getCoinInput(), menu.getBankAccount(), amount);
	}
	
	public static void DepositCoins(Player player, Container coinInput, BankAccount account, MoneyValue amount)
	{
		if(account == null)
			return;

		MoneyView valueOfContainer = MoneyAPI.valueOfContainer(coinInput);
		for(MoneyValue value : valueOfContainer.allValues())
		{
			if(value.sameType(amount))
			{
				MoneyValue depositAmount = amount;
				if(depositAmount.isEmpty() || !value.containsValue(depositAmount))
					depositAmount = value;
				//Take the money from the container
				MoneyAPI.takeMoneyFromContainer(coinInput, player, depositAmount);
				//Add the money to the bank account
				account.depositCoins(depositAmount);
				account.LogInteraction(player, depositAmount, true);
				return;
			}
		}
		
	}

	public static boolean ServerGiveCoins(BankAccount account, MoneyValue amount)
	{
		if(account == null || amount.isEmpty())
			return false;

		account.depositCoins(amount);
		account.pushNotification(() -> new DepositWithdrawNotification.Server(account.getName(), true, amount));
		return true;
	}

	public static Pair<Boolean, MoneyValue> ServerTakeCoins(BankAccount account, MoneyValue amount)
	{
		if(account == null || amount.isEmpty())
			return Pair.of(false, MoneyValue.empty());

		MoneyValue taken = account.withdrawCoins(amount);
		account.pushNotification(() -> new DepositWithdrawNotification.Server(account.getName(), false, taken));
		return Pair.of(true, taken);
	}
	
	public static void WithdrawCoins(IBankAccountMenu menu, MoneyValue amount)
	{
		if(menu == null)
			return;
		WithdrawCoins(menu.getPlayer(), menu.getCoinInput(), menu.getBankAccount(), amount);
	}
	
	public static void WithdrawCoins(Player player, Container coinOutput, BankAccount account, MoneyValue amount)
	{
		if(account == null || amount.isEmpty())
			return;
		
		MoneyValue withdrawnAmount = account.withdrawCoins(amount);

		CurrencyType currencyType = withdrawnAmount.getCurrency();
		if(currencyType == null)
		{
			account.depositCoins(withdrawnAmount);
			return;
		}
		MoneyAPI.addMoneyToContainer(coinOutput, player, withdrawnAmount);
		account.LogInteraction(player, withdrawnAmount, false);
	}
	
	public static MutableComponent TransferCoins(IBankAccountAdvancedMenu menu, MoneyValue amount, BankReference destination)
	{
		return TransferCoins(menu.getPlayer(), menu.getBankAccount(), amount, destination == null ? null : destination.get());
	}
	
	public static MutableComponent TransferCoins(Player player, BankAccount fromAccount, MoneyValue amount, BankAccount destinationAccount)
	{
		if(fromAccount == null)
			return EasyText.translatable("gui.bank.transfer.error.null.from");
		if(destinationAccount == null)
			return EasyText.translatable("gui.bank.transfer.error.null.to");
		if(amount.isEmpty())
			return EasyText.translatable("gui.bank.transfer.error.amount", amount.getString("nothing"));
		if(fromAccount == destinationAccount)
			return EasyText.translatable("gui.bank.transfer.error.same");
		
		MoneyValue withdrawnAmount = fromAccount.withdrawCoins(amount);
		if(withdrawnAmount.isEmpty())
			return EasyText.translatable("gui.bank.transfer.error.nobalance", amount.getString());
		
		destinationAccount.depositCoins(withdrawnAmount);
		fromAccount.LogTransfer(player, withdrawnAmount, destinationAccount.getName(), false);
		destinationAccount.LogTransfer(player, withdrawnAmount, fromAccount.getName(), true);
		
		return EasyText.translatable("gui.bank.transfer.success", withdrawnAmount.getString(), destinationAccount.getName());
		
	}
	
	public BankAccount() { this((Runnable)null); }
	public BankAccount(Runnable markDirty) { this.markDirty = markDirty; }
	
	public BankAccount(CompoundTag compound) { this(null, compound); }
	public BankAccount(Runnable markDirty, CompoundTag compound) {
		this.markDirty = markDirty;
		this.coinStorage.safeLoad(compound, "CoinStorage");
		this.logger.load(compound.getCompound("AccountLogs"));
		this.ownerName = compound.getString("OwnerName");
		if(compound.contains("NotificationLevel"))
		{
			MoneyValue level = MoneyValue.safeLoad(compound, "NotificationLevel");
			if(!level.isEmpty() && !level.isFree())
				this.notificationLevels.put(level.getUniqueName(), level);
		}
		else if(compound.contains("NotificationLevels"))
		{
			ListTag list = compound.getList("NotificationLevels", Tag.TAG_COMPOUND);
			for(int i = 0; i < list.size(); ++i)
			{
				MoneyValue level = MoneyValue.load(list.getCompound(i));
				if(level.isInvalid() || (!level.isFree() && !level.isEmpty()))
					this.notificationLevels.put(level.getUniqueName(), level);
			}
		}
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
		ListTag notificationLevelList = new ListTag();
		this.notificationLevels.forEach((key,level) -> {
			notificationLevelList.add(level.save());
		});
		compound.put("NotificationLevels", notificationLevelList);
		return compound;
	}

	@Override
	public void formatTooltip(@Nonnull List<Component> tooltip) {
		IMoneyHolder.defaultTooltipFormat(tooltip, this.getTooltipTitle(), this.getStoredMoney());
	}

	@Override
	public Component getTooltipTitle() { return EasyText.translatable("tooltip.lightmanscurrency.trader.info.money.bank",this.getName()); }

}
