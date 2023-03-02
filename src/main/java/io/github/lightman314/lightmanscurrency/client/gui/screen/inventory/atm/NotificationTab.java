package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageSetBankNotificationLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;

public class NotificationTab extends ATMTab {

	public NotificationTab(ATMScreen screen) { super(screen); }
	
	CoinValueInput notificationSelection;
	
	@Override
	public IconData getIcon() { return IconData.of(Items.ENDER_PEARL); }

	@Override
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.atm.notification"); }

	@Override
	public void init() {
		
		SimpleSlot.SetInactive(this.screen.getMenu());
		
		Component accountName = this.screen.getMenu().getPlayer().getDisplayName();
		if(this.screen.getMenu().getBankAccount() != null)
			accountName = this.screen.getMenu().getBankAccount() .getName();
		this.notificationSelection = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiLeft(), this.screen.getGuiTop(), accountName, this.screen.getMenu().getBankAccount().getNotificationValue(), this.screen.getFont(), this::onValueChanged, this.screen::addRenderableTabWidget));
		this.notificationSelection.drawBG = false;
		this.notificationSelection.allowFreeToggle = false;
		this.notificationSelection.init();
		
	}

	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.hideCoinSlots(pose);
		
		BankAccount account = this.screen.getMenu().getBankAccount();
		if(account != null)
			TextRenderUtil.drawCenteredMultilineText(pose, account.getNotificationLevel() > 0 ? new TranslatableComponent("gui.lightmanscurrency.notification.details", account.getNotificationValue().getString()) : new TranslatableComponent("gui.lightmanscurrency.notification.disabled"), this.screen.getGuiLeft() + 5, this.screen.getXSize() - 10, this.screen.getGuiTop() + 70, 0x404040);
		
	}

	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY) { }

	@Override
	public void tick() { this.notificationSelection.tick();; }
	
	@Override
	public void onClose() {
		SimpleSlot.SetActive(this.screen.getMenu());
	}
	
	public void onValueChanged(CoinValue value) {
		this.screen.getMenu().getBankAccount().setNotificationValue(value);
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetBankNotificationLevel(value));
	}

}
