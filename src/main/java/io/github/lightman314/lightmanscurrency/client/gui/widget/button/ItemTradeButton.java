package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import java.util.UUID;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces.ITradeButtonContainer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces.ITradeButtonStockSource;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.tradedata.ItemTradeData.ItemTradeType;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
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
	
	int tradeIndex;
	Slot itemDisplaySlot;
	Supplier<ITradeButtonStockSource> source;
	ITradeButtonContainer container;
	UUID traderID;
	
	FontRenderer font;
	
	public ItemTradeButton(int x, int y, IPressable pressable, int tradeIndex, FontRenderer font, Supplier<ITradeButtonStockSource> source)
	{
		this(x,y,pressable, tradeIndex,font,source, null);
	}
	
	public ItemTradeButton(int x, int y, IPressable pressable, int tradeIndex, FontRenderer font, Supplier<ITradeButtonStockSource> source, ITradeButtonContainer container)
	{
		super(x, y, WIDTH, HEIGHT, ITextComponent.getTextComponentOrEmpty(""), pressable);
		this.tradeIndex = tradeIndex;
		this.tradeIndex = tradeIndex;
		this.font = font;
		this.source = source;
		this.container = container;
	}
	
	private ItemTradeData getTrade() { return this.container.GetTrade(this.tradeIndex); }
	
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
		int offset = getRenderYOffset(this.getTrade().getTradeDirection());
		if(this.isHovered)
			offset += HEIGHT;
		//Draw Button BG
		this.blit(matrixStack, this.x, this.y, 0, offset, WIDTH, HEIGHT);
		
		boolean hasPermission = hasPermission();
		boolean hasDiscount = false;
		CoinValue cost = this.getTrade().getCost();
		if(container != null)
		{
			TradeCostEvent event = container.TradeCostEvent(this.getTrade());
			cost = event.getCostResult();
			hasDiscount = event.getCostMultiplier() < 1d;
		}
			
		this.font.drawString(matrixStack, getTradeText(cost, this.getTrade().isValid(), hasStock(), hasSpace(), hasPermission), this.x + TEXTPOS_X, this.y + TEXTPOS_Y, getTradeTextColor(this.getTrade(), canAfford(), hasStock(), hasPermission, hasDiscount));
		
		/*if(trade.isValid() && !trade.hasStock(this.tileEntity) && !this.tileEntity.isCreative()) //Display the No Stock message if the trade is valid, but we're out of stock
			this.font.drawString(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.outofstock").getString(), this.x + TEXTPOS_X, this.y + TEXTPOS_Y, 0xFF0000);
		else if(canAfford())
			this.font.drawString(matrixStack, MoneyUtil.getStringOfValue(this.trade.getCost()), this.x + TEXTPOS_X, this.y + TEXTPOS_Y, 0xFFFFFF);
		else
			this.font.drawString(matrixStack, MoneyUtil.getStringOfValue(this.trade.getCost()), this.x + TEXTPOS_X, this.y + TEXTPOS_Y, 0xFF0000);*/
		
	}
	
	public static String getTradeText(CoinValue cost, boolean isValid, boolean hasStock, boolean hasSpace, boolean hasPermission)
	{
		if(isValid && !hasPermission)
			return new TranslationTextComponent("tooltip.lightmanscurrency.denied").getString();
		else if(isValid && !hasStock)
			return new TranslationTextComponent("tooltip.lightmanscurrency.outofstock").getString();
		else if(isValid && !hasSpace)
			return new TranslationTextComponent("tooltip.lightmanscurrency.outofspace").getString();
		else if(cost.getRawValue() == 0) //Is free
			return new TranslationTextComponent("gui.button.lightmanscurrency.free").getString();
		else
			return cost.getString();
	}
	
	public static int getTradeTextColor(ItemTradeData trade, boolean canAfford, boolean hasStock, boolean hasPermission, boolean hasDiscount)
	{
		if((trade.isValid() && !hasStock) || !canAfford || !hasPermission)
			return 0xFF0000;
		if(hasDiscount)
			return 0x00FF00;
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
		if(this.getTrade().getTradeDirection() == ItemTradeType.SALE)
		{
			if(this.getTrade().isFree())
				return true;
			else if(this.container != null)
			{
				return this.container.GetCoinValue() >= this.getTrade().getCost().getRawValue();
			}
		}
		else if(this.getTrade().getTradeDirection() == ItemTradeType.PURCHASE)
		{
			return InventoryUtil.GetItemCount(this.container.GetItemInventory(), this.getTrade().getSellItem()) >= this.getTrade().getSellItem().getCount();
		}
		return true;
	}
	
	protected boolean hasStock()
	{
		if(getTrade().getTradeDirection() == ItemTradeType.SALE)
		{
			return getTrade().hasStock(this.source.get().getStorage()) || this.source.get().isCreative();
		}
		else if(getTrade().getTradeDirection() == ItemTradeType.PURCHASE)
		{
			return getTrade().hasEnoughMoney(this.source.get().getStoredMoney()) || this.source.get().isCreative();
		}
		return false;
	}
	
	protected boolean hasSpace()
	{
		if(getTrade().getTradeDirection() == ItemTradeType.PURCHASE)
			return InventoryUtil.CanPutItemStack(this.source.get().getStorage(), this.getTrade().getSellItem());
		return true;
	}
	
	protected boolean hasPermission()
	{
		return this.container.PermissionToTrade(this.tradeIndex);
	}
	
	protected boolean isActive()
	{
		if(getTrade().isValid())
		{
			if(getTrade().getTradeDirection() == ItemTradeType.SALE)
			{
				//Return whether we have enough of the item we're selling in stock.
				return this.source.get().isCreative() || getTrade().hasStock(this.source.get().getStorage());
			}
			else if(getTrade().getTradeDirection() == ItemTradeType.PURCHASE)
			{
				//Return whether we have enough money to pay for the items we're buying.
				//Confirm that there's enough room to place the intended items in storage
				return (this.source.get().isCreative() || getTrade().hasEnoughMoney(this.source.get().getStoredMoney())) && hasSpace();
			}
		}
		return false;
	}

}
