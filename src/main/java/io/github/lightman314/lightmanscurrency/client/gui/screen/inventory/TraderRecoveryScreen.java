package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.menus.TraderRecoveryMenu;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.emergencyejection.CPacketChangeSelectedData;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TraderRecoveryScreen extends AbstractContainerScreen<TraderRecoveryMenu> {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
	
	public TraderRecoveryScreen(TraderRecoveryMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageHeight = 222;
		this.imageWidth = 176;
	}
	
	Button buttonLeft;
	Button buttonRight;
	
	@Override
	protected void init() {
		super.init();
		
		this.buttonLeft = this.addRenderableWidget(new IconButton(this.leftPos - 20, this.topPos, b -> this.changeSelection(-1), IconAndButtonUtil.ICON_LEFT));
		this.buttonRight = this.addRenderableWidget(new IconButton(this.leftPos + this.imageWidth, this.topPos, b -> this.changeSelection(1), IconAndButtonUtil.ICON_RIGHT));
		
	}
	
	@Override
	protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
	}
	
	private MutableComponent getTraderTitle() {
		EjectionData data = this.menu.getSelectedData();
		if(data != null)
			return data.getTraderName();
		return new TranslatableComponent("gui.lightmanscurrency.trader_recovery.nodata");
	}
	
	@Override
	protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
		this.font.draw(pose, this.getTraderTitle(), this.titleLabelX, this.titleLabelY, 0x404040);
		this.font.draw(pose, this.playerInventoryTitle, this.inventoryLabelX, this.imageHeight - 94, 0x404040);
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTicks);
		this.renderTooltip(pose, mouseX, mouseY);
		
	}
	
	@Override
	protected void containerTick() {
		
		this.buttonLeft.active = this.menu.getSelectedIndex() > 0;
		this.buttonRight.active = this.menu.getSelectedIndex() < this.menu.getValidEjectionData().size() - 2;
		
	}
	
	private void changeSelection(int delta) {
		int newSelection = this.menu.getSelectedIndex() + delta;
		LightmansCurrencyPacketHandler.instance.sendToServer(new CPacketChangeSelectedData(newSelection));
	}

}
