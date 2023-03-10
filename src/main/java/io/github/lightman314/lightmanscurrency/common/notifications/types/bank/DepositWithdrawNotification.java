package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

public abstract class DepositWithdrawNotification extends Notification {

	public static final ResourceLocation PLAYER_TYPE = new ResourceLocation(LightmansCurrency.MODID, "bank_deposit_player");
	public static final ResourceLocation TRADER_TYPE = new ResourceLocation(LightmansCurrency.MODID, "bank_deposit_trader");
	public static final ResourceLocation SERVER_TYPE = new ResourceLocation(LightmansCurrency.MODID, "bank_deposit_server");

	protected IFormattableTextComponent accountName;
	protected boolean isDeposit;
	protected CoinValue amount = new CoinValue();

	protected DepositWithdrawNotification(IFormattableTextComponent accountName, boolean isDeposit, CoinValue amount) { this.accountName = accountName; this.isDeposit = isDeposit; this.amount = amount; }
	protected DepositWithdrawNotification() {}
	
	@Override
	public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

	@Override
	protected void saveAdditional(CompoundNBT compound) {
		compound.putString("Name", ITextComponent.Serializer.toJson(this.accountName));
		compound.putBoolean("Deposit", this.isDeposit);
		this.amount.save(compound, "Amount");
	}

	@Override
	protected void loadAdditional(CompoundNBT compound) {
		this.accountName = ITextComponent.Serializer.fromJson(compound.getString("Name"));
		this.isDeposit = compound.getBoolean("Deposit");
		this.amount.load(compound, "Amount");
	}
	
	protected abstract IFormattableTextComponent getName();
	
	@Override
	public IFormattableTextComponent getMessage() {
		return EasyText.translatable("log.bank", this.getName(), EasyText.translatable(this.isDeposit ? "log.bank.deposit" : "log.bank.withdraw"), this.amount.getComponent());
	}
	
	public static class Player extends DepositWithdrawNotification {

		PlayerReference player;
		
		public Player(PlayerReference player, IFormattableTextComponent accountName, boolean isDeposit, CoinValue amount) { super(accountName, isDeposit, amount); this.player = player; }
		public Player(CompoundNBT compound) { this.load(compound); }
		
		@Override
		protected IFormattableTextComponent getName() { return this.player.getNameComponent(true); }
		
		@Override
		protected ResourceLocation getType() { return PLAYER_TYPE; }
		
		@Override
		protected void saveAdditional(CompoundNBT compound) {
			super.saveAdditional(compound);
			compound.put("Player", this.player.save());
		}
		
		@Override
		protected void loadAdditional(CompoundNBT compound) {
			super.loadAdditional(compound);
			this.player = PlayerReference.load(compound.getCompound("Player"));
		}
		
		@Override
		protected boolean canMerge(Notification other) {
			if(other instanceof Player)
			{
				Player n = (Player)other;
				return n.accountName.equals(this.accountName) && n.isDeposit == this.isDeposit && n.amount.equals(this.amount) && n.player.is(this.player);
			}
			return false;
		}
		
	}
	
	public static class Trader extends DepositWithdrawNotification {
		IFormattableTextComponent traderName;
		
		public Trader(IFormattableTextComponent traderName, IFormattableTextComponent accountName, boolean isDeposit, CoinValue amount) { super(accountName, isDeposit, amount); this.traderName = traderName; }
		public Trader(CompoundNBT compound) { this.load(compound); }
		
		@Override
		protected IFormattableTextComponent getName() { return this.traderName; }
		
		@Override
		protected ResourceLocation getType() { return TRADER_TYPE; }
		
		@Override
		protected void saveAdditional(CompoundNBT compound) {
			super.saveAdditional(compound);
			compound.putString("Trader", ITextComponent.Serializer.toJson(this.traderName));
		}
		
		@Override
		protected void loadAdditional(CompoundNBT compound) {
			super.loadAdditional(compound);
			this.traderName = ITextComponent.Serializer.fromJson(compound.getString("Trader"));
		}
		
		@Override
		protected boolean canMerge(Notification other) {
			if(other instanceof Trader)
			{
				Trader n = (Trader)other;
				return n.accountName.equals(this.accountName) && n.isDeposit == this.isDeposit && n.amount.equals(this.amount) && n.traderName.equals(this.traderName);
			}
			return false;
		}
		
	}

	public static class Server extends DepositWithdrawNotification {

		public Server(IFormattableTextComponent accountName, boolean isDeposit, CoinValue amount) { super(accountName, isDeposit, amount); }
		public Server(CompoundNBT compound) { this.load(compound); }

		@Override
		protected IFormattableTextComponent getName() { return EasyText.translatable("notifications.bank.server"); }

		@Override
		protected ResourceLocation getType() { return SERVER_TYPE; }

		@Override
		protected boolean canMerge(Notification other) { return false; }

	}
	
}