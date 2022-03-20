package io.github.lightman314.lightmanscurrency.client.gui.widget.lockableslot;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.menus.containers.LockableContainer.LockData;
import io.github.lightman314.lightmanscurrency.menus.slots.LockableSlot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class LockableSlotButton extends Button {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/lockableslot.png");
	
	private final LockableSlot slot;
	
	public LockableSlotButton(AbstractContainerScreen<?> screen, LockableSlot slot, OnPress onPress) {
		super(screen.getGuiLeft() + slot.x - 1, screen.getGuiTop() + slot.y - 19, 18, 18, new TextComponent(""), onPress);
		this.slot = slot;
	}
	
	private LockData getLockData() {
		return this.slot.getLockableContainer().getLockData(this.slot.getSlotIndex());
	}
	
	@Override
	public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
		//Draw the slot background
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		this.blit(pose, this.x, this.y, 0, 0, 18, 18);
		
		LockData data = this.getLockData();
		if(data.fullyLocked())
		{
			//Render lock icon
			this.blit(pose, this.x + 1, this.y + 1, 18, this.isHovered ? 16 : 0, 16, 16);
		}
		else if(data.hasItemFilter())
		{
			//Render filter item
			ItemRenderUtil.drawItemStack(this, null, data.filterItem(), this.x + 1, this.y + 1);
		}
	}
	
	@Override
	protected boolean isValidClickButton(int button) { return true; }
	
	public void renderTooltip(Screen screen, PoseStack pose, int mouseX, int mouseY) {
		LockData data = this.getLockData();
		if(data.fullyLocked())
			screen.renderTooltip(pose, new TranslatableComponent("tooltip.lightmanscurrency.lockableslot.locked"), mouseX, mouseY);
		else if(data.hasItemFilter())
			screen.renderTooltip(pose, new TranslatableComponent("tooltip.lightmanscurrency.lockableslot.item", data.filterItem().getHoverName()), mouseX, mouseY);
		else
			screen.renderTooltip(pose, new TranslatableComponent("tooltip.lightmanscurrency.lockableslot.unlocked"), mouseX, mouseY);
	}
	
	@Override
	public void playDownSound(SoundManager sm) { }
	
}
