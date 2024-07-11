package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank;

import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletBankScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget.IBankAccountWidget;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class InteractionTab extends WalletBankTab implements IBankAccountWidget {

	public InteractionTab(WalletBankScreen screen) { super(screen); }

	BankAccountWidget accountWidget;
	
	@Nonnull
    @Override
	public IconData getIcon() { return IconData.of(ModBlocks.COINPILE_GOLD); }

	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.atm.interact"); }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.accountWidget = this.addChild(new BankAccountWidget(screenArea.y, this, 7, this::addChild));
		this.accountWidget.allowEmptyDeposits = false;
		this.accountWidget.getAmountSelection().drawBG = false;
		
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		Component accountName = Component.literal("ERROR FINDING ACCOUNT");
		IBankAccount ba = this.screen.getMenu().getBankAccount();
		if(ba != null)
			accountName = ba.getName();
		gui.drawString(accountName, 8, MoneyValueWidget.HEIGHT, 0x404040);
		this.accountWidget.renderInfo(gui);
	}

	@Override
	public void tick() { this.accountWidget.tick(); }

	@Override
	public Screen getScreen() { return this.screen; }

	@Override
	public IBankAccount getBankAccount() { return this.screen.getMenu().getBankAccount(); }

	@Override
	public IMoneyViewer getCoinAccess() { return this.screen.getMenu().getCoinInputHandler(); }

}
