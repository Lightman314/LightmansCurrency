package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.money.CoinData;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.CoinValue.ValueType;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CoinValueInput extends AbstractWidget{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID,"textures/gui/coinvalueinput.png");
	
	public static final int HEIGHT = 69;
	public static final int DISPLAY_WIDTH = 176;
	
	private static final int SEGMENT_WIDTH = 20;
	private static final int SEGMENT_SPACING = 5;
	private static final int SEGMENT_TOTAL = SEGMENT_WIDTH + SEGMENT_SPACING;
	
	private final int leftOffset;
	private final ICoinValueInput parent;
	
	private final ValueType inputType;
	
	private CoinValue coinValue;
	Button toggleFree;
	private List<Button> increaseButtons;
	private List<Button> decreaseButtons;
	private Component title;
	public void setTitle(Component title) {this.title = title; }
	
	String lastInput = "";
	EditBox valueInput;
	String prefix;
	String postfix;
	
	public boolean allowFreeToggle = true;
	
	public boolean drawBG = true;
	
	public CoinValueInput(int y, Component title, CoinValue startingValue, @Nonnull ICoinValueInput parent) {
		
		super(0, y, calculateWidth(), HEIGHT, title);
		
		this.inputType = Config.SERVER.coinValueInputType.get();
		this.title = title;
		
		if(this.width == parent.getWidth())
			this.leftOffset = 0;
		else
			this.leftOffset = (parent.getWidth() - this.width) /  2;
		this.parent = parent;
		this.coinValue = startingValue.copy();
		
		
		if(this.inputType == ValueType.VALUE)
			this.setPrefixAndPostfix();
		//To be called manually by the screen so that the buttons will render after the main body
		//this.init();
		
	}
	
	private void setPrefixAndPostfix()
	{
		String format = Config.SERVER.valueFormat.get();
		//Have to replace the {value} with a non-illegal character in order to split the string
		String[] splitFormat = format.replace("{value}", "`").split("`",2);
		if(splitFormat.length < 2)
		{
			//Determine which is the prefix, and which is the postfix
			if(format.startsWith("{value}"))
			{
				prefix = "";
				postfix = splitFormat[0];
			}
			else
			{
				prefix = splitFormat[0];
				postfix = "";
			}
		}
		else
		{
			prefix = splitFormat[0];
			postfix = splitFormat[1];
		}
				
	}
	
	public void init()
	{
		this.toggleFree = this.parent.addCustomWidget(new PlainButton(this.leftOffset + this.width - 14, this.y + 4, 10, 10, this::ToggleFree, GUI_TEXTURE, 40, HEIGHT));
		this.increaseButtons = new ArrayList<>();
		this.decreaseButtons = new ArrayList<>();
		//Initialize default button setup
		if(this.inputType == ValueType.DEFAULT)
		{
			int buttonCount = MoneyUtil.getAllData(MoneyUtil.MAIN_CHAIN).size();
			for(int x = 0; x < buttonCount; x++)
			{
				Button newButton = this.parent.addCustomWidget(new PlainButton(this.leftOffset + 10 + (x * SEGMENT_TOTAL), this.y + 15, 20, 10, this::IncreaseButtonHit, GUI_TEXTURE, 0, HEIGHT));
				newButton.active = true;
				increaseButtons.add(newButton);
				newButton = this.parent.addCustomWidget(new PlainButton(this.leftOffset + 10 + (x * SEGMENT_TOTAL), this.y + 53, 20, 10, this::DecreaseButtonHit, GUI_TEXTURE, 20, HEIGHT));
				newButton.active = false;
				decreaseButtons.add(newButton);
			}
		}
		else
		{
			//Value input
			int prefixWidth = this.parent.getFont().width(this.prefix);
			if(prefixWidth > 0)
				prefixWidth += 2;
			int postfixWidth = this.parent.getFont().width(this.postfix);
			if(postfixWidth > 0)
				postfixWidth += 2;
			this.valueInput = this.parent.addCustomWidget(new EditBox(this.parent.getFont(), this.leftOffset + 10 + prefixWidth, this.y + 20, DISPLAY_WIDTH - 20 - prefixWidth - postfixWidth, 20, new TextComponent("")));
		}
		this.tick();
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		//Match the buttons visibility to our visibility.
		this.toggleFree.visible = this.allowFreeToggle && this.visible;
		this.increaseButtons.forEach(button -> button.visible = this.visible);
		this.decreaseButtons.forEach(button -> button.visible = this.visible);
		if(!this.visible) //If not visible, render nothing
			return;
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f,  1f,  1f, 1f);
		
		int startX = this.x + this.leftOffset;
		int startY = this.y;
		
		if(this.inputType == ValueType.DEFAULT)
		{
			List<CoinData> coinData = MoneyUtil.getAllData(MoneyUtil.MAIN_CHAIN);
			int buttonCount = coinData.size();
			
			if(this.drawBG)
			{
				//Render the left edge
				this.blit(poseStack, startX, startY, 0, 0, 10, HEIGHT);
				//Render each column & spacer
				for(int x = 0; x < buttonCount; x++)
				{
					//Render the button column;
					this.blit(poseStack, startX + 10 + (x * SEGMENT_TOTAL), startY, 10, 0, SEGMENT_WIDTH, HEIGHT);
					//Render the in-between button spacer
					if(x < (buttonCount - 1)) //Don't render the last spacer
						this.blit(poseStack, startX + 30 + (x * SEGMENT_TOTAL), startY, 30, 0, SEGMENT_SPACING, HEIGHT);
				}
				//Render the right edge
				this.blit(poseStack, startX + 30 + ((buttonCount - 1) * SEGMENT_TOTAL), startY, 40, 0, 10, HEIGHT);
			}
			
			//Draw the coins initial & sprite
			for(int x = 0; x < buttonCount; x++)
			{
				//Draw sprite
				ItemRenderUtil.drawItemStack(this, this.parent.getFont(), new ItemStack(coinData.get(x).coinItem), startX + (x * SEGMENT_TOTAL) + 12, startY + 26);
				//Draw string
				String countString = String.valueOf(this.coinValue.getEntry(coinData.get(x).coinItem));// + coinData.get(x).getInitial().getString();
				int width = this.parent.getFont().width(countString);
				this.parent.getFont().draw(poseStack, countString, startX + (x * SEGMENT_TOTAL) + 20 - (width / 2), startY + 43, 0x404040);
				
			}
		}
		//Draw background for the text input field variant
		else if(this.drawBG)
		{
			//Render the left edge
			this.blit(poseStack, startX, startY, 0, 0, 10, HEIGHT);
			//Render the center until we've hit the end
			int xPos = startX + 10;
			while(xPos < startX + DISPLAY_WIDTH - 10)
			{
				int xSize = Math.min(30, startX + DISPLAY_WIDTH - 10 - xPos);
				this.blit(poseStack, xPos, startY, 10, 0, xSize, HEIGHT);
				xPos += xSize;
			}
			//Render the right edge
			this.blit(poseStack, xPos, startY, 40, 0, 10, HEIGHT);
			
			//Draw the prefix and postfix
			this.parent.getFont().draw(poseStack, this.prefix, startX + 10, startY + 26, 0xFFFFFF);
			int postfixWidth = this.parent.getFont().width(this.postfix);
			this.parent.getFont().draw(poseStack, this.postfix, startX + DISPLAY_WIDTH - 10 - postfixWidth, startY + 26, 0xFFFFFF);
			
		}
		
		//Render the title
		this.parent.getFont().draw(poseStack, this.title.getString(), startX + 8F, startY + 5F, 0x404040);
		//Render the current price in the top-right corner
		int priceWidth = this.parent.getFont().width(this.coinValue.getString());
		int freeButtonOffset = this.allowFreeToggle ? 15 : 5;
		this.parent.getFont().draw(poseStack, this.coinValue.getString(), startX + this.width - freeButtonOffset - priceWidth, startY + 5F, 0x404040);
		
	}
	
	public void tick()
	{
		//Set the decrease buttons as inactive if their value is 0;
		if(this.inputType == ValueType.DEFAULT)
		{
			List<Item> coinItems = MoneyUtil.getAllCoins();
			for(int i = 0; i < decreaseButtons.size(); i++)
			{
				if(i >= coinItems.size())
					decreaseButtons.get(i).active = false;
				else
				{
					decreaseButtons.get(i).active = this.coinValue.getEntry(coinItems.get(i)) > 0;
				}
			}
			for(int i = 0; i < this.increaseButtons.size(); i++)
				this.increaseButtons.get(i).active = !this.coinValue.isFree();
		}
		else if(this.valueInput != null)
		{
			this.valueInput.tick();
			this.valueInput.active = !this.coinValue.isFree();
			if(!this.coinValue.isFree())
			{
				TextInputUtil.whitelistFloat(this.valueInput);
				if(!this.lastInput.contentEquals(this.valueInput.getValue()))
				{
					this.lastInput = this.valueInput.getValue();
					this.coinValue = MoneyUtil.displayValueToCoinValue(this.getDisplayValue());
					this.parent.OnCoinValueChanged(this);
				}
			}
			else
			{
				this.valueInput.setValue("");
				this.lastInput = this.valueInput.getValue();
			}
		}
		
	}
	
	public static int calculateWidth()
	{
		if(Config.SERVER.coinValueInputType.get() == ValueType.VALUE)
			return DISPLAY_WIDTH;
		//Default width based on coin count
		int buttonCount = MoneyUtil.getAllData(MoneyUtil.MAIN_CHAIN).size();
		return 20 + (SEGMENT_WIDTH * buttonCount) + (SEGMENT_SPACING * (buttonCount - 1));
	}
	
	public void IncreaseButtonHit(Button button)
	{
		if(!this.increaseButtons.contains(button))
			return;
		
		int coinIndex = this.increaseButtons.indexOf(button);
		
		List<Item> coins = MoneyUtil.getAllCoins();
		if(coinIndex >= 0 && coinIndex < coins.size())
		{
			Item coin = coins.get(coinIndex);
			int addAmount = 1;
			if(Screen.hasShiftDown())
				addAmount = getLargeIncreaseAmount(coin);
			if(Screen.hasControlDown())
				addAmount *= 10;
			this.coinValue.addValue(coin, addAmount);
			this.parent.OnCoinValueChanged(this);
		}
		else
			LightmansCurrency.LogError("Invalid index (" + coinIndex + ") found for the increasing button.");
	}
	
	public void DecreaseButtonHit(Button button)
	{
		if(!this.decreaseButtons.contains(button))
			return;
			
		int coinIndex = this.decreaseButtons.indexOf(button);
		
		List<Item> coins = MoneyUtil.getAllCoins();
		if(coinIndex >= 0 && coinIndex < coins.size())
		{
			Item coin = coins.get(coinIndex);
			int removeAmount = 1;
			if(Screen.hasShiftDown())
				removeAmount = getLargeIncreaseAmount(coin);
			if(Screen.hasControlDown())
				removeAmount *= 10;
			//LightmansCurrency.LOGGER.info("Removing " + (Screen.hasShiftDown() ? 5 : 1) + " coins of type '" + MoneyUtil.getAllCoins().get(coinIndex).getRegistryName().toString() + "' from the input value.");
			this.coinValue.removeValue(coin, removeAmount);
			this.parent.OnCoinValueChanged(this);
		}
		else
			LightmansCurrency.LogError("Invalid index (" + coinIndex + ") found for the decreasing button.");
	}
	
	private final int getLargeIncreaseAmount(Item coinItem)
	{
		Pair<Item,Integer> upwardConversion = MoneyUtil.getUpwardConversion(coinItem);
		if(upwardConversion != null)
			return getLargeAmount(upwardConversion);
		else
		{
			Pair<Item,Integer> downwardConversion = MoneyUtil.getDownwardConversion(coinItem);
			if(downwardConversion != null)
				return getLargeAmount(downwardConversion);
			//No conversion found for this coin. Assume 10;
			return 10;
		}
	}
	
	private final int getLargeAmount(Pair<Item,Integer> conversion)
	{
		if(conversion.getSecond() >= 64)
			return 16;
		if(conversion.getSecond() > 10)
			return 10;
		if(conversion.getSecond() > 5)
			return 5;
		return 2;
	}
	
	private void ToggleFree(Button button)
	{
		this.coinValue.setFree(!this.coinValue.isFree());
		this.parent.OnCoinValueChanged(this);
	}
	
	public CoinValue getCoinValue()
	{
		return this.coinValue;
	}
	
	public double getDisplayValue()
	{
		if(this.valueInput != null)
		{
			return TextInputUtil.getDoubleValue(this.valueInput);
		}
		return this.coinValue.getDisplayValue();
	}
	
	public void setCoinValue(CoinValue newValue)
	{
		this.coinValue = newValue.copy();
		if(this.inputType == ValueType.VALUE)
		{
			this.valueInput.setValue(Config.formatValueOnly(newValue.getDisplayValue()));
		}
	}
	
	public static interface ICoinValueInput
	{
		public <T extends GuiEventListener & Widget & NarratableEntry> T addCustomWidget(T button);
		public int getWidth();
		public Font getFont();
		public void OnCoinValueChanged(CoinValueInput input);
	}

	@Override
	public void updateNarration(NarrationElementOutput narrator) { }

}
