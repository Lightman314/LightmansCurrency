package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.anti_ad.mc.ipn.api.IPNIgnore;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenu;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageOpenWalletBank;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageWalletConvertCoins;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageWalletQuickCollect;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageWalletToggleAutoConvert;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

import javax.annotation.Nonnull;

@IPNIgnore
public class WalletScreen extends ContainerScreen<WalletMenu> {

	private final int BASEHEIGHT = 114;
	
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/wallet.png");
	
	IconButton buttonToggleAutoConvert;
	Button buttonConvert;
	boolean autoConvert = false;
	
	Button buttonOpenBank;
	
	Button buttonQuickCollect;
	
	public WalletScreen(WalletMenu container, PlayerInventory inventory, ITextComponent title) { super(container, inventory, title); }
	
	@Override
	protected void renderBg(@Nonnull MatrixStack pose, float partialTicks, int mouseX, int mouseY)
	{
		
		RenderUtil.bindTexture(GUI_TEXTURE);
		RenderUtil.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		//Draw the top
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, 17);
		//Draw the middle strips
		for(int y = 0; y < this.menu.getRowCount(); y++)
		{
			this.blit(pose, this.leftPos, this.topPos + 17 + y * 18, 0, 17, this.imageWidth, 18);
		}
		//Draw the bottom
		this.blit(pose, this.leftPos, this.topPos + 17 + this.menu.getRowCount() * 18, 0, 35, this.imageWidth, BASEHEIGHT - 17);
		
		//Draw the slots
		for(int y = 0; y * 9 < this.menu.getSlotCount(); y++)
		{
			for(int x = 0; x < 9 && x + y * 9 < this.menu.getSlotCount(); x++)
			{
				this.blit(pose, this.leftPos + 7 + x * 18, this.topPos + 17 + y * 18, 0, BASEHEIGHT + 18, 18, 18);
			}
		}
		
	}
	
	private ITextComponent getWalletName() {
		ItemStack wallet = this.menu.getWallet();
		return wallet.isEmpty() ? EasyText.empty() : wallet.getHoverName();
	}
	
	@Override
	protected void renderLabels(@Nonnull MatrixStack pose, int mouseX, int mouseY)
	{
		this.font.draw(pose, this.getWalletName(), 8.0f, 6.0f, 0x404040);
		this.font.draw(pose, this.inventory.getName(), 8.0f, (this.imageHeight - 94), 0x404040);
	}
	
	@Override
	protected void init()
	{
		
		this.imageHeight = BASEHEIGHT + this.menu.getRowCount() * 18;
		this.imageWidth = 176;
		
		super.init();
		
		this.buttons.clear();
		this.children.clear();
		this.buttonConvert = null;
		this.buttonToggleAutoConvert = null;
		
		int buttonPosition = this.topPos;
		
		if(this.menu.canConvert())
		{
			//Create the buttons
			this.buttonConvert = this.addButton(new IconButton(this.leftPos - 20, buttonPosition, this::PressConvertButton, IconData.of(GUI_TEXTURE, this.imageWidth, 0)));
			buttonPosition += 20;
			
			if(this.menu.canPickup())
			{
				this.buttonToggleAutoConvert = this.addButton(new IconButton(this.leftPos - 20, buttonPosition, this::PressAutoConvertToggleButton, IconData.of(GUI_TEXTURE, this.imageWidth, 16)));
				buttonPosition += 20;
				this.updateToggleButton();
			}
		}
		
		if(this.menu.hasBankAccess())
		{
			this.buttonOpenBank = this.addButton(new IconButton(this.leftPos - 20, buttonPosition, this::PressOpenBankButton, IconData.of(ModBlocks.MACHINE_ATM.get().asItem())));
		}
		
		this.buttonQuickCollect = this.addButton(new PlainButton(this.leftPos + 159, this.topPos + this.imageHeight - 95, 10, 10, this::PressQuickCollectButton, GUI_TEXTURE, this.imageWidth + 16, 0));
		
	}
	
	@Override
	public void tick()
	{

		super.tick();
		
		if(this.buttonToggleAutoConvert != null)
		{
			//CurrencyMod.LOGGER.info("Local AC: " + this.autoConvert + " Stack AC: " + this.container.getAutoConvert());
			if(this.menu.getAutoConvert() != this.autoConvert)
				this.updateToggleButton();
		}
		
	}
	
	private void updateToggleButton()
	{
		//CurrencyMod.LOGGER.info("Updating AutoConvert Button");
		this.autoConvert = this.menu.getAutoConvert();
		this.buttonToggleAutoConvert.setIcon(IconData.of(GUI_TEXTURE, this.imageWidth, this.autoConvert ? 16 : 32));
	}
	
	@Override
	public void render(@Nonnull MatrixStack pose, int mouseX, int mouseY, float partialTicks)
	{
		
		this.renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTicks);
		this.renderTooltip(pose, mouseX,  mouseY);
		
		if(this.buttonConvert != null && this.buttonConvert.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, EasyText.translatable("tooltip.lightmanscurrency.wallet.convert"), mouseX, mouseY);
		}
		else if(this.buttonToggleAutoConvert != null && this.buttonToggleAutoConvert.isMouseOver(mouseX, mouseY))
		{
			if(this.autoConvert)
				this.renderTooltip(pose, EasyText.translatable("tooltip.lightmanscurrency.wallet.autoconvert.disable"), mouseX, mouseY);
			else
				this.renderTooltip(pose, EasyText.translatable("tooltip.lightmanscurrency.wallet.autoconvert.enable"), mouseX, mouseY);
		}
		else if(this.buttonOpenBank != null && this.buttonOpenBank.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, EasyText.translatable("tooltip.lightmanscurrency.wallet.openbank"), mouseX, mouseY);
		}
	}
	
	private void PressConvertButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageWalletConvertCoins());
	}
	
	private void PressAutoConvertToggleButton(Button button)
	{
		this.menu.ToggleAutoConvert();
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageWalletToggleAutoConvert());
	}
	
	private void PressOpenBankButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenWalletBank(this.menu.getWalletStackIndex()));
	}
	
	private void PressQuickCollectButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageWalletQuickCollect());
	}
	
}
