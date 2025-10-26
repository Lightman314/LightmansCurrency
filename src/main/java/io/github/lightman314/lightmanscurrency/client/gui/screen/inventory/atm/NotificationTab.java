package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class NotificationTab extends ATMTab {

	public NotificationTab(ATMScreen screen) { super(screen); }
	
	MoneyValueWidget notificationSelection;

	EasyButton buttonResetATMCards;
	
	@Nonnull
	@Override
	public IconData getIcon() { return ItemIcon.ofItem(Items.ENDER_PEARL); }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_ATM_NOTIFICATIONS.get(); }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.screen.setCoinSlotsActive(false);

		this.notificationSelection = this.addChild(MoneyValueWidget.builder()
				.position(screenArea.pos)
				.oldIfNotFirst(firstOpen,this.notificationSelection)
				.valueHandler(this::onValueChanged)
				.typeChangeListener(this::onValueTypeChanged)
				.blockFreeInputs()
				.build());
		//Manually trigger type change to set the widgets current value as appropriate
		this.onValueTypeChanged(this.notificationSelection);

		//Reset Button to tweak card validation
		this.buttonResetATMCards = this.addChild(EasyTextButton.builder()
				.position(screenArea.pos.offset(20,117))
				.width(screenArea.width - 40)
				.text(LCText.BUTTON_BANK_CARD_VERIFCATION_RESET)
				.pressAction(this::resetCardVerification)
				.addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_BANK_CARD_VERIFCATION_RESET, TooltipHelper.DEFAULT_TOOLTIP_WIDTH))
				.build());

	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		IBankAccount account = this.screen.getMenu().getBankAccount();
		if(account != null)
			TextRenderUtil.drawCenteredMultilineText(gui, this.getRandomNotificationLevelText(), 5, this.screen.getXSize() - 10, 70, 0x404040);
		
	}

	@Nonnull
	private MutableComponent getRandomNotificationLevelText()
	{
		IBankAccount account = this.screen.getMenu().getBankAccount();
		if(account != null)
		{
			Map<String,MoneyValue> limits = account.getNotificationLevels();
			if(limits.isEmpty())
				return LCText.GUI_BANK_NOTIFICATIONS_DISABLED.get();
			List<MoneyValue> values = limits.values().stream().toList();
			int displayIndex = (int)(TimeUtil.getCurrentTime() / 2000 % values.size());
			return LCText.GUI_BANK_NOTIFICATIONS_DETAILS.get(values.get(displayIndex).getText());
		}
		return EasyText.literal("ERROR!");

	}
	
	@Override
	protected void closeAction() { this.screen.setCoinSlotsActive(true); }
	
	public void onValueChanged(MoneyValue value) {
		if(this.notificationSelection == null)
			return;
		if(value.isEmpty() || value.isFree())
		{
			String type = this.notificationSelection.getCurrentHandlerType();
			this.screen.getMenu().SetNotificationValueAndUpdate(type, MoneyValue.empty());
		}
		else
			this.screen.getMenu().SetNotificationValueAndUpdate(value.getUniqueName(), value);
	}

	public void onValueTypeChanged(@Nonnull MoneyValueWidget widget)
	{
		IBankAccount account = this.screen.getMenu().getBankAccount();
		if(account != null)
		{
			String type = widget.getCurrentHandlerType();
			MoneyValue currentValue = account.getNotificationLevelFor(type);
			widget.changeValue(currentValue);
		}
	}

	private void resetCardVerification() { this.menu.SendMessage(this.builder().setFlag("ResetCards")); }

}
