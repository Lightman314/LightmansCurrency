package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class NotificationTab extends ATMTab {

	public NotificationTab(ATMScreen screen) { super(screen); }
	
	MoneyValueWidget notificationSelection;
	
	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(Items.ENDER_PEARL); }

	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.atm.notification"); }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		SimpleSlot.SetInactive(this.screen.getMenu());

		this.notificationSelection = this.addChild(new MoneyValueWidget(screenArea.x, screenArea.y, this.notificationSelection, MoneyValue.empty(), this::onValueChanged));
		this.notificationSelection.drawBG = false;
		this.notificationSelection.allowFreeInput = false;
		//
		this.onValueTypeChanged(this.notificationSelection);
		//Set change listener to update input value
		this.notificationSelection.setHandlerChangeListener(this::onValueTypeChanged);

		
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		this.hideCoinSlots(gui);

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
				return EasyText.translatable("gui.lightmanscurrency.notification.disabled");
			List<MoneyValue> values = limits.values().stream().toList();
			int displayIndex = (int)(TimeUtil.getCurrentTime() / 2000 % values.size());
			return EasyText.translatable("gui.lightmanscurrency.notification.details", values.get(displayIndex).getText());
		}
		return EasyText.literal("ERROR!");

	}
	
	@Override
	protected void closeAction() { SimpleSlot.SetActive(this.screen.getMenu()); }
	
	public void onValueChanged(MoneyValue value) {
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

}
