package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import java.util.UUID;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces.ITradeButtonContainer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces.ITradeButtonStockSource;
import io.github.lightman314.lightmanscurrency.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.tradedata.ItemTradeData.ItemTradeType;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemTradeButton extends Button{
	
	public static final ResourceLocation TRADE_TEXTURES = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/traderbuttons.png");
	
	public static final int WIDTH = 79;
	public static final int HEIGHT = 18;
	
	public static final float TEXTPOS_X = 20f;
	public static final float TEXTPOS_Y = 5F;
	
	public static final int SLOT_OFFSET_X = 1;
	public static final int SLOT_OFFSET_Y = 1;
	
	ItemTradeData trade;
	Slot itemDisplaySlot;
	Supplier<ITradeButtonStockSource> source;
	ITradeButtonContainer container;
	UUID traderID;
	
	FontRenderer font;
	
	public ItemTradeButton(int x, int y, IPressable pressable, ItemTradeData trade, FontRenderer font, Supplier<ITradeButtonStockSource> source)
	{
		this(x,y,pressable,trade,font,source, null);
	}
	
	public ItemTradeButton(int x, int y, IPressable pressable, ItemTradeData trade, FontRenderer font, Supplier<ITradeButtonStockSource> source, ITradeButtonContainer container)
	{
		super(x, y, WIDTH, HEIGHT, ITextComponent.getTextComponentOrEmpty(""), pressable);
		this.trade = trade;
		this.font = font;
		this.source = source;
		this.container = container;
	}
	
	/**
	 * Updates the trade data for this buttons trade.
	 * @param trade The updated trade data.
	 */
	public void UpdateTrade(ItemTradeData trade)
	{
		this.trade = trade;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		//Set active status
		this.active = isActive();
		Minecraft.getInstance().getTextureManager().bindTexture(TRADE_TEXTURES);
		
		if(this.active)
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		else
			RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);
		int offset = getRenderYOffset(this.trade.getTradeDirection());
		if(this.isHovered)
			offset += HEIGHT;
		//Draw Button BG
		this.blit(matrixStack, this.x, this.y, 0, offset, WIDTH, HEIGHT);
		
		this.font.drawString(matrixStack, getTradeText(this.trade, hasStock(), hasSpace()), this.x + TEXTPOS_X, this.y + TEXTPOS_Y, getTradeTextColor(this.trade, canAfford(), hasStock()));
		
		/*if(trade.isValid() && !trade.hasStock(this.tileEntity) && !this.tileEntity.isCreative()) //Display the No Stock message if the trade is valid, but we're out of stock
			this.font.drawString(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.outofstock").getString(), this.x + TEXTPOS_X, this.y + TEXTPOS_Y, 0xFF0000);
		else if(canAfford())
			this.font.drawString(matrixStack, MoneyUtil.getStringOfValue(this.trade.getCost()), this.x + TEXTPOS_X, this.y + TEXTPOS_Y, 0xFFFFFF);
		else
			this.font.drawString(matrixStack, MoneyUtil.getStringOfValue(this.trade.getCost()), this.x + TEXTPOS_X, this.y + TEXTPOS_Y, 0xFF0000);*/
		
	}
	
	public static String getTradeText(ItemTradeData trade, boolean hasStock, boolean hasSpace)
	{
		if(trade.isValid() && !hasStock)
			return new TranslationTextComponent("tooltip.lightmanscurrency.outofstock").getString();
		else if(trade.isValid() && !hasSpace)
			return new TranslationTextComponent("tooltip.lightmanscurrency.outofspace").getString();
		else if(trade.isFree())
			return new TranslationTextComponent("gui.button.lightmanscurrency.free").getString();
		else
			return trade.getCost().getString();
	}
	
	public static int getTradeTextColor(ItemTradeData trade, boolean canAfford, boolean hasStock)
	{
		if((trade.isValid() && !hasStock) || !canAfford)
			return 0xFF0000;
		return 0xFFFFFF;
	}
	
	public static int getRenderYOffset(ItemTradeData.ItemTradeType tradeDirection)
	{
		if(tradeDirection == ItemTradeData.ItemTradeType.PURCHASE)
			return HEIGHT * 2;
		//LightmansCurrency.LogWarning("Could not get Y render offset for TradeDirection." + tradeDirection.name());
		return 0;
	}
	
	protected boolean canAfford()
	{
		if(this.trade.getTradeDirection() == ItemTradeType.SALE)
		{
			if(this.trade.isFree())
				return true;
			else if(this.container != null)
			{
				return this.container.GetCoinValue() >= this.trade.getCost().getRawValue();
			}
		}
		else if(this.trade.getTradeDirection() == ItemTradeType.PURCHASE)
		{
			return InventoryUtil.GetItemCount(this.container.GetItemInventory(), this.trade.getSellItem()) >= this.trade.getSellItem().getCount();
		}
		return true;
	}
	
	protected boolean hasStock()
	{
		if(trade.getTradeDirection() == ItemTradeType.SALE)
		{
			return trade.hasStock(this.source.get().getStorage()) || this.source.get().isCreative();
		}
		else if(trade.getTradeDirection() == ItemTradeType.PURCHASE)
		{
			return trade.hasEnoughMoney(this.source.get().getStoredMoney()) || this.source.get().isCreative();
		}
		return false;
	}
	
	protected boolean hasSpace()
	{
		if(trade.getTradeDirection() == ItemTradeType.PURCHASE)
			return InventoryUtil.CanPutItemStack(this.source.get().getStorage(), this.trade.getSellItem());
		return true;
	}
	
	protected boolean isActive()
	{
		if(trade.isValid())
		{
			if(trade.getTradeDirection() == ItemTradeType.SALE)
			{
				//Return whether we have enough of the item we're selling in stock.
				return this.source.get().isCreative() || trade.hasStock(this.source.get().getStorage());
			}
			else if(trade.getTradeDirection() == ItemTradeType.PURCHASE)
			{
				//Return whether we have enough money to pay for the items we're buying.
				//Confirm that there's enough room to place the intended items in storage
				return (this.source.get().isCreative() || trade.hasEnoughMoney(this.source.get().getStoredMoney())) && hasSpace();
			}
		}
		return false;
	}

}
