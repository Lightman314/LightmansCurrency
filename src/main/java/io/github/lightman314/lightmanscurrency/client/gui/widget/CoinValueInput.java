package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.easy.IEasyTickable;
import io.github.lightman314.lightmanscurrency.common.money.CoinData;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue.ValueType;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CoinValueInput extends EasyWidgetWithChildren implements IScrollable, IEasyTickable {


	public static final long MAX_PRICE = Long.MAX_VALUE / 100L;
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID,"textures/gui/coinvalueinput.png");

	public static final Consumer<CoinValue> EMPTY_CONSUMER = v -> {};

	public static final int HEIGHT = 69;
	public static final int DISPLAY_WIDTH = 176;

	public static final Sprite SPRITE_FREE_TOGGLE = Sprite.SimpleSprite(GUI_TEXTURE, 40, HEIGHT, 10, 10);
	public static final Sprite SPRITE_UP_ARROW = Sprite.SimpleSprite(GUI_TEXTURE, 0, HEIGHT, 20, 10);
	public static final Sprite SPRITE_DOWN_ARROW = Sprite.SimpleSprite(GUI_TEXTURE, 20, HEIGHT, 20, 10);
	public static final Sprite SPRITE_LEFT_ARROW = Sprite.SimpleSprite(GUI_TEXTURE, 50, HEIGHT, 10, 20);
	public static final Sprite SPRITE_RIGHT_ARROW = Sprite.SimpleSprite(GUI_TEXTURE, 60, HEIGHT, 10, 20);

	public static final int MAX_BUTTON_COUNT = 6;
	
	private static final int SEGMENT_WIDTH = 20;
	private static final int SEGMENT_SPACING = 5;
	private static final int SEGMENT_TOTAL = SEGMENT_WIDTH + SEGMENT_SPACING;
	
	private final ValueType inputType;
	
	private final Font font;
	private final Consumer<CoinValue> onValueChanged;

	EasyButton buttonLeft;
	EasyButton buttonRight;
	
	private CoinValue coinValue;
	EasyButton toggleFree;
	private List<EasyButton> increaseButtons;
	private List<EasyButton> decreaseButtons;
	private Component title;
	public void setTitle(Component title) {this.title = title; }
	
	String lastInput = "";
	EditBox valueInput;
	String prefix;
	String postfix;
	
	public boolean allowFreeToggle = true;
	
	public boolean drawBG = true;
	
	public boolean locked = false;
	
	int scroll = 0;
	List<CoinData> coinData = new ArrayList<>();
	
	public CoinValueInput(ScreenPosition pos, Component title, CoinValue startingValue, Font font, Consumer<CoinValue> onValueChanged) { this(pos.x, pos.y, title, startingValue, font, onValueChanged); }
	public CoinValueInput(int x, int y, Component title, CoinValue startingValue, Font font, Consumer<CoinValue> onValueChanged) {
		super(x, y, DISPLAY_WIDTH, HEIGHT);
		
		this.inputType = Config.SERVER.coinValueInputType.get();
		this.title = title;
		
		this.font = font;
		this.onValueChanged = onValueChanged;
		
		this.coinValue = startingValue;
		
		if(this.inputType == ValueType.VALUE)
			this.setPrefixAndPostfix();
		else
			this.getCoinData();
	}

	@Override
	public CoinValueInput withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

	@Override
	public void addChildren() {
		this.addChild(new ScrollListener(this.getArea(), this));
		this.toggleFree = this.addChild(new PlainButton(this.getX() + this.width - 14, this.getY() + 4, this::ToggleFree, SPRITE_FREE_TOGGLE));
		this.increaseButtons = new ArrayList<>();
		this.decreaseButtons = new ArrayList<>();
		//Initialize default button setup
		if(this.inputType == ValueType.DEFAULT)
		{
			int buttonCount = this.coinData.size();
			if(buttonCount > MAX_BUTTON_COUNT)
			{
				buttonCount = MAX_BUTTON_COUNT;
				this.buttonLeft = this.addChild(new PlainButton(this.getX() + 4, this.getY() + 29, b -> this.scrollLeft(), SPRITE_LEFT_ARROW));
				this.buttonLeft.visible = false;
				this.buttonRight = this.addChild(new PlainButton(this.getX() + this.width - 14, this.getY() + 29, b -> this.scrollRight(), SPRITE_RIGHT_ARROW));
			}
			int startX = this.getStartX() + this.getX();
			for(int x = 0; x < buttonCount; x++)
			{
				EasyButton newButton = this.addChild(new PlainButton(startX + (x * SEGMENT_TOTAL), this.getY() + 15, this::IncreaseButtonHit, SPRITE_UP_ARROW));
				newButton.active = true;
				increaseButtons.add(newButton);
				newButton = this.addChild(new PlainButton(startX + (x * SEGMENT_TOTAL), this.getY() + 53, this::DecreaseButtonHit, SPRITE_DOWN_ARROW));
				newButton.active = false;
				decreaseButtons.add(newButton);
			}
		}
		else
		{
			//Value input
			int prefixWidth = this.font.width(this.prefix);
			if(prefixWidth > 0)
				prefixWidth += 2;
			int postfixWidth = this.font.width(this.postfix);
			if(postfixWidth > 0)
				postfixWidth += 2;
			this.valueInput = this.addChild(new EditBox(this.font, this.getX() + 10 + prefixWidth, this.getY() + 20, DISPLAY_WIDTH - 20 - prefixWidth - postfixWidth, 20, EasyText.empty()));
		}
		this.tick();
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
	
	private void getCoinData() {
		this.coinData = MoneyUtil.getAllData(MoneyUtil.MAIN_CHAIN);
	}

	@Override
	protected void renderTick() {
		//Match the buttons visibility to our visibility.
		this.toggleFree.visible = this.allowFreeToggle && this.visible;
		this.increaseButtons.forEach(button -> button.visible = this.visible);
		this.decreaseButtons.forEach(button -> button.visible = this.visible);
		if(this.valueInput != null)
			this.valueInput.visible = this.visible;
	}

	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui)
	{
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f,  1f,  1f, 1f);
		gui.resetColor();
		
		if(this.drawBG)
		{
			//Render the background
			gui.blit(GUI_TEXTURE, 0,0,0,0,DISPLAY_WIDTH,HEIGHT);
		}
		
		if(this.inputType == ValueType.DEFAULT)
		{
			
			this.validateScroll();
			
			int buttonCount = Math.min(coinData.size(), MAX_BUTTON_COUNT);
			
			//Draw the coins initial & sprite
			int startX = this.getStartX();
			for(int x = 0; x < buttonCount; x++)
			{
				//Draw sprite
				gui.renderItem(new ItemStack(this.coinData.get(x + this.scroll).coinItem), startX + (x * SEGMENT_TOTAL) + 2, 26);
				//Draw string
				String countString = String.valueOf(this.coinValue.getEntry(this.coinData.get(x + this.scroll).coinItem));
				int width = this.font.width(countString);
				gui.drawString(countString, startX + (x * SEGMENT_TOTAL) + 10 - (width / 2), 43, 0x404040);
			}
		}
		//Draw background for the text input field variant
		else if(this.inputType == ValueType.VALUE)
		{
			
			//Draw the prefix and postfix
			gui.drawString(this.prefix, 10, 26, 0xFFFFFF);
			int postfixWidth = this.font.width(this.postfix);
			gui.drawString(this.postfix, DISPLAY_WIDTH - 10 - postfixWidth, 26, 0xFFFFFF);
			
		}
		
		//Render the current price in the top-right corner
		int priceWidth = this.font.width(this.coinValue.getString());
		int freeButtonOffset = this.allowFreeToggle ? 15 : 5;
		gui.drawString(this.coinValue.getComponent(), this.width - freeButtonOffset - priceWidth, 5, 0x404040);
		
		//Render the title
		int titleWidth = this.width - 7 - freeButtonOffset - priceWidth;
		gui.drawString(TextRenderUtil.fitString(this.title, titleWidth), 8, 5, 0x404040);
		
	}
	
	private int getStartX() {
		int buttonCount = Math.min(this.coinData.size(), MAX_BUTTON_COUNT);
		int space = this.width - (buttonCount * SEGMENT_TOTAL) + SEGMENT_SPACING;
		return space / 2;
	}

	@Override
	public void tick()
	{
		//Set the decrease buttons as inactive if their value is 0;
		this.toggleFree.active = !this.locked;
		if(this.inputType == ValueType.DEFAULT)
		{
			List<Item> coinItems = MoneyUtil.getAllCoins();
			for(int i = 0; i < decreaseButtons.size(); i++)
			{
				if(i + this.scroll >= coinItems.size())
					decreaseButtons.get(i).active = false;
				else
					decreaseButtons.get(i).active = this.coinValue.getEntry(coinItems.get(i + this.scroll)) > 0 && !this.locked;
			}
			for (EasyButton increaseButton : this.increaseButtons)
				increaseButton.active = !this.coinValue.isFree() && !this.locked;
			
			if(this.buttonLeft != null)
				this.buttonLeft.visible = this.scroll > 0;
			if(this.buttonRight != null)
				this.buttonRight.visible = this.scroll < this.getMaxScroll();
		}
		else if(this.valueInput != null)
		{
			this.valueInput.tick();
			this.valueInput.active = !this.coinValue.isFree() && !this.locked;
			if(!this.coinValue.isFree())
			{
				TextInputUtil.whitelistFloat(this.valueInput);
				if(!this.lastInput.contentEquals(this.valueInput.getValue()))
				{
					this.lastInput = this.valueInput.getValue();
					this.coinValue = MoneyUtil.displayValueToCoinValue(this.getDisplayValue());
					this.onValueChanged.accept(this.coinValue);
				}
			}
			else
			{
				this.valueInput.setValue("");
				this.lastInput = this.valueInput.getValue();
			}
		}
		
	}
	
	public void IncreaseButtonHit(EasyButton button)
	{
		if(!this.increaseButtons.contains(button))
			return;
		
		int coinIndex = this.increaseButtons.indexOf(button);
		if(coinIndex < 0)
			return;
		
		coinIndex += this.scroll;
		
		if(coinIndex >= 0 && coinIndex < this.coinData.size())
		{
			Item coin = this.coinData.get(coinIndex).coinItem;
			int addAmount = 1;
			if(Screen.hasShiftDown())
				addAmount = getLargeIncreaseAmount(coin);
			if(Screen.hasControlDown())
				addAmount *= 10;
			this.coinValue = this.coinValue.plusValue(coin, addAmount);
			this.ValidateValue();
			this.onValueChanged.accept(this.coinValue);
		}
		else
			LightmansCurrency.LogError("Invalid index (" + coinIndex + ") found for the increasing button.");
	}


	
	public void DecreaseButtonHit(EasyButton button)
	{
		if(!this.decreaseButtons.contains(button))
			return;
			
		int coinIndex = this.decreaseButtons.indexOf(button);
		if(coinIndex < 0)
			return;
		
		coinIndex += this.scroll;
		
		if(coinIndex >= 0 && coinIndex < this.coinData.size())
		{
			Item coin = this.coinData.get(coinIndex).coinItem;
			int removeAmount = 1;
			if(Screen.hasShiftDown())
				removeAmount = getLargeIncreaseAmount(coin);
			if(Screen.hasControlDown())
				removeAmount *= 10;
			//LightmansCurrency.LOGGER.info("Removing " + (Screen.hasShiftDown() ? 5 : 1) + " coins of type '" + MoneyUtil.getAllCoins().get(coinIndex).getRegistryName().toString() + "' from the input value.");
			this.coinValue = this.coinValue.minusValue(coin, removeAmount);
			this.onValueChanged.accept(this.coinValue);
		}
		else
			LightmansCurrency.LogError("Invalid index (" + coinIndex + ") found for the decreasing button.");
	}
	
	private int getLargeIncreaseAmount(Item coinItem)
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
	
	private int getLargeAmount(Pair<Item,Integer> conversion)
	{
		if(conversion.getSecond() >= 64)
			return 16;
		if(conversion.getSecond() > 10)
			return 10;
		if(conversion.getSecond() > 5)
			return 5;
		return 2;
	}
	
	private void ToggleFree(EasyButton button)
	{
		this.coinValue = this.coinValue.isFree() ? CoinValue.EMPTY : CoinValue.FREE;
		this.onValueChanged.accept(this.coinValue);
	}
	
	public CoinValue getCoinValue() { return this.coinValue; }
	
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
		this.coinValue = newValue;
		this.ValidateValue();
		if(this.inputType == ValueType.VALUE && this.valueInput != null)
			this.valueInput.setValue(Config.formatValueOnly(newValue.getDisplayValue()));
	}
	
	private void scrollLeft() {
		this.scroll--;
		this.validateScroll();
	}
	
	private void scrollRight() {
		this.scroll++;
		this.validateScroll();
	}

	private void ValidateValue()
	{
		if(this.coinValue.getValueNumber() > MAX_PRICE)
			this.coinValue = CoinValue.fromNumber(MAX_PRICE);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) { return false; }

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }

	@Override
	public int currentScroll() { return this.scroll; }

	@Override
	public void setScroll(int newScroll) { this.scroll = Math.max(newScroll, this.getMaxScroll()); }

	@Override
	public int getMaxScroll() { return IScrollable.calculateMaxScroll(MAX_BUTTON_COUNT, this.coinData.size()); }


}
