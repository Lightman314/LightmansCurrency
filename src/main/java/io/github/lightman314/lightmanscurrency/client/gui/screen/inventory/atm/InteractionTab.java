package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.MoneyContainer;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget.IBankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class InteractionTab extends ATMTab implements IBankAccountWidget{

	public InteractionTab(ATMScreen screen) { super(screen); }
	
	BankAccountWidget accountWidget;
	
	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(ModBlocks.COINPILE_GOLD); }

	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.atm.interact"); }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.accountWidget = this.addChild(new BankAccountWidget(screenArea.y, this, 14, this::addChild));
		this.accountWidget.getAmountSelection().drawBG = false;
		
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		Component accountName = EasyText.literal("ERROR FINDING ACCOUNT");
		IBankAccount account = this.getBankAccount();
		if(account != null)
			accountName = account.getName();
		gui.drawString(accountName, 8, 6 + MoneyValueWidget.HEIGHT, 0x404040);
		this.accountWidget.renderInfo(gui);
	}

	@Override
	public Screen getScreen() { return this.screen; }

	@Override
	public IBankAccount getBankAccount() { return this.screen.getMenu().getBankAccount(); }

	@Override
	public MoneyContainer getCoinAccess() { return this.screen.getMenu().getCoinInput(); }

}
