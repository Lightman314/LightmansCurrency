package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget.IBankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;

public class InteractionTab extends ATMTab implements IBankAccountWidget{

	public InteractionTab(ATMScreen screen) { super(screen); }
	
	BankAccountWidget accountWidget;
	
	@Override
	public IconData getIcon() { return IconData.of(ModBlocks.COINPILE_GOLD); }

	@Override
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.atm.interact"); }

	@Override
	public void init() {
		
		this.accountWidget = new BankAccountWidget(this.screen.getGuiTop() - CoinValueInput.HEIGHT, this, 20);
		
	}

	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		Component accountName = new TextComponent("");
		if(this.screen.getMenu().getAccount() != null)
			accountName = this.screen.getMenu().getAccount().getName();
		this.screen.getFont().draw(pose, accountName.getString(), this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + 6f, 0x404040);
		this.accountWidget.renderInfo(pose);
	}

	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY) { }

	@Override
	public void tick() { this.accountWidget.tick(); }
	
	@Override
	public void onClose() { this.accountWidget = null; }

	@Override
	public <T extends GuiEventListener & Widget & NarratableEntry> T addCustomWidget(T button) {
		if(button instanceof AbstractWidget)
			this.screen.addRenderableTabWidget((AbstractWidget)button);
		return button;
	}

	@Override
	public Font getFont() {
		return this.screen.getFont();
	}

	@Override
	public Screen getScreen() {
		return this.screen;
	}

	@Override
	public BankAccount getAccount() {
		return this.screen.getMenu().getAccount();
	}

	@Override
	public Container getCoinAccess() {
		return this.screen.getMenu().getCoinInput();
	}

}
