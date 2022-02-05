package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageATMConversion;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ConversionTab extends ATMTab{

	public ConversionTab(ATMScreen screen) { super(screen); }

	public static final int LARGEBUTTON_WIDTH = 82;
	public static final int LARGEBUTTON_HEIGHT = 18;
	
	public static final int SMALLBUTTON_WIDTH = 26;
	public static final int SMALLBUTTON_HEIGHT = 18;
	
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
	
	@Override
	public void init() {
		
		//Large Buttons
		this.buttonConvertAllUp = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + 5, this.screen.getGuiTop() + 13, LARGEBUTTON_WIDTH, LARGEBUTTON_HEIGHT, this::PressConversionButton , ATMScreen.GUI_TEXTURE, 0, this.screen.getYSize()));
		this.buttonConvertAllDown = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + 89, this.screen.getGuiTop() + 13, LARGEBUTTON_WIDTH, LARGEBUTTON_HEIGHT, this::PressConversionButton , ATMScreen.GUI_TEXTURE, LARGEBUTTON_WIDTH, this.screen.getYSize()));
		
		//Small Buttons
		//Copper & Iron
		this.buttonConvertIronToCopper = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + 6, this.screen.getGuiTop() + 41, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::PressConversionButton , ATMScreen.GUI_TEXTURE, this.screen.getXSize(), 0));
		this.buttonConvertCopperToIron = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + 6, this.screen.getGuiTop() + 69, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::PressConversionButton , ATMScreen.GUI_TEXTURE, this.screen.getXSize() + SMALLBUTTON_WIDTH, 0));
		//Iron & Gold
		this.buttonConvertGoldToIron = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + 41, this.screen.getGuiTop() + 41, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::PressConversionButton , ATMScreen.GUI_TEXTURE, this.screen.getXSize(), 2 * SMALLBUTTON_HEIGHT));
		this.buttonConvertIronToGold = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + 41, this.screen.getGuiTop() + 69, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::PressConversionButton , ATMScreen.GUI_TEXTURE, this.screen.getXSize() + SMALLBUTTON_WIDTH, 2 * SMALLBUTTON_HEIGHT));
		//Gold & Emerald
		this.buttonConvertEmeraldToGold = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + 75, this.screen.getGuiTop() + 41, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::PressConversionButton , ATMScreen.GUI_TEXTURE, this.screen.getXSize(), 4 * SMALLBUTTON_HEIGHT));
		this.buttonConvertGoldToEmerald = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + 75, this.screen.getGuiTop() + 69, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::PressConversionButton , ATMScreen.GUI_TEXTURE, this.screen.getXSize() + SMALLBUTTON_WIDTH, 4 * SMALLBUTTON_HEIGHT));
		//Emerald & Diamond
		this.buttonConvertDiamondToEmerald = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + 109, this.screen.getGuiTop() + 41, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::PressConversionButton , ATMScreen.GUI_TEXTURE, this.screen.getXSize(), 6 * SMALLBUTTON_HEIGHT));
		this.buttonConvertEmeraldToDiamond = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + 109, this.screen.getGuiTop() + 69, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::PressConversionButton , ATMScreen.GUI_TEXTURE, this.screen.getXSize() + SMALLBUTTON_WIDTH, 6 * SMALLBUTTON_HEIGHT));
		//Diamond & Netherrite
		this.buttonConvertNetherriteToDiamond = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + 144, this.screen.getGuiTop() + 41, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::PressConversionButton , ATMScreen.GUI_TEXTURE, this.screen.getXSize(), 8 * SMALLBUTTON_HEIGHT));
		this.buttonConvertDiamondToNetherrite = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + 144, this.screen.getGuiTop() + 69, SMALLBUTTON_WIDTH, SMALLBUTTON_HEIGHT, this::PressConversionButton , ATMScreen.GUI_TEXTURE, this.screen.getXSize() + SMALLBUTTON_WIDTH, 8 * SMALLBUTTON_HEIGHT));
		
		
	}

	@Override
	public void preRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) { }

	@Override
	public void postRender(MatrixStack matrix, int mouseX, int mouseY) { }
	
	@Override
	public void tick() { }
	
	@Override
	public void onClose() { }
	
	private void PressConversionButton(Button button)
	{
		int buttonInput = 0;
		if(button == buttonConvertAllUp)
		{
			buttonInput = 100;
		}
		else if(button == buttonConvertAllDown)
		{
			buttonInput = -100;
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
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageATMConversion(buttonInput));
		}
		else
		{
			LightmansCurrency.LogError("Unknown Button was hit for on the ATM Screen.");
		}
	}

	@Override
	public IconData getIcon() { return IconData.of(ModBlocks.MACHINE_ATM); }

	@Override
	public ITextComponent getTooltip() { return new TranslationTextComponent("tooltip.lightmanscurrency.atm.conversion"); }
	
	

}
