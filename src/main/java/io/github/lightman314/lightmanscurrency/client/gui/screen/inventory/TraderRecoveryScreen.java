package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.menus.TraderRecoveryMenu;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.emergencyejection.CPacketChangeSelectedData;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class TraderRecoveryScreen extends ContainerScreen<TraderRecoveryMenu> {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
	
	public TraderRecoveryScreen(TraderRecoveryMenu menu, PlayerInventory inventory, ITextComponent title) {
		super(menu, inventory, title);
		this.imageHeight = 222;
		this.imageWidth = 176;
	}
	
	Button buttonLeft;
	Button buttonRight;
	
	@Override
	protected void init() {
		super.init();
		
		this.buttonLeft = this.addButton(new IconButton(this.leftPos - 20, this.topPos, b -> this.changeSelection(-1), IconAndButtonUtil.ICON_LEFT));
		this.buttonRight = this.addButton(new IconButton(this.leftPos + this.imageWidth, this.topPos, b -> this.changeSelection(1), IconAndButtonUtil.ICON_RIGHT));
		
	}
	
	@Override
	protected void renderBg(@Nonnull MatrixStack pose, float partialTicks, int mouseX, int mouseY) {
		
		RenderUtil.bindTexture(GUI_TEXTURE);
		RenderUtil.color4f(1f, 1f, 1f, 1f);
		
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
	}
	
	private IFormattableTextComponent getTraderTitle() {
		EjectionData data = this.menu.getSelectedData();
		if(data != null)
			return data.getTraderName();
		return EasyText.translatable("gui.lightmanscurrency.trader_recovery.nodata");
	}
	
	@Override
	protected void renderLabels(@Nonnull MatrixStack pose, int mouseX, int mouseY) {
		this.font.draw(pose, this.getTraderTitle(), this.titleLabelX, this.titleLabelY, 0x404040);
		this.font.draw(pose, this.inventory.getName(), this.inventoryLabelX, this.imageHeight - 94, 0x404040);
	}
	
	@Override
	public void render(@Nonnull MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTicks);
		this.renderTooltip(pose, mouseX, mouseY);
		
	}
	
	@Override
	public void tick() {

		super.tick();

		this.buttonLeft.active = this.menu.getSelectedIndex() > 0;
		this.buttonRight.active = this.menu.getSelectedIndex() < this.menu.getValidEjectionData().size() - 1;
		
	}
	
	private void changeSelection(int delta) {
		int newSelection = this.menu.getSelectedIndex() + delta;
		LightmansCurrencyPacketHandler.instance.sendToServer(new CPacketChangeSelectedData(newSelection));
	}

}
