package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.containers.TicketMachineContainer;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.ticket_machine.MessageCraftTicket;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;



public class TicketMachineScreen extends ContainerScreen<TicketMachineContainer>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/ticket_machine.png");
	
	private Button buttonCraft;
	
	public TicketMachineScreen(TicketMachineContainer container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		this.ySize = 138;
		this.xSize = 176;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = (this.width - this.xSize) / 2;
		int startY = (this.height - this.ySize) / 2;
		this.blit(matrix, startX, startY, 0, 0, this.xSize, this.ySize);
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		this.font.drawString(matrix, this.title.getString(), 8.0f, 6.0f, 0x404040);
		this.font.drawString(matrix, this.playerInventory.getDisplayName().getString(), 8.0f, (this.ySize - 94), 0x404040);
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		this.buttonCraft = this.addButton(new PlainButton(this.guiLeft + 79, this.guiTop + 21, 24, 16, this::craftTicket, GUI_TEXTURE, this.xSize, 0));
		//this.buttonCraft.active = false;
		this.buttonCraft.visible = false;
		
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		//this.buttonCraft.active = this.container.validInputs() && this.container.validOutputs();
		this.buttonCraft.visible = this.container.validInputs() && this.container.validOutputs();
		
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX,  mouseY);
		
		if(this.buttonCraft != null && this.buttonCraft.active && this.buttonCraft.isMouseOver(mouseX, mouseY))
		{
			if(this.container.hasMasterTicket())
				this.renderTooltip(matrixStack, new TranslationTextComponent("gui.button.lightmanscurrency.craft_ticket"), mouseX, mouseY);
			else
				this.renderTooltip(matrixStack, new TranslationTextComponent("gui.button.lightmanscurrency.craft_master_ticket"), mouseX, mouseY);
		}
		
	}
	
	private void craftTicket(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCraftTicket(Screen.hasShiftDown()));
	}
	
}
