package io.github.lightman314.lightmanscurrency.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageSetCustomName;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.ItemTradeData;

public class TraderNameScreen extends Screen{
	
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/tradername.png");
	
	private int xSize = 176;
	private int ySize = 64;
	
	Player player;
	TraderBlockEntity blockEntity;
	
	EditBox nameField;
	
	Button saveButton;
	
	public TraderNameScreen(TraderBlockEntity tileEntity, Player player)
	{
		super(new TranslatableComponent("gui.lightmanscurrency.changename"));
		this.blockEntity = tileEntity;
		this.player = player;
	}
	
	@Override
	protected void init()
	{
		
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		
		
		this.nameField = this.addRenderableWidget(new EditBox(this.font, guiLeft + 8, guiTop + 14, 160, 18, TextComponent.EMPTY));
		if(this.blockEntity.hasCustomName())
			this.nameField.setValue(this.blockEntity.getName().getString());
		this.nameField.setMaxLength(ItemTradeData.MAX_CUSTOMNAME_LENGTH);
		//this.children.add(this.nameField);
		
		this.saveButton = this.addRenderableWidget(new Button(guiLeft + 7, guiTop + 38, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.save"), this::SaveChanges));
		this.saveButton.active = false;
		this.addRenderableWidget(new Button(guiLeft + 120, guiTop + 38, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.back"), this::Back));
		
	}
	
	@Override
	public void tick()
	{
		if(this.blockEntity.isRemoved())
		{
			if(this.player instanceof LocalPlayer)
				((LocalPlayer)this.player).clientSideCloseContainer();
			return;
		}
		super.tick();
		String input = this.nameField.getValue();
		this.saveButton.active = input.length() > 0 && input != this.blockEntity.getName().getString();
		this.nameField.tick();
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		
		//RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		//this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		int startX = (this.width - this.xSize) / 2;
		int startY = (this.height - this.ySize) / 2;
		this.blit(matrixStack, startX, startY, 0, 0, this.xSize, this.ySize);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		this.font.draw(matrixStack, new TranslatableComponent("gui.lightmanscurrency.customname").getString(), startX + 8.0F, startY + 4.0F, 0x404040);
		
		this.nameField.render(matrixStack, mouseX, mouseY, partialTicks);
		
	}
	
	protected void SaveChanges(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetCustomName(this.blockEntity.getBlockPos(), this.nameField.getValue()));
		Back(button);
	}
	
	protected void Back(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(blockEntity.getBlockPos()));
	}

}
