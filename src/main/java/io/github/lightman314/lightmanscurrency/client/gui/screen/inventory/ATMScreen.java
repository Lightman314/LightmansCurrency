package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.containers.ATMContainer;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.atm.MessageATM;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class ATMScreen extends ContainerScreen<ATMContainer>{

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
	
	public ATMScreen(ATMContainer container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		this.ySize = 212;
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
		this.font.drawString(matrix, this.title.getString(), 8.0f, 4.0f, 0x404040);
		this.font.drawString(matrix, this.playerInventory.getDisplayName().getString(), 8.0f, (this.ySize - 94), 0x404040);
	}
	
	@Override
	protected void init()
	{
		super.init();
		//Large Buttons
		this.buttonConvertAllUp = this.addButton(new PlainButton(this.guiLeft + 5, this.guiTop + 13, LARGEBUTTON_WIDTH, LARGEBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, 0, this.ySize));
		this.buttonConvertAllDown = this.addButton(new PlainButton(this.guiLeft + 89, this.guiTop + 13, LARGEBUTTON_WIDTH, LARGEBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, LARGEBUTTON_WIDTH, this.ySize));
		
		//Small Buttons
		//Copper & Iron
		this.buttonConvertIronToCopper = this.addButton(new PlainButton(this.guiLeft + 6, this.guiTop + 41, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.xSize, 0));
		this.buttonConvertCopperToIron = this.addButton(new PlainButton(this.guiLeft + 6, this.guiTop + 69, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.xSize + SMALLBUTTON_WIDTH, 0));
		//Iron & Gold
		this.buttonConvertGoldToIron = this.addButton(new PlainButton(this.guiLeft + 41, this.guiTop + 41, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.xSize, 2 * SMALLBUTTON_HEIGHT));
		this.buttonConvertIronToGold = this.addButton(new PlainButton(this.guiLeft + 41, this.guiTop + 69, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.xSize + SMALLBUTTON_WIDTH, 2 * SMALLBUTTON_HEIGHT));
		//Gold & Emerald
		this.buttonConvertEmeraldToGold = this.addButton(new PlainButton(this.guiLeft + 75, this.guiTop + 41, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.xSize, 4 * SMALLBUTTON_HEIGHT));
		this.buttonConvertGoldToEmerald = this.addButton(new PlainButton(this.guiLeft + 75, this.guiTop + 69, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.xSize + SMALLBUTTON_WIDTH, 4 * SMALLBUTTON_HEIGHT));
		//Emerald & Diamond
		this.buttonConvertDiamondToEmerald = this.addButton(new PlainButton(this.guiLeft + 109, this.guiTop + 41, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.xSize, 6 * SMALLBUTTON_HEIGHT));
		this.buttonConvertEmeraldToDiamond = this.addButton(new PlainButton(this.guiLeft + 109, this.guiTop + 69, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.xSize + SMALLBUTTON_WIDTH, 6 * SMALLBUTTON_HEIGHT));
		//Diamond & Netherrite
		this.buttonConvertNetherriteToDiamond = this.addButton(new PlainButton(this.guiLeft + 144, this.guiTop + 41, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.xSize, 8 * SMALLBUTTON_HEIGHT));
		this.buttonConvertDiamondToNetherrite = this.addButton(new PlainButton(this.guiLeft + 144, this.guiTop + 69, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::pressButton , GUI_TEXTURE, this.xSize + SMALLBUTTON_WIDTH, 8 * SMALLBUTTON_HEIGHT));
		
		
	}
	
	@Override
	public void tick()
	{
		super.tick();
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX,  mouseY);
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
