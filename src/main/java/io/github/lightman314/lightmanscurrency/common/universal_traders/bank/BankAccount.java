package io.github.lightman314.lightmanscurrency.common.universal_traders.bank;

import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
	}
	
	public BankAccount() { this((IMarkDirty)null); }
	public BankAccount(IMarkDirty markDirty) { this.markDirty = markDirty; }
	
	public BankAccount(CompoundTag compound) { this(null, compound); }
	public BankAccount(IMarkDirty markDirty, CompoundTag compound) {
		this.markDirty = markDirty;
		this.coinStorage.readFromNBT(compound, "CoinStorage");
	}
	
	public void markDirty()
	{
		if(this.markDirty != null)
			this.markDirty.markDirty();
	}
	
	public final CompoundTag save() {
		CompoundTag compound = new CompoundTag();
		this.coinStorage.writeToNBT(compound, "CoinStorage");
		return compound;
	}
	
	public static AccountReference GenerateReference(Player player) { return GenerateReference(player.level.isClientSide, AccountType.Player, player.getUUID()); }
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
	
}
