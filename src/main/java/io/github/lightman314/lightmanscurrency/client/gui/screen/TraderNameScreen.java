package io.github.lightman314.lightmanscurrency.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageSetCustomName;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.ItemTradeData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TraderNameScreen extends Screen{
	
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/tradername.png");
	
	private int xSize = 176;
	private int ySize = 64;
	
	PlayerEntity player;
	TraderTileEntity tileEntity;
	
	TextFieldWidget nameField;
	
	Button saveButton;
	
	public TraderNameScreen(TraderTileEntity tileEntity, PlayerEntity player)
	{
		super(new TranslationTextComponent("gui.lightmanscurrency.changename"));
		this.tileEntity = tileEntity;
		this.player = player;
	}
	
	@Override
	protected void init()
	{
		
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		
		
		this.nameField = new TextFieldWidget(this.font, guiLeft + 8, guiTop + 14, 160, 18, ITextComponent.getTextComponentOrEmpty(""));
		if(this.tileEntity.hasCustomName())
			this.nameField.setText(this.tileEntity.getName().getString());
		this.nameField.setMaxStringLength(ItemTradeData.MAX_CUSTOMNAME_LENGTH);
		this.children.add(this.nameField);
		
		this.saveButton = this.addButton(new Button(guiLeft + 7, guiTop + 38, 50, 20, new TranslationTextComponent("gui.button.lightmanscurrency.save"), this::SaveChanges));
		this.saveButton.active = false;
		this.addButton(new Button(guiLeft + 120, guiTop + 38, 50, 20, new TranslationTextComponent("gui.button.lightmanscurrency.back"), this::Back));
		
	}
	
	@Override
	public void tick()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeScreen();
			return;
		}
		super.tick();
		String input = this.nameField.getText();
		this.saveButton.active = input.length() > 0 && input != this.tileEntity.getName().getString();
		this.nameField.tick();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = (this.width - this.xSize) / 2;
		int startY = (this.height - this.ySize) / 2;
		this.blit(matrixStack, startX, startY, 0, 0, this.xSize, this.ySize);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		this.font.drawString(matrixStack, new TranslationTextComponent("gui.lightmanscurrency.customname").getString(), startX + 8.0F, startY + 4.0F, 0x404040);
		
		this.nameField.render(matrixStack, mouseX, mouseY, partialTicks);
		
	}
	
	protected void SaveChanges(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetCustomName(this.tileEntity.getPos(), this.nameField.getText()));
		Back(button);
	}
	
	protected void Back(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(tileEntity.getPos()));
	}

}
