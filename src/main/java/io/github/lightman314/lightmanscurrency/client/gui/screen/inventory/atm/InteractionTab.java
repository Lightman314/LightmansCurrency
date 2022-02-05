package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget.IBankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class InteractionTab extends ATMTab implements IBankAccountWidget{

	public InteractionTab(ATMScreen screen) { super(screen); }
	
	BankAccountWidget accountWidget;
	
	@Override
	public IconData getIcon() { return IconData.of(ModBlocks.COINPILE_GOLD); }

	@Override
	public ITextComponent getTooltip() { return new TranslationTextComponent("tooltip.lightmanscurrency.atm.interact"); }

	@Override
	public void init() {
		
		this.accountWidget = new BankAccountWidget(this.screen.getGuiTop() - CoinValueInput.HEIGHT, this, 20);
		
	}

	@Override
	public void preRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		this.accountWidget.renderCoinValueWidget(matrix, mouseX, mouseY, partialTicks);
		this.accountWidget.renderInfo(matrix);
	}

	@Override
	public void postRender(MatrixStack matrix, int mouseX, int mouseY) { }

	@Override
	public void tick() { this.accountWidget.tick(); }
	
	@Override
	public void onClose() { this.accountWidget = null; }

	@Override
	public <T extends Button> T addCustomWidget(T widget) {
		return this.screen.addRenderableTabWidget(widget);
	}
	
	@Override
	public <T extends IGuiEventListener> T addCustomListener(T widget) {
		return this.screen.addTabListener(widget);
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
	public BankAccount getAccount() {
		return this.screen.getContainer().getAccount();
	}

	@Override
	public IInventory getCoinAccess() {
		return this.screen.getContainer().getCoinInput();
	}

	@Override
	public int getWidth() {
		return this.screen.width;
	}

}
