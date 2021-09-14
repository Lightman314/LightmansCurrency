package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.containers.ATMContainer;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.atm.MessageATM;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class ATMScreen extends AbstractContainerScreen<ATMContainer>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/atm.png");
	
	private static final int LARGEBUTTON_WIDTH = 82;
	private static final int LARGEBUTTON_HEIGHT = 18;
	
	private static final int SMALLBUTTON_WIDTH = 26;
	private static final int SMALLBUTTON_HEIGHT = 18;
	
	//Large Buttons
	private Button buttonConvertAllUp;
	private Button buttonConvertAllDown;
	//Small Buttons
	//Copper & Iron
	private Button buttonConvertCopperToIron;
	private Button buttonConvertIronToCopper;
	//Iron & Gold
	private Button buttonConvertIronToGold;
	private Button buttonConvertGoldToIron;
	//Gold & Emerald
	private Button buttonConvertGoldToEmerald;
	private Button buttonConvertEmeraldToGold;
	//Emerald & Diamond
	private Button buttonConvertEmeraldToDiamond;
	private Button buttonConvertDiamondToEmerald;
	//Diamond & Netherrite
	private Button buttonConvertDiamondToNetherrite;
	private Button buttonConvertNetherriteToDiamond;
	
	public ATMScreen(ATMContainer container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		this.imageHeight = 212;
		this.imageWidth = 176;
	}
	
	@Override
	protected void renderBg(PoseStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		//this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = (this.width - this.imageWidth) / 2;
		int startY = (this.height - this.imageHeight) / 2;
		this.blit(matrix, startX, startY, 0, 0, this.imageWidth, this.imageHeight);
		
		//CoinSlot.drawEmptyCoinSlots(matrix, startX, startY, this.container, this);
		
		CoinSlot.drawEmptyCoinSlots(this, this.menu, matrix, startX, startY);
		
	}
	
	@Override
	protected void renderLabels(PoseStack matrix, int mouseX, int mouseY)
	{
		this.font.draw(matrix, this.title.getString(), 8.0f, 4.0f, 0x404040);
		this.font.draw(matrix, this.playerInventoryTitle.getString(), 8.0f, (this.imageHeight - 94), 0x404040);
	}
	
	@Override
	protected void init()
	{
		super.init();
		//Large Buttons
		this.buttonConvertAllUp = this.addRenderableWidget(new PlainButton(this.leftPos + 5, this.topPos + 13, LARGEBUTTON_WIDTH, LARGEBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, 0, this.imageHeight));
		this.buttonConvertAllDown = this.addRenderableWidget(new PlainButton(this.leftPos + 89, this.topPos + 13, LARGEBUTTON_WIDTH, LARGEBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, LARGEBUTTON_WIDTH, this.imageHeight));
		
		//Small Buttons
		//Copper & Iron
		this.buttonConvertIronToCopper = this.addRenderableWidget(new PlainButton(this.leftPos + 6, this.topPos + 41, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.imageWidth, 0));
		this.buttonConvertCopperToIron = this.addRenderableWidget(new PlainButton(this.leftPos + 6, this.topPos + 69, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.imageWidth + SMALLBUTTON_WIDTH, 0));
		//Iron & Gold
		this.buttonConvertGoldToIron = this.addRenderableWidget(new PlainButton(this.leftPos + 41, this.topPos + 41, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.imageWidth, 2 * SMALLBUTTON_HEIGHT));
		this.buttonConvertIronToGold = this.addRenderableWidget(new PlainButton(this.leftPos + 41, this.topPos + 69, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.imageWidth + SMALLBUTTON_WIDTH, 2 * SMALLBUTTON_HEIGHT));
		//Gold & Emerald
		this.buttonConvertEmeraldToGold = this.addRenderableWidget(new PlainButton(this.leftPos + 75, this.topPos + 41, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.imageWidth, 4 * SMALLBUTTON_HEIGHT));
		this.buttonConvertGoldToEmerald = this.addRenderableWidget(new PlainButton(this.leftPos + 75, this.topPos + 69, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.imageWidth + SMALLBUTTON_WIDTH, 4 * SMALLBUTTON_HEIGHT));
		//Emerald & Diamond
		this.buttonConvertDiamondToEmerald = this.addRenderableWidget(new PlainButton(this.leftPos + 109, this.topPos + 41, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.imageWidth, 6 * SMALLBUTTON_HEIGHT));
		this.buttonConvertEmeraldToDiamond = this.addRenderableWidget(new PlainButton(this.leftPos + 109, this.topPos + 69, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.imageWidth + SMALLBUTTON_WIDTH, 6 * SMALLBUTTON_HEIGHT));
		//Diamond & Netherrite
		this.buttonConvertNetherriteToDiamond = this.addRenderableWidget(new PlainButton(this.leftPos + 144, this.topPos + 41, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.imageWidth, 8 * SMALLBUTTON_HEIGHT));
		this.buttonConvertDiamondToNetherrite = this.addRenderableWidget(new PlainButton(this.leftPos + 144, this.topPos + 69, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.imageWidth + SMALLBUTTON_WIDTH, 8 * SMALLBUTTON_HEIGHT));
		
		
	}
	
	@Override
	public void containerTick()
	{
		
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX,  mouseY);
	}
	
	private void pressButton(Button button)
	{
		int buttonInput = 0;
		if(button == buttonConvertAllUp)
		{
			buttonInput = 100;
			//CurrencyMod.LOGGER.info("Hit ConvertAllUp button!");
		}
		else if(button == buttonConvertAllDown)
		{
			buttonInput = -100;
			//CurrencyMod.LOGGER.info("Hit ConvertAllDown button!");
		}
		else if(button == buttonConvertCopperToIron)
		{
			buttonInput = 1;
		}
		else if(button == buttonConvertIronToCopper)
		{
			buttonInput = -1;
		}
		else if(button == buttonConvertIronToGold)
		{
			buttonInput = 2;
		}
		else if(button == buttonConvertGoldToIron)
		{
			buttonInput = -2;
		}
		else if(button == buttonConvertGoldToEmerald)
		{
			buttonInput = 3;
		}
		else if(button == buttonConvertEmeraldToGold)
		{
			buttonInput = -3;
		}
		else if(button == buttonConvertEmeraldToDiamond)
		{
			buttonInput = 4;
		}
		else if(button == buttonConvertDiamondToEmerald)
		{
			buttonInput = -4;
		}
		else if(button == buttonConvertDiamondToNetherrite)
		{
			buttonInput = 5;
		}
		else if(button == buttonConvertNetherriteToDiamond)
		{
			buttonInput = -5;
		}
		
		if(buttonInput != 0)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageATM(buttonInput));
		}
		else
		{
			LightmansCurrency.LogError("Unknown Button was hit for on the ATM Screen.");
		}
		
	}
	
}
