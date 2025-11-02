package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank;

import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletBankScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.bank.BankInteractionWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.bank.IBankInteractionHandler;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class InteractionTab extends WalletBankTab implements IBankInteractionHandler {

	public InteractionTab(WalletBankScreen screen) { super(screen); }

	BankInteractionWidget accountWidget;

	@Nonnull
	@Override
	public IconData getIcon() { return ItemIcon.ofItem(ModBlocks.COINPILE_GOLD); }

	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.atm.interact"); }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.accountWidget = this.addChild(BankInteractionWidget.builder()
				.position(screenArea.pos.offset(this.menu.halfBonusWidth,0))
				.handler(this)
				.spacing(12)
				.blockEmptyDeposits()
				.build());

	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		Component accountName = Component.literal("ERROR FINDING ACCOUNT");
		IBankAccount ba = this.screen.getMenu().getBankAccount();
		if(ba != null)
			accountName = ba.getName();
		TextRenderUtil.drawCenteredText(gui, TextRenderUtil.fitString(accountName,this.screen.getXSize() - 12), this.screen.getXSize() / 2, MoneyValueWidget.HEIGHT + 1, 0x404040);
	}

	@Override
	public IBankAccount getBankAccount() { return this.screen.getMenu().getBankAccount(); }

	@Override
	public IMoneyViewer getCoinAccess() { return this.screen.getMenu().getCoinInputHandler(); }

}