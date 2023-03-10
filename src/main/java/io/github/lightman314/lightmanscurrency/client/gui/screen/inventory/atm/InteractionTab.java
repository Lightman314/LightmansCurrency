package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget.IBankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class InteractionTab extends ATMTab implements IBankAccountWidget{

	public InteractionTab(ATMScreen screen) { super(screen); }
	
	BankAccountWidget accountWidget;
	
	@Nonnull
    @Override
	public IconData getIcon() { return IconData.of(ModBlocks.COINPILE_GOLD.get()); }

	@Override
	public IFormattableTextComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.atm.interact"); }

	@Override
	public void init() {
		
		this.accountWidget = new BankAccountWidget(this.screen.getGuiTop(), this, 14);
		this.accountWidget.getAmountSelection().drawBG = false;
		
	}

	@Override
	public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		ITextComponent accountName = EasyText.literal("ERROR FINDING ACCOUNT");
		if(this.screen.getMenu().getBankAccount() != null)
			accountName = this.screen.getMenu().getBankAccount().getName();
		this.screen.getFont().draw(pose, accountName, this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + 6f + CoinValueInput.HEIGHT, 0x404040);
		this.accountWidget.renderInfo(pose);
	}

	@Override
	public void postRender(MatrixStack pose, int mouseX, int mouseY) { }

	@Override
	public void tick() { this.accountWidget.tick(); }
	
	@Override
	public void onClose() { this.accountWidget = null; }

	@Override
	public <T extends Widget> T addCustomWidget(T button) {
		this.screen.addRenderableTabWidget(button);
		return button;
	}

	@Override
	public FontRenderer getFont() {
		return this.screen.getFont();
	}

	@Override
	public Screen getScreen() {
		return this.screen;
	}

	@Override
	public BankAccount getBankAccount() {
		return this.screen.getMenu().getBankAccount();
	}

	@Override
	public IInventory getCoinAccess() {
		return this.screen.getMenu().getCoinInput();
	}

}
