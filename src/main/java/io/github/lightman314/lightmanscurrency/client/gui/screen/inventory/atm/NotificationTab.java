package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public class NotificationTab extends ATMTab {

	public NotificationTab(ATMScreen screen) { super(screen); }
	
	CoinValueInput notificationSelection;
	
	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(Items.ENDER_PEARL); }

	@Override
	public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.atm.notification"); }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		SimpleSlot.SetInactive(this.screen.getMenu());
		
		Component accountName = this.screen.getMenu().getPlayer().getDisplayName();
		if(this.screen.getMenu().getBankAccount() != null)
			accountName = this.screen.getMenu().getBankAccount() .getName();
		this.notificationSelection = this.addChild(new CoinValueInput(screenArea.x, screenArea.y, accountName, this.screen.getMenu().getBankAccount().getNotificationValue(), this.screen.getFont(), this::onValueChanged));
		this.notificationSelection.drawBG = false;
		this.notificationSelection.allowFreeToggle = false;
		
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		this.hideCoinSlots(gui);
		
		BankAccount account = this.screen.getMenu().getBankAccount();
		if(account != null)
			TextRenderUtil.drawCenteredMultilineText(gui, account.getNotificationLevel() > 0 ? EasyText.translatable("gui.lightmanscurrency.notification.details", account.getNotificationValue().getString()) : EasyText.translatable("gui.lightmanscurrency.notification.disabled"), 5, this.screen.getXSize() - 10, 70, 0x404040);
		
	}
	
	@Override
	protected void closeAction() { SimpleSlot.SetActive(this.screen.getMenu()); }
	
	public void onValueChanged(CoinValue value) { this.screen.getMenu().SetNotificationValueAndUpdate(value); }

}
