package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletBankScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget.IBankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import org.jetbrains.annotations.NotNull;

public class InteractionTab extends WalletBankTab implements IBankAccountWidget {

	public InteractionTab(WalletBankScreen screen) { super(screen); }

	BankAccountWidget accountWidget;
	
	@Override
	public @NotNull IconData getIcon() { return IconData.of(ModBlocks.COINPILE_GOLD); }

	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.atm.interact"); }

	@Override
	public void init() {
		
		this.accountWidget = new BankAccountWidget(this.screen.getGuiTop(), this, 7);
		this.accountWidget.allowEmptyDeposits = false;
		this.accountWidget.getAmountSelection().drawBG = false;
		
	}

	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		Component accountName = Component.literal("ERROR FINDING ACCOUNT");
		if(this.screen.getMenu().getBankAccount() != null)
			accountName = this.screen.getMenu().getBankAccount().getName();
		this.screen.getFont().draw(pose, accountName, this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + CoinValueInput.HEIGHT, 0x404040);
		this.accountWidget.renderInfo(pose);
	}

	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY) { }

	@Override
	public void tick() { this.accountWidget.tick(); }
	
	@Override
	public void onClose() { this.accountWidget = null; }

	@Override
	public <T extends GuiEventListener & Renderable & NarratableEntry> T addCustomWidget(T button) {
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
	public BankAccount getBankAccount() {
		return this.screen.getMenu().getBankAccount();
	}

	@Override
	public Container getCoinAccess() {
		return this.screen.getMenu().getCoinInput();
	}

}
