package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.notifications.NotificationDisplayWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public class LogTab extends ATMTab{

	public LogTab(ATMScreen screen) { super(screen); }
	
	NotificationDisplayWidget logWidget;
	
	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(Items.WRITABLE_BOOK); }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_ATM_LOGS.get(); }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		SimpleSlot.SetInactive(this.screen.getMenu());
		
		this.logWidget = this.addChild(new NotificationDisplayWidget(screenArea.x + 7, screenArea.y + 15, screenArea.width - 14, 6, this::getNotifications));
		this.logWidget.backgroundColor = 0;
		
	}
	
	private List<Notification> getNotifications() {
		IBankAccount ba = this.screen.getMenu().getBankAccount();
		if(ba != null)
			return ba.getNotifications();
		return new ArrayList<>();
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		this.hideCoinSlots(gui);
		gui.drawString(this.getTooltip(), 8, 6, 0x404040);
	}

	@Override
	protected void closeAction() { SimpleSlot.SetActive(this.screen.getMenu()); }

}
