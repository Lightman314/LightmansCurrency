package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class LogTab extends ATMTab{

	public LogTab(ATMScreen screen) { super(screen); }
	
	ScrollTextDisplay logWidget;
	
	@Override
	public IconData getIcon() { return IconData.of(Items.WRITABLE_BOOK); }

	@Override
	public ITextComponent getTooltip() { return new TranslationTextComponent("tooltip.lightmanscurrency.atm.log"); }

	@Override
	public void init() {
		
		this.logWidget = this.screen.addRenderableTabWidget(new ScrollTextDisplay(this.screen.getGuiLeft() + 5, this.screen.getGuiTop() + 5, this.screen.getXSize() - 10, 87, this.screen.getFont(), this::getBankLog));
		this.logWidget.invertText = true;
		
	}
	
	private List<ITextComponent> getBankLog() {
		BankAccount account = this.screen.getContainer().getAccount();
		if(account != null)
			return account.getLogs().logText;
		return new ArrayList<>();
	}

	@Override
	public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
	}

	@Override
	public void postRender(MatrixStack pose, int mouseX, int mouseY) { }
	
	@Override
	public void tick() { }

	@Override
	public void onClose() { }

}
