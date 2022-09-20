package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.notifications.NotificationDisplayWidget;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.menus.slots.SimpleSlot;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;

public class LogTab extends ATMTab{

	public LogTab(ATMScreen screen) { super(screen); }
	
	NotificationDisplayWidget logWidget;
	
	@Override
	public IconData getIcon() { return IconData.of(Items.WRITABLE_BOOK); }

	@Override
	public MutableComponent getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.atm.log"); }

	@Override
	public void init() {
		
		SimpleSlot.SetInactive(this.screen.getMenu());
		
		this.logWidget = this.screen.addRenderableTabWidget(new NotificationDisplayWidget(this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 15, this.screen.getXSize() - 14, 6, this.screen.getFont(), this::getNotifications));
		this.logWidget.backgroundColor = 0;
		
	}
	
	private List<Notification> getNotifications() {
		BankAccount ba = this.screen.getMenu().getBankAccount();
		if(ba != null)
			return ba.getNotifications();
		return new ArrayList<>();
	}

	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		this.hideCoinSlots(pose);
		this.screen.getFont().draw(pose, this.getTooltip(), this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + 6f, 0x404040);
	}

	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY) { this.logWidget.tryRenderTooltip(pose, this.screen, mouseX, mouseY); }
	
	@Override
	public void tick() { }

	@Override
	public void onClose() {
		SimpleSlot.SetActive(this.screen.getMenu());
	}

}