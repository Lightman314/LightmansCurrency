package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinData;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class CoinValueInput extends Widget{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID,"textures/gui/coinvalueinput.png");
	
	public static final int HEIGHT = 69;
	
	private final int leftOffset;
	private final ICoinValueInput parent;
	
	private CoinValue coinValue;
	Button toggleFree;
	private List<Button> increaseButtons;
	private List<Button> decreaseButtons;
	private ITextComponent title;
	public void setTitle(ITextComponent title) { this.title = title; }
	
	public boolean allowFreeToggle = true;
	
	public boolean drawBG = true;
	
	
	public CoinValueInput(int y, ITextComponent title, CoinValue startingValue, @Nonnull ICoinValueInput parent) {
		
		super(0, y, calculateWidth(), HEIGHT, new StringTextComponent(""));
		this.title = title;
		
		if(this.width == parent.getWidth())
			this.leftOffset = 0;
		else
			this.leftOffset = (parent.getWidth() - this.width) /  2;
		this.parent = parent;
		this.coinValue = startingValue.copy();
		
		this.init();
		
		
	}
	
	private void init()
	{
		
		this.toggleFree = this.parent.addButton(new PlainButton(this.leftOffset  + this.width - 14, this.y + 4, 10, 10, this::ToggleFree, GUI_TEXTURE, 40, HEIGHT));
		this.increaseButtons = new ArrayList<>();
		this.decreaseButtons = new ArrayList<>();
		int buttonCount = MoneyUtil.getAllData().size();
		for(int x = 0; x < buttonCount; x++)
		{
			increaseButtons.add(this.parent.addButton(new PlainButton(this.leftOffset + 10 + (x * 30), this.y + 15, 20, 10, this::IncreaseButtonHit, GUI_TEXTURE, 0, HEIGHT)));
			Button newButton = this.parent.addButton(new PlainButton(this.leftOffset + 10 + (x * 30), this.y + 53, 20, 10, this::DecreaseButtonHit, GUI_TEXTURE, 20, HEIGHT));
			newButton.active = false;
			decreaseButtons.add(newButton);
		}
		this.tick();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		//Match the buttons visibility to our visibility.
		this.increaseButtons.forEach(button -> button.visible = this.visible);
		this.decreaseButtons.forEach(button -> button.visible = this.visible);
		this.toggleFree.visible = this.allowFreeToggle && this.visible;
		if(!this.visible) //If not visible, render nothing
			return;
		
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		Minecraft.getInstance().getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = this.x + this.leftOffset;
		int startY = this.y;
		List<CoinData> coinData = MoneyUtil.getAllData();
		int buttonCount = coinData.size();
		
		if(this.drawBG)
		{
			//Render the left edge
			this.blit(matrixStack, startX, startY, 0, 0, 10, HEIGHT);
			
			//Render each column & spacer
			for(int x = 0; x < buttonCount; x++)
			{
				//Render the button column;
				this.blit(matrixStack, startX + 10 + (x * 30), startY, 10, 0, 20, HEIGHT);
				//Render the in-between button spacer
				if(x < (buttonCount - 1)) //Don't render the last spacer
					this.blit(matrixStack, startX + 30 + (x * 30), startY, 30, 0, 10, HEIGHT);
			}
			
			//Render the right edge
			this.blit(matrixStack, startX + 30 + ((buttonCount - 1) * 30), startY, 40, 0, 10, HEIGHT);
		}
		
		//Draw the coins initial & sprite
		for(int x = 0; x < buttonCount; x++)
		{
			//Draw sprite
			ItemRenderUtil.drawItemStack(this, this.parent.getFont(), new ItemStack(coinData.get(x).getCoinItem()), startX + (x * 30) + 12, startY + 26, false);
			//Draw string
			String countString = String.valueOf(this.coinValue.getEntry(coinData.get(x).getCoinItem()));// + coinData.get(x).getInitial().getString();
			int width = this.parent.getFont().getStringWidth(countString);
			this.parent.getFont().drawString(matrixStack, countString, startX + (x * 30) + 20 - (width / 2), startY + 43, 0x404040);
			
		}
		//Render the title
		this.parent.getFont().drawString(matrixStack, this.title.getString(), startX + 8F, startY + 5F, 0x404040);
		//Render the current price in the top-right corner
		String value = this.coinValue.getString();
		int priceWidth = this.parent.getFont().getStringWidth(value);
		int freeButtonOffset = this.allowFreeToggle ? 15 : 5;
		this.parent.getFont().drawString(matrixStack, value, startX + this.width - freeButtonOffset - priceWidth, startY + 5f, 0x404040);
		
	}
	
	public void tick()
	{
		//Set the decrease buttons as inactive if their value is 0;
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
	
	public static int calculateWidth()
	{
		//Get button count
		int buttonCount = MoneyUtil.getAllData().size();
		
		return 20 + (20 * buttonCount) + (10 * (buttonCount - 1));
		
	}
	
	private void IncreaseButtonHit(Button button)
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
	
	private void DecreaseButtonHit(Button button)
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
	}
	
	public CoinValue getCoinValue()
	{
		return this.coinValue;
	}
	
	public void setCoinValue(CoinValue newValue)
	{
		this.coinValue = newValue.copy();
	}
	
	public static interface ICoinValueInput
	{
		public <T extends Button> T addButton(T button);
		public int getWidth();
		public FontRenderer getFont();
		public void OnCoinValueChanged(CoinValueInput input);
	}

}
