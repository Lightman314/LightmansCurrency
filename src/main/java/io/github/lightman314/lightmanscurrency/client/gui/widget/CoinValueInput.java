package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinData;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CoinValueInput extends AbstractWidget{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID,"textures/gui/coinvalueinput.png");
	
	public static final int HEIGHT = 69;
	
	private final int leftOffset;
	private final ICoinValueInput parent;
	
	private CoinValue coinValue;
	private List<Button> increaseButtons;
	private List<Button> decreaseButtons;
	private Component title;
	
	public CoinValueInput(int y, Component title, CoinValue startingValue, @Nonnull ICoinValueInput parent) {
		
		super(0, y, calculateWidth(), HEIGHT, title);
		
		this.title = title;
		
		if(this.width == parent.getWidth())
			this.leftOffset = 0;
		else
			this.leftOffset = (parent.getWidth() - this.width) /  2;
		this.parent = parent;
		this.coinValue = startingValue.copy();
		
		this.init();
		
		
	}
	
	protected void init()
	{
		this.increaseButtons = new ArrayList<>();
		this.decreaseButtons = new ArrayList<>();
		int buttonCount = MoneyUtil.getAllData().size();
		for(int x = 0; x < buttonCount; x++)
		{
			increaseButtons.add(this.parent.addCustomWidget(new PlainButton(this.leftOffset + 10 + (x * 30), this.y + 15, 20, 10, this::IncreaseButtonHit, GUI_TEXTURE, 0, HEIGHT)));
			Button newButton = this.parent.addCustomWidget(new PlainButton(this.leftOffset + 10 + (x * 30), this.y + 53, 20, 10, this::DecreaseButtonHit, GUI_TEXTURE, 20, HEIGHT));
			newButton.active = false;
			decreaseButtons.add(newButton);
		}
		this.tick();
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		int startX = this.x + this.leftOffset;
		int startY = this.y;
		//Render the left edge
		this.blit(matrixStack, startX, startY, 0, 0, 10, HEIGHT);
		List<CoinData> coinData = MoneyUtil.getAllData();
		int buttonCount = coinData.size();
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
		
		//Draw the coins initial & sprite
		for(int x = 0; x < buttonCount; x++)
		{
			//Draw sprite
			this.drawItemStack(new ItemStack(coinData.get(x).getCoinItem()), startX + (x * 30) + 12, startY + 26);
			//Draw string
			String countString = String.valueOf(this.coinValue.getEntry(coinData.get(x).getCoinItem()));// + coinData.get(x).getInitial().getString();
			int width = this.parent.getFont().width(countString);
			this.parent.getFont().draw(matrixStack, countString, startX + (x * 30) + 20 - (width / 2), startY + 43, 0x404040);
			
		}
		//Render the title
		this.parent.getFont().draw(matrixStack, this.title.getString(), startX + 8F, startY + 5F, 0x404040);
		//Render the current price in the top-right corner
		int priceWidth = this.parent.getFont().width(this.coinValue.getString());
		this.parent.getFont().draw(matrixStack, this.coinValue.getString(), startX + this.width - 5F - priceWidth, startY + 5F, 0x404040);
		
	}
	
	/**
    * Draws an ItemStack.
    *  
    * The z index is increased by 32 (and not decreased afterwards), and the item is then rendered at z=200.
    */
	private void drawItemStack(ItemStack stack, int x, int y) {
		
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
	   
      	//RenderSystem.translatef(0.0F, 0.0F, 32.0F);
      	//this.setBlitOffset(200);
      	//itemRenderer.zLevel = 200.0F;
      	//Font font = stack.getItem().getFontRenderer(stack);
      	//if (font == null) font = this.parent.getFont();
      	itemRenderer.renderGuiItem(stack, x, y);
      	//this.setBlitOffset(0);
      	//itemRenderer.zLevel = 0.0F;
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
	}
	
	public static int calculateWidth()
	{
		//Get button count
		int buttonCount = MoneyUtil.getAllData().size();
		
		return 20 + (20 * buttonCount) + (10 * (buttonCount - 1));
		
	}
	
	public void IncreaseButtonHit(Button button)
	{
		if(!this.increaseButtons.contains(button))
			return;
		
		int coinIndex = this.increaseButtons.indexOf(button);
		
		if(coinIndex >= 0 && coinIndex < MoneyUtil.getAllCoins().size())
		{
			//LightmansCurrency.LOGGER.info("Adding " + (Screen.hasShiftDown() ? 5 : 1) + " coins of type '" + MoneyUtil.getAllCoins().get(coinIndex).getRegistryName().toString() + "' from the input value.");
			this.coinValue.addValue(MoneyUtil.getAllCoins().get(coinIndex), Screen.hasShiftDown() ? 5 : 1);
			this.parent.OnCoinValueChanged(this);
		}
		else
		{
			LightmansCurrency.LogError("Invalid index (" + coinIndex + ") found for the increasing button.");
		}
	}
	
	public void DecreaseButtonHit(Button button)
	{
		if(!this.decreaseButtons.contains(button))
			return;
		int coinIndex = this.decreaseButtons.indexOf(button);
		if(coinIndex >= 0 && coinIndex < MoneyUtil.getAllCoins().size())
		{
			//LightmansCurrency.LOGGER.info("Removing " + (Screen.hasShiftDown() ? 5 : 1) + " coins of type '" + MoneyUtil.getAllCoins().get(coinIndex).getRegistryName().toString() + "' from the input value.");
			this.coinValue.removeValue(MoneyUtil.getAllCoins().get(coinIndex), Screen.hasShiftDown() ? 5 : 1);
			this.parent.OnCoinValueChanged(this);
		}
		else
		{
			LightmansCurrency.LogError("Invalid index (" + coinIndex + ") found for the decreasing button.");
		}
			
		
	}
	
	public CoinValue getCoinValue()
	{
		return this.coinValue;
	}
	
	public static interface ICoinValueInput
	{
		public <T extends AbstractWidget> T addCustomWidget(T widget);
		public int getWidth();
		public Font getFont();
		public void OnCoinValueChanged(CoinValueInput input);
	}

	@Override
	public void updateNarration(NarrationElementOutput p_169152_) {
		// TODO Auto-generated method stub
		
	}

}
