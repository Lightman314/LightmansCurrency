package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import org.anti_ad.mc.ipn.api.IPNIgnore;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.ticket_machine.MessageCraftTicket;
import io.github.lightman314.lightmanscurrency.common.menus.TicketMachineMenu;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

@IPNIgnore
public class TicketMachineScreen extends AbstractContainerScreen<TicketMachineMenu>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/ticket_machine.png");
	
	private Button buttonCraft;
	
	public TicketMachineScreen(TicketMachineMenu container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		this.imageHeight = 138;
		this.imageWidth = 176;
	}
	
	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
	{
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
	}
	
	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
	{
		this.font.draw(poseStack, this.title, 8.0f, 6.0f, 0x404040);
		this.font.draw(poseStack, this.playerInventoryTitle, 8.0f, (this.imageHeight - 94), 0x404040);
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		this.buttonCraft = this.addRenderableWidget(new PlainButton(this.leftPos + 79, this.topPos + 21, 24, 16, this::craftTicket, GUI_TEXTURE, this.imageWidth, 0));
		this.buttonCraft.visible = false;
		
	}
	
	@Override
	public void containerTick()
	{
		
		this.buttonCraft.visible = this.menu.validInputs() && this.menu.roomForOutput();
		
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX,  mouseY);
		
		if(this.buttonCraft != null && this.buttonCraft.active && this.buttonCraft.isMouseOver(mouseX, mouseY))
		{
			if(this.menu.hasMasterTicket())
				this.renderTooltip(matrixStack, new TranslatableComponent("gui.button.lightmanscurrency.craft_ticket"), mouseX, mouseY);
			else
				this.renderTooltip(matrixStack, new TranslatableComponent("gui.button.lightmanscurrency.craft_master_ticket"), mouseX, mouseY);
		}
		
	}
	
	private void craftTicket(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCraftTicket(Screen.hasShiftDown()));
	}
	
}
