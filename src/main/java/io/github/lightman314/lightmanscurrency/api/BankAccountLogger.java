package io.github.lightman314.lightmanscurrency.api;

import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class BankAccountLogger extends TextLogger {

	public BankAccountLogger() {
		super("BankAccountHistory");
	}

	public void AddLog(ITrader trader, CoinValue amount, boolean isDeposit) {
		this.AddLog(new TranslationTextComponent("log.bank", this.getTraderText(trader), this.getDepositText(isDeposit), getCostText(amount)));
	}
	
	public void AddLog(PlayerEntity player, CoinValue amount, boolean isDeposit) {
		this.AddLog(new TranslationTextComponent("log.bank", this.getPlayerText(player), this.getDepositText(isDeposit), getCostText(amount)));
	}
	
	public void AddLog(PlayerEntity player, CoinValue amount, ITextComponent destination, boolean wasReceived) {
		this.AddLog(new TranslationTextComponent("log.bank.transfer", this.getPlayerText(player), getCostText(amount), getToFromText(wasReceived), destination));
	}
	
	protected final ITextComponent getToFromText(boolean wasReceived) {
		return new TranslationTextComponent("log.bank.transfer." + (wasReceived ? "from" : "to"));
	}
	
	protected final ITextComponent getDepositText(boolean isDeposit) {
		return new TranslationTextComponent("log.bank." + (isDeposit ? "deposit" : "withdraw"));
	}
	
	protected final ITextComponent getTraderText(ITrader trader) {
		return new TranslationTextComponent(trader.getName().getString()).mergeStyle(TextFormatting.YELLOW);
	}
	
	protected final ITextComponent getPlayerText(PlayerEntity player) {
		return new TranslationTextComponent(player.getDisplayName().getString()).mergeStyle(TextFormatting.GREEN);
	}
	
}
