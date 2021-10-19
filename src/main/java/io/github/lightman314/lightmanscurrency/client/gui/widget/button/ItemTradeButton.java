package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces.ITradeButtonContainer;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemTradeButton extends Button{
	
	public static final ResourceLocation TRADE_TEXTURES = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/traderbuttons.png");
	
	public static final int WIDTH = 79;
	public static final int HEIGHT = 18;
	
	public static final float TEXTPOS1_X = WIDTH - 20f;
	public static final float TEXTPOS2_X = 20f;
	public static final float TEXTPOS_Y = 5F;
	
	public static final int SLOT_OFFSET1_X = WIDTH - 17;
	public static final int SLOT_OFFSET2_X = 1;
	public static final int SLOT_OFFSET_Y = 1;
	
	int tradeIndex;
	Slot itemDisplaySlot;
	Supplier<IItemTrader> source;
	ITradeButtonContainer container;
	UUID traderID;
	Screen screen;
	
	FontRenderer font;
	
	public ItemTradeButton(int x, int y, IPressable pressable, int tradeIndex, Screen screen, FontRenderer font, Supplier<IItemTrader> source, ITradeButtonContainer container)
	{
		super(x, y, WIDTH, HEIGHT, ITextComponent.getTextComponentOrEmpty(""), pressable);
		this.tradeIndex = tradeIndex;
		this.tradeIndex = tradeIndex;
		this.screen = screen;
		this.font = font;
		this.source = source;
		this.container = container;
	}
	
	private ItemTradeData getTrade() { return this.container.GetTrade(this.tradeIndex); }
	
	@Override
	public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		
		//Set active status
		this.active = isActive(this.getTrade(), this.container.getTrader());
		renderItemTradeButton(matrixStack, this.screen, this.font, this.x, this.y, this.tradeIndex, this.container.getTrader(), this.container, this.isHovered, false, false);
		
	}
	
	@SuppressWarnings("deprecation")
	public static void renderItemTradeButton(MatrixStack matrixStack, Screen screen, FontRenderer font, int x, int y, int tradeIndex, IItemTrader trader, @Nullable ITradeButtonContainer container, boolean hovered, boolean forceActive, boolean inverted)
	{
		ItemTradeData trade = trader.getTrade(tradeIndex);
		Minecraft.getInstance().getTextureManager().bindTexture(TRADE_TEXTURES);
		boolean active = forceActive ? true : isActive(trade, trader);
		
		if(active)
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		else
			RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);
		int offset = getRenderYOffset(trade.getTradeType());
		if(hovered)
			offset += HEIGHT;
		//Draw Button BG
		screen.blit(matrixStack, x, y, inverted ? WIDTH : 0, offset, WIDTH, HEIGHT);
		
		boolean hasPermission = forceActive ? true : false;
		boolean hasDiscount = false;
		boolean isValid = forceActive ? true : trade.isValid();
		boolean hasStock = forceActive ? true : trade.hasStock(trader);
		boolean hasSpace = forceActive ? true : trade.hasSpace(trader);
		boolean canAfford = forceActive ? true : false;
		CoinValue cost = trade.getCost();
		if(!forceActive && container != null)
		{
			//Discount check
			TradeCostEvent event = container.TradeCostEvent(trader.getTrade(tradeIndex));
			cost = event.getCostResult();
			hasDiscount = event.getCostMultiplier() != 1d;
			//Permission
			hasPermission = container.PermissionToTrade(tradeIndex);
			//CanAfford
			canAfford = canAfford(trade, container);
		}
		
		if(trade.isBarter())
		{
			
			//Render the barter item
			int xPos = x + (inverted ? SLOT_OFFSET1_X : SLOT_OFFSET2_X);
			ItemRenderUtil.drawItemStack(screen, font, trade.getBarterItem(), xPos, y + SLOT_OFFSET_Y, true);
			
			String text = getTradeText(CoinValue.EMPTY, false, isValid, hasStock, hasSpace, hasPermission);
			int textColor = getTradeTextColor(trade.isValid(), canAfford, hasStock, hasPermission, false);
			if(text == "" && !canAfford)
			{
				text = "X";
				textColor = 0xFF0000;
			}
			int textLength = font.getStringWidth(text);
			
			font.drawString(matrixStack, text, x + (WIDTH / 2) - (textLength / 2), y + TEXTPOS_Y, textColor);
			
		}
		else
		{
			String tradeText = getTradeText(cost, trade.isFree(), isValid, hasStock, hasSpace, hasPermission);
			int tradeColor = getTradeTextColor(trade.isValid(), canAfford, hasStock, hasPermission, hasDiscount);
			if(inverted)
			{
				//Inverted now has the sell item on the left side
				font.drawString(matrixStack, tradeText, x + TEXTPOS2_X, y + TEXTPOS_Y, tradeColor);
			}
			else
			{
				//Default now has sell item on the right to remove the need to move it when 
				int stringLength = font.getStringWidth(tradeText);
				font.drawString(matrixStack, tradeText, x + TEXTPOS1_X - stringLength, y + TEXTPOS_Y, tradeColor);
			}
		}
		int xPos = x + (inverted ? SLOT_OFFSET2_X : SLOT_OFFSET1_X);
		//Render the sell item
		ItemRenderUtil.drawItemStack(screen, font, trade.getSellItem(), xPos, y + SLOT_OFFSET_Y, true);
		
	}
	
	public void tryRenderTooltip(MatrixStack matrixStack, Screen screen, IItemTrader trader, boolean inverted, int mouseX, int mouseY)
	{
		if(this.isHovered)
			tryRenderTooltip(matrixStack, screen, this.getTrade(), trader, this.x, this.y, inverted, mouseX, mouseY);
	}
	
	public static int tryRenderTooltip(MatrixStack matrixStack, Screen screen, ItemTradeData trade, IItemTrader trader, int x, int y, boolean inverted, int mouseX, int mouseY)
	{
		switch(trade.getTradeType())
		{
		case BARTER:
			if(isMouseOverSlot(1, x, y, mouseX, mouseY, inverted))
			{
				//Render tooltip for barter item
				List<ITextComponent> tooltip = getTooltipForItem(screen, trade, 1, trader);
				if(tooltip != null)
				{
					screen.func_243308_b(matrixStack, tooltip, mouseX, mouseY);
					return 2;
				}
				return -2;
			}
		default:
			if(isMouseOverSlot(0, x, y, mouseX, mouseY, inverted))
			{
				//Render tooltip for sell item
				List<ITextComponent> tooltip = getTooltipForItem(screen, trade, 0, trader);
				if(tooltip != null)
				{
					screen.func_243308_b(matrixStack, tooltip, mouseX, mouseY);
					return 1;
				}
				return -1;
			}
		}
		return 0;
	}
	
	public static boolean isMouseOverSlot(int slotIndex, int x, int y, int mouseX, int mouseY, boolean inverted)
	{
		int minX = x + (slotIndex == 1 ? SLOT_OFFSET2_X : SLOT_OFFSET1_X);
		if(inverted)
			minX = x + (slotIndex == 1 ? SLOT_OFFSET1_X : SLOT_OFFSET2_X);
		return mouseX >= minX && mouseX <= (minX + 16) && mouseY >= y + 1 && mouseY <= (y + HEIGHT - 1);
	}
	
	public static List<ITextComponent> getTooltipForItem(Screen screen, ItemTradeData trade, int slot, IItemTrader trader)
	{
		ItemStack itemStack = slot == 1 ? trade.getBarterItem() : trade.getSellItem();
		if(itemStack.isEmpty())
			return null;
		if(!trade.getCustomName().isEmpty() && (trade.isSale() || (trade.isBarter() && slot != 1)))
			itemStack.setDisplayName(new StringTextComponent("§6" + trade.getCustomName()));
		List<ITextComponent> tooltips = screen.getTooltipFromItem(itemStack);
		//If this is the sell item, give tooltips otherwise do nothing
		if(slot != 1)
		{
			//Info
			tooltips.add(new TranslationTextComponent("tooltip.lightmanscurrency.trader.info"));
			//Custom Name
			if(!trade.getCustomName().isEmpty())
				tooltips.add(new TranslationTextComponent("tooltip.lightmanscurrency.trader.originalname", trade.getSellItem().getDisplayName()));
			//Stock
			tooltips.add(new TranslationTextComponent("tooltip.lightmanscurrency.trader.stock", trader.isCreative() ? new TranslationTextComponent("tooltip.lightmanscurrency.trader.stock.infinite") : new StringTextComponent("§6" + trade.stockCount(trader))));
			
			//Nothing else to add yet
		}
		return tooltips;
	}
	
	public static String getTradeText(CoinValue cost, boolean isFree, boolean isValid, boolean hasStock, boolean hasSpace, boolean hasPermission)
	{
		if(isValid && !hasPermission)
			return new TranslationTextComponent("tooltip.lightmanscurrency.denied").getString();
		else if(isValid && !hasStock)
			return new TranslationTextComponent("tooltip.lightmanscurrency.outofstock").getString();
		else if(isValid && !hasSpace)
			return new TranslationTextComponent("tooltip.lightmanscurrency.outofspace").getString();
		else if(isValid && isFree)
			return new TranslationTextComponent("gui.button.lightmanscurrency.free").getString();
		else
			return cost.getString();
	}
	
	public static int getTradeTextColor(boolean isValid, boolean canAfford, boolean hasStock, boolean hasPermission, boolean hasDiscount)
	{
		if((isValid && !hasStock) || !canAfford || !hasPermission)
			return 0xFF0000;
		else if(hasDiscount)
			return 0x00FF00;
		return 0xFFFFFF;
	}
	
	public static int getRenderYOffset(ItemTradeData.ItemTradeType tradeDirection)
	{
		if(tradeDirection == ItemTradeData.ItemTradeType.PURCHASE)
			return HEIGHT * 2;
		else if(tradeDirection == ItemTradeData.ItemTradeType.BARTER)
			return HEIGHT * 4;
		//LightmansCurrency.LogWarning("Could not get Y render offset for TradeDirection." + tradeDirection.name());
		return 0;
	}
	
	protected static boolean canAfford(ItemTradeData trade, ITradeButtonContainer container)
	{
		if(trade.isSale())
		{
			if(trade.isFree())
				return true;
			else
				return container.GetCoinValue() >= trade.getCost().getRawValue();
		}
		else if(trade.isPurchase())
		{
			return InventoryUtil.GetItemCount(container.GetItemInventory(), trade.getSellItem()) >= trade.getSellItem().getCount();
		}
		else if(trade.isBarter())
		{
			return InventoryUtil.GetItemCount(container.GetItemInventory(), trade.getBarterItem()) >= trade.getBarterItem().getCount();
		}
		return true;
	}
	
	public static boolean isActive(ItemTradeData trade, IItemTrader trader)
	{
		if(trade.isValid())
		{
			//Return whether we have enough of the item we're selling in stock.
			return trader.isCreative() || trade.hasStock(trader);
		}
		return false;
	}

}
