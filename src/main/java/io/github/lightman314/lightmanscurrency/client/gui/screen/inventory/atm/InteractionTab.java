package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.bank.BankInteractionWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.bank.IBankInteractionHandler;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class InteractionTab extends ATMTab implements IBankInteractionHandler {

	public InteractionTab(ATMScreen screen) { super(screen); }

	BankInteractionWidget accountWidget;

	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(ModBlocks.COINPILE_GOLD); }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_ATM_INTERACT.get(); }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.accountWidget = this.addChild(BankInteractionWidget.builder()
				.position(screenArea.pos)
				.handler(this)
				.spacing(19)
				.build());

	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		Component accountName = EasyText.literal("ERROR FINDING ACCOUNT");
		IBankAccount account = this.getBankAccount();
		if(account != null)
			accountName = account.getName();
		TextRenderUtil.drawCenteredText(gui,TextRenderUtil.fitString(accountName,this.screen.getXSize() - 12),this.screen.getXSize() /2, 6 + MoneyValueWidget.HEIGHT, 0x404040);
	}

	@Override
	public IBankAccount getBankAccount() { return this.screen.getMenu().getBankAccount(); }

	@Override
	public IMoneyViewer getCoinAccess() { return this.screen.getMenu().getMoneyHandler(); }

}