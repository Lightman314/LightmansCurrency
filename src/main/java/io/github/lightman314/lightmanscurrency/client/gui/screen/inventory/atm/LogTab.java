package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.notifications.NotificationDisplayWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class LogTab extends ATMTab{

	public LogTab(ATMScreen screen) { super(screen); }

	NotificationDisplayWidget logWidget;

	@Nonnull
	@Override
	public IconData getIcon() { return IconUtil.ICON_SHOW_LOGGER; }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_ATM_LOGS.get(); }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.screen.setCoinSlotsActive(false);

		this.logWidget = this.addChild(NotificationDisplayWidget.builder()
				.position(screenArea.pos.offset(15,15))
				.width(screenArea.width - 30)
				.rowCount(6)
				.notificationSource(this::getNotifications)
				.build());

		this.addChild(ScrollBarWidget.builder()
				.onRight(this.logWidget)
				.build());

	}

	private List<Notification> getNotifications() {
		IBankAccount ba = this.screen.getMenu().getBankAccount();
		if(ba != null)
			return ba.getNotifications();
		return new ArrayList<>();
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) { gui.drawString(this.getTooltip(), 8, 6, 0x404040); }

	@Override
	protected void closeAction() { this.screen.setCoinSlotsActive(true); }

}