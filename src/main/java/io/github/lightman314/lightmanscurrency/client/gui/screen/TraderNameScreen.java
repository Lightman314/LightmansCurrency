package io.github.lightman314.lightmanscurrency.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageSetCustomName;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class TraderNameScreen extends Screen{
	
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/tradername.png");
	
	private int xSize = 176;
	private int ySize = 64;
	
	Player player;
	TraderTileEntity tileEntity;
	
	EditBox nameField;
	
	Button saveButton;
	
	public TraderNameScreen(TraderTileEntity tileEntity, Player player)
	{
		super(new TranslatableComponent("gui.lightmanscurrency.changename"));
		this.tileEntity = tileEntity;
		this.player = player;
	}
	
	@Override
	protected void init()
	{
		
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		
		
		this.nameField = this.addRenderableWidget(new EditBox(this.font, guiLeft + 8, guiTop + 14, 160, 18, new TextComponent("")));
		if(this.tileEntity.hasCustomName())
			this.nameField.setValue(this.tileEntity.getName().getString());
		this.nameField.setMaxLength(ItemTradeData.MAX_CUSTOMNAME_LENGTH);
		
		this.saveButton = this.addRenderableWidget(new Button(guiLeft + 7, guiTop + 38, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.save"), this::SaveChanges));
		this.saveButton.active = false;
		this.addRenderableWidget(new Button(guiLeft + 120, guiTop + 38, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.back"), this::Back));
		
	}
	
	@Override
	public void tick()
	{
		if(this.tileEntity.isRemoved())
		{
			Minecraft.getInstance().setScreen(null);
			return;
		}
		super.tick();
		String input = this.nameField.getValue();
		this.saveButton.active = input.length() > 0 && input != this.tileEntity.getName().getString();
		this.nameField.tick();
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(poseStack);
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		int startX = (this.width - this.xSize) / 2;
		int startY = (this.height - this.ySize) / 2;
		this.blit(poseStack, startX, startY, 0, 0, this.xSize, this.ySize);
		
		super.render(poseStack, mouseX, mouseY, partialTicks);
		
		this.font.draw(poseStack, new TranslatableComponent("gui.lightmanscurrency.customname").getString(), startX + 8.0F, startY + 4.0F, 0x404040);
		
	}
	
	protected void SaveChanges(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetCustomName(this.tileEntity.getBlockPos(), this.nameField.getValue()));
		Back(button);
	}
	
	protected void Back(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(tileEntity.getBlockPos()));
	}

}
