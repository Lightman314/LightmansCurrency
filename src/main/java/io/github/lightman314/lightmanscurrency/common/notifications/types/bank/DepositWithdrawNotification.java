package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class DepositWithdrawNotification extends Notification {

	public static final NotificationType<Player> PLAYER_TYPE = new NotificationType<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "bank_deposit_player"),DepositWithdrawNotification::createPlayer);
	public static final NotificationType<Trader> TRADER_TYPE = new NotificationType<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "bank_deposit_trader"),DepositWithdrawNotification::createTrader);
	public static final NotificationType<Server> SERVER_TYPE = new NotificationType<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "bank_deposit_server"),DepositWithdrawNotification::createServer);

	protected MutableComponent accountName;
	protected boolean isDeposit;
	protected MoneyValue amount = MoneyValue.empty();

	protected DepositWithdrawNotification(MutableComponent accountName, boolean isDeposit, MoneyValue amount) { this.accountName = accountName; this.isDeposit = isDeposit; this.amount = amount; }
	protected DepositWithdrawNotification() {}
	
	@Nonnull
	@Override
	public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		compound.putString("Name", Component.Serializer.toJson(this.accountName,lookup));
		compound.putBoolean("Deposit", this.isDeposit);
		compound.put("Amount", this.amount.save());
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		this.accountName = Component.Serializer.fromJson(compound.getString("Name"),lookup);
		this.isDeposit = compound.getBoolean("Deposit");
		this.amount = MoneyValue.safeLoad(compound, "Amount");
	}
	
	protected abstract MutableComponent getName();
	
	@Nonnull
	@Override
	public MutableComponent getMessage() { return LCText.NOTIFICATION_BANK_DEPOSIT_WITHDRAW.get(this.getName(), this.isDeposit ? LCText.NOTIFICATION_BANK_DEPOSIT.get() : LCText.NOTIFICATION_BANK_WITHDRAW.get(), this.amount.getText()); }

	private static Player createPlayer() { return new Player(); }
	private static Trader createTrader() { return new Trader(); }
	private static Server createServer() { return new Server(); }

	public static class Player extends DepositWithdrawNotification {

		PlayerReference player;

		private Player() {}
		public Player(PlayerReference player, MutableComponent accountName, boolean isDeposit, MoneyValue amount) { super(accountName, isDeposit, amount); this.player = player; }
		
		@Override
		protected MutableComponent getName() { return this.player.getNameComponent(true); }
		
		@Nonnull
        @Override
		protected NotificationType<Player> getType() { return PLAYER_TYPE; }
		
		@Override
		protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
			super.saveAdditional(compound,lookup);
			compound.put("Player", this.player.save());
		}
		
		@Override
		protected void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
			super.loadAdditional(compound,lookup);
			this.player = PlayerReference.load(compound.getCompound("Player"));
		}
		
		@Override
		protected boolean canMerge(@Nonnull Notification other) {
			if(other instanceof Player n)
				return n.accountName.equals(this.accountName) && n.isDeposit == this.isDeposit && n.amount.equals(this.amount) && n.player.is(this.player);
			return false;
		}
		
	}
	
	public static class Trader extends DepositWithdrawNotification {
		MutableComponent traderName;

		private Trader() {}
		public Trader(MutableComponent traderName, MutableComponent accountName, boolean isDeposit, MoneyValue amount) { super(accountName, isDeposit, amount); this.traderName = traderName; }
		
		@Override
		protected MutableComponent getName() { return this.traderName; }
		
		@Nonnull
        @Override
		protected NotificationType<Trader> getType() { return TRADER_TYPE; }
		
		@Override
		protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
			super.saveAdditional(compound,lookup);
			compound.putString("Trader", Component.Serializer.toJson(this.traderName,lookup));
		}
		
		@Override
		protected void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
			super.loadAdditional(compound,lookup);
			this.traderName = Component.Serializer.fromJson(compound.getString("Trader"),lookup);
		}
		
		@Override
		protected boolean canMerge(@Nonnull Notification other) {
			if(other instanceof Trader n)
				return n.accountName.equals(this.accountName) && n.isDeposit == this.isDeposit && n.amount.equals(this.amount) && n.traderName.equals(this.traderName);
			return false;
		}
		
	}

	public static class Server extends DepositWithdrawNotification {

		private Server() {}
		private Server(MutableComponent accountName, boolean isDeposit, MoneyValue amount) { super(accountName, isDeposit, amount); }

		public static Supplier<Notification> create(@Nonnull MutableComponent accountName, boolean isDeposit, @Nonnull MoneyValue amount) { return () -> new Server(accountName,isDeposit,amount); }

		@Override
		protected MutableComponent getName() { return LCText.NOTIFICATION_BANK_DEPOSIT_WITHDRAW_SERVER.get(); }

		@Nonnull
        @Override
		protected NotificationType<Server> getType() { return SERVER_TYPE; }

		@Override
		protected boolean canMerge(@Nonnull Notification other) { return false; }

	}
	
}
