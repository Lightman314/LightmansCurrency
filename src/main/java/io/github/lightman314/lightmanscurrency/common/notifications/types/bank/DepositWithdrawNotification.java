package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.SingleLineNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class DepositWithdrawNotification extends SingleLineNotification {

	public static final NotificationType<Player> PLAYER_TYPE = new NotificationType<>(VersionUtil.lcResource("bank_deposit_player"),DepositWithdrawNotification::createPlayer);
	public static final NotificationType<Custom> CUSTOM_TYPE = new NotificationType<>(VersionUtil.lcResource("bank_deposit_trader"),DepositWithdrawNotification::createTrader);
	public static final NotificationType<Server> SERVER_TYPE = new NotificationType<>(VersionUtil.lcResource("bank_deposit_server"),DepositWithdrawNotification::createServer);

	protected Component accountName;
	protected boolean isDeposit;
	protected MoneyValue amount = MoneyValue.empty();

	protected DepositWithdrawNotification(Component accountName, boolean isDeposit, MoneyValue amount) { this.accountName = accountName; this.isDeposit = isDeposit; this.amount = amount; }
	protected DepositWithdrawNotification() {}

	@Override
	public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

	@Override
	protected void saveAdditional(CompoundTag compound) {
		compound.putString("Name", Component.Serializer.toJson(this.accountName));
		compound.putBoolean("Deposit", this.isDeposit);
		compound.put("Amount", this.amount.save());
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		this.accountName = Component.Serializer.fromJson(compound.getString("Name"));
		this.isDeposit = compound.getBoolean("Deposit");
		this.amount = MoneyValue.safeLoad(compound, "Amount");
	}
	
	protected abstract Component getName();
	
	
	@Override
	public Component getMessage() { return LCText.NOTIFICATION_BANK_DEPOSIT_WITHDRAW.get(this.getName(), this.isDeposit ? LCText.NOTIFICATION_BANK_DEPOSIT.get() : LCText.NOTIFICATION_BANK_WITHDRAW.get(), this.amount.getText()); }

	private static Player createPlayer() { return new Player(); }
	private static Custom createTrader() { return new Custom(); }
	private static Server createServer() { return new Server(); }

	public static class Player extends DepositWithdrawNotification {

		PlayerReference player;

		private Player() {}
		public Player(PlayerReference player, Component accountName, boolean isDeposit, MoneyValue amount) { super(accountName, isDeposit, amount); this.player = player; }
		
		@Override
		protected Component getName() { return this.player.getNameComponent(true); }
		
		
        @Override
		protected NotificationType<Player> getType() { return PLAYER_TYPE; }
		
		@Override
		protected void saveAdditional(CompoundTag compound) {
			super.saveAdditional(compound);
			compound.put("Player", this.player.save());
		}
		
		@Override
		protected void loadAdditional(CompoundTag compound) {
			super.loadAdditional(compound);
			this.player = PlayerReference.load(compound.getCompound("Player"));
		}
		
		@Override
		protected boolean canMerge(Notification other) {
			if(other instanceof Player n)
				return n.accountName.equals(this.accountName) && n.isDeposit == this.isDeposit && n.amount.equals(this.amount) && n.player.is(this.player);
			return false;
		}
		
	}
	
	public static class Custom extends DepositWithdrawNotification {
        Component objectName;

		private Custom() {}
		public Custom(String objectName, Component accountName, boolean isDeposit, MoneyValue amount) { this(EasyText.literal(objectName),accountName,isDeposit,amount); }
		public Custom(Component objectName, Component accountName, boolean isDeposit, MoneyValue amount) { super(accountName, isDeposit, amount); this.objectName = objectName; }
		
		@Override
		protected Component getName() { return this.objectName; }
		
		
        @Override
		protected NotificationType<Custom> getType() { return CUSTOM_TYPE; }
		
		@Override
		protected void saveAdditional(CompoundTag compound) {
			super.saveAdditional(compound);
			compound.putString("Trader", Component.Serializer.toJson(this.objectName));
		}
		
		@Override
		protected void loadAdditional(CompoundTag compound) {
			super.loadAdditional(compound);
			this.objectName = Component.Serializer.fromJson(compound.getString("Trader"));
		}
		
		@Override
		protected boolean canMerge(Notification other) {
			if(other instanceof Custom n)
				return n.accountName.equals(this.accountName) && n.isDeposit == this.isDeposit && n.amount.equals(this.amount) && n.objectName.equals(this.objectName);
			return false;
		}
		
	}

	public static class Server extends DepositWithdrawNotification {

		private Server() {}
		private Server(Component accountName, boolean isDeposit, MoneyValue amount) { super(accountName, isDeposit, amount); }

		public static Supplier<Notification> create(Component accountName, boolean isDeposit, MoneyValue amount) { return () -> new Server(accountName,isDeposit,amount); }

		@Override
		protected Component getName() { return LCText.NOTIFICATION_BANK_DEPOSIT_WITHDRAW_SERVER.get(); }

		
        @Override
		protected NotificationType<Server> getType() { return SERVER_TYPE; }

		@Override
		protected boolean canMerge(Notification other) { return false; }

	}
	
}
