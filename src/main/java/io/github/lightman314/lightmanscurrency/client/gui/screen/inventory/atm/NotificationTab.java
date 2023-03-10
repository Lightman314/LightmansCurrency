package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageSetBankNotificationLevel;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class NotificationTab extends ATMTab {

	public NotificationTab(ATMScreen screen) { super(screen); }
	
	CoinValueInput notificationSelection;
	
	@Nonnull
    @Override
	public IconData getIcon() { return IconData.of(Items.ENDER_PEARL); }

	@Override
	public ITextComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.atm.notification"); }

	@Override
	public void init() {
		
		SimpleSlot.SetInactive(this.screen.getMenu());
		
		ITextComponent accountName = this.screen.getMenu().getPlayer().getDisplayName();
		if(this.screen.getMenu().getBankAccount() != null)
			accountName = this.screen.getMenu().getBankAccount() .getName();
		this.notificationSelection = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiLeft(), this.screen.getGuiTop(), accountName, this.screen.getMenu().getBankAccount().getNotificationValue(), this.screen.getFont(), this::onValueChanged, this.screen::addRenderableTabWidget));
		this.notificationSelection.drawBG = false;
		this.notificationSelection.allowFreeToggle = false;
		this.notificationSelection.init();
		
	}

	@Override
	public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.hideCoinSlots(pose);
		
		BankAccount account = this.screen.getMenu().getBankAccount();
		if(account != null)
			TextRenderUtil.drawCenteredMultilineText(pose, account.getNotificationLevel() > 0 ? EasyText.translatable("gui.lightmanscurrency.notification.details", account.getNotificationValue().getString()) : EasyText.translatable("gui.lightmanscurrency.notification.disabled"), this.screen.getGuiLeft() + 5, this.screen.getXSize() - 10, this.screen.getGuiTop() + 70, 0x404040);
		
	}

	@Override
	public void postRender(MatrixStack pose, int mouseX, int mouseY) { }

	@Override
	public void tick() { this.notificationSelection.tick(); }
	
	@Override
	public void onClose() {
		SimpleSlot.SetActive(this.screen.getMenu());
	}
	
	public void onValueChanged(CoinValue value) {
		this.screen.getMenu().getBankAccount().setNotificationValue(value);
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetBankNotificationLevel(value));
	}

}
