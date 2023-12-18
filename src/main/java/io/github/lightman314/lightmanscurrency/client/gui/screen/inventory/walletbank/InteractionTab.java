package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank;

import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletBankScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget.IBankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class InteractionTab extends WalletBankTab implements IBankAccountWidget {

	public InteractionTab(WalletBankScreen screen) { super(screen); }

	BankAccountWidget accountWidget;
	
	@Nonnull
    @Override
	public @NotNull IconData getIcon() { return IconData.of(ModBlocks.COINPILE_GOLD); }

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
		if(this.screen.getMenu().getBankAccount() != null)
			accountName = this.screen.getMenu().getBankAccount().getName();
		gui.drawString(accountName, 8, MoneyValueWidget.HEIGHT, 0x404040);
		this.accountWidget.renderInfo(gui);
	}

	@Override
	public void tick() { this.accountWidget.tick(); }

	@Override
	public Screen getScreen() { return this.screen; }

	@Override
	public BankAccount getBankAccount() { return this.screen.getMenu().getBankAccount(); }

	@Override
	public Container getCoinAccess() { return this.screen.getMenu().getCoinInput(); }

}
