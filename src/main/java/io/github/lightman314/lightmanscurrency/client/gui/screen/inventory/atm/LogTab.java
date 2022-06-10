package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.menus.slots.SimpleSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

public class LogTab extends ATMTab{

	public LogTab(ATMScreen screen) { super(screen); }
	
	ScrollTextDisplay logWidget;
	
	@Override
	public IconData getIcon() { return IconData.of(Items.WRITABLE_BOOK); }

	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.atm.log"); }

	@Override
	public void init() {
		
		SimpleSlot.SetInactive(this.screen.getMenu());
		
		this.logWidget = this.screen.addRenderableTabWidget(new ScrollTextDisplay(this.screen.getGuiLeft() + 5, this.screen.getGuiTop() + 5, this.screen.getXSize() - 10, 108, this.screen.getFont(), this::getBankLog));
		this.logWidget.invertText = true;
		
	}
	
	private List<MutableComponent> getBankLog() {
		BankAccount account = this.screen.getMenu().getAccount();
		if(account != null)
			return account.getLogs().logText;
		return new ArrayList<>();
	}

	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		RenderSystem.setShaderTexture(0, ATMScreen.GUI_TEXTURE);
		this.screen.blit(pose, this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 97, 7, 79, 162, 18);
		
	}

	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY) { }
	
	@Override
	public void tick() { }

	@Override
	public void onClose() {
		SimpleSlot.SetActive(this.screen.getMenu());
	}

}
