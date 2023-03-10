package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.anti_ad.mc.ipn.api.IPNIgnore;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.coinmint.MessageMintCoin;
import io.github.lightman314.lightmanscurrency.common.menus.MintMenu;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

import javax.annotation.Nonnull;

@IPNIgnore
public class MintScreen extends ContainerScreen<MintMenu> {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/coinmint.png");
	
	private Button buttonMint;
	
	public MintScreen(MintMenu container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		this.imageHeight = 138;
		this.imageWidth = 176;
	}
	
	@Override
	protected void renderBg(@Nonnull MatrixStack poseStack, float partialTicks, int mouseX, int mouseY)
	{

		RenderUtil.bindTexture(GUI_TEXTURE);
		RenderUtil.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
	}
	
	@Override
	protected void renderLabels(@Nonnull MatrixStack poseStack, int mouseX, int mouseY)
	{
		this.font.draw(poseStack, this.title, 8.0f, 6.0f, 0x404040);
		this.font.draw(poseStack, this.inventory.getName(), 8.0f, (this.imageHeight - 94), 0x404040);
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		this.buttonMint = this.addButton(new PlainButton(this.leftPos + 79, this.topPos + 21, 24, 16, this::mintCoin, GUI_TEXTURE, this.imageWidth, 0));
		this.buttonMint.visible = false;
		
		this.tick();
		
	}
	
	@Override
	public void tick()
	{

		super.tick();
		
		this.buttonMint.visible = this.menu.blockEntity.validMintInput();
		
	}
	
	@Override
	public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX,  mouseY);
		
		if(this.buttonMint != null && this.buttonMint.visible && this.buttonMint.isMouseOver(mouseX, mouseY))
		{
			if(this.menu.isMeltInput())
				this.renderTooltip(matrixStack, EasyText.translatable("gui.button.lightmanscurrency.melt"), mouseX, mouseY);
			else
				this.renderTooltip(matrixStack, EasyText.translatable("gui.button.lightmanscurrency.mint"), mouseX, mouseY);
		}
		
	}
	
	private void mintCoin(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageMintCoin(Screen.hasShiftDown(), this.menu.blockEntity.getBlockPos()));
	}
	
}
