package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import java.util.List;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.PlayerContainer;
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
	public static final ResourceLocation DEFAULT_BACKGROUND = new ResourceLocation(LightmansCurrency.MODID, "items/empty_item_slot");
	public static final Pair<ResourceLocation,ResourceLocation> BACKGROUND = Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, DEFAULT_BACKGROUND);
	
	public static final int WIDTH = 79;
	public static final int HEIGHT = 18;
	
	public static final float TEXTPOS1_X = WIDTH - 20f;
	public static final float TEXTPOS2_X = 20f;
	public static final float TEXTPOS_Y = 5F;
	
	public static final int SLOT_OFFSET1_X = WIDTH - 17;
	public static final int SLOT_OFFSET2_X = 1;
	public static final int SLOT_OFFSET_Y = 1;
	
	int tradeIndex;
	Supplier<IItemTrader> source;
	Supplier<Long> availableCoins;
	Supplier<IInventory> itemSlots;
	Screen screen;
	
	FontRenderer font;
	
	public ItemTradeButton(int x, int y, IPressable pressable, int tradeIndex, Screen screen, FontRenderer font, Supplier<IItemTrader> source, Supplier<Long> availableCoins, Supplier<IInventory> itemSlots)
	{
		super(x, y, WIDTH, HEIGHT, new StringTextComponent(""), pressable);
		this.tradeIndex = tradeIndex;
		this.screen = screen;
		this.font = font;
		this.source = source;
		this.availableCoins = availableCoins;
		this.itemSlots = itemSlots;
	}
	
	private ItemTradeData getTrade() { return this.source.get().getTrade(this.tradeIndex); }
	
	@Override
	public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		
		//Set active status
		this.active = isActive(this.getTrade(), this.source.get());
		renderItemTradeButton(matrixStack, this.screen, this.font, this.x, this.y, this.tradeIndex, this.source.get(), false, this.isHovered, false, this.availableCoins.get(), this.itemSlots.get());
		
	}
	
	public static void renderItemTradeButton(MatrixStack matrixStack, Screen screen, FontRenderer font, int x, int y, int tradeIndex, IItemTrader trader, boolean inverted)
	{
		renderItemTradeButton(matrixStack, screen, font, x, y, tradeIndex, trader, inverted, false, true, 0, new Inventory(1));
	}
	
	@SuppressWarnings("deprecation")
	private static void renderItemTradeButton(MatrixStack matrixStack, Screen screen, FontRenderer font, int x, int y, int tradeIndex, IItemTrader trader, boolean inverted, boolean hovered, boolean forceActive, long availableCoins, IInventory itemSlots)
	{
		Minecraft minecraft = Minecraft.getInstance();
		PlayerEntity player = minecraft.player;
		
		ItemTradeData trade = trader.getTrade(tradeIndex);
		minecraft.getTextureManager().bindTexture(TRADE_TEXTURES);
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
		boolean hasStock = forceActive ? true : trade.hasStock(trader) || trader.getCoreSettings().isCreative();
		boolean hasSpace = forceActive ? true : trade.hasSpace(trader) || trader.getCoreSettings().isCreative();
		boolean canAfford = forceActive ? true : false;
		CoinValue cost = trade.getCost();
		if(!forceActive)
		{
			//Discount check
			TradeCostEvent event = trader.runTradeCostEvent(player, tradeIndex);
			cost = event.getCostResult();
			hasDiscount = event.getCostMultiplier() != 1d;
			//Permission
			hasPermission = !trader.runPreTradeEvent(player, tradeIndex).isCanceled();
			//CanAfford
			canAfford = canAfford(trade, availableCoins, itemSlots);
		}
		
		if(trade.isBarter())
		{
			
			//Render the barter item
			int xPos = x + (inverted ? SLOT_OFFSET1_X : SLOT_OFFSET2_X);
			if(trade.getBarterItem().isEmpty() && forceActive)
			{
				//Render empty slot background for empty barter slot
				xPos = x + (inverted ? SLOT_OFFSET1_X : SLOT_OFFSET2_X);
				ItemRenderUtil.drawSlotBackground(matrixStack, xPos, y + SLOT_OFFSET_Y, BACKGROUND);
			}
			else if(!trade.getBarterItem().isEmpty())
				ItemRenderUtil.drawItemStack(screen, font, trade.getBarterItem(), xPos, y + SLOT_OFFSET_Y, true);
			
			//Render barter item text
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
			String tradeText = getTradeText(cost, trade.getCost().isFree(), isValid, hasStock, hasSpace, hasPermission);
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
		ItemStack sellItem = trade.getSellItem();
		if(sellItem.isEmpty() && forceActive)
		{
			//Render empty slot backgrounds for special trade types
			Pair<ResourceLocation,ResourceLocation> background = trade.getRestriction().getEmptySlotBG();
			if(background == null)
				background = BACKGROUND;
			if(background != null)
			{
				ItemRenderUtil.drawSlotBackground(matrixStack, xPos, y + SLOT_OFFSET_Y, background);
			}
		}
		else if(!trade.getSellItem().isEmpty())
			ItemRenderUtil.drawItemStack(screen, font, trade.getSellItem(), xPos, y + SLOT_OFFSET_Y, true);
		
		
	}
	
	public void tryRenderTooltip(MatrixStack matrixStack, Screen screen, IItemTrader trader, boolean inverted, int mouseX, int mouseY)
	{
		if(this.isHovered)
			tryRenderTooltip(matrixStack, screen, this.tradeIndex, trader, this.x, this.y, inverted, mouseX, mouseY);
	}
	
	public static int tryRenderTooltip(MatrixStack matrixStack, Screen screen, int tradeIndex, IItemTrader trader, int x, int y, boolean inverted, int mouseX, int mouseY)
	{
		switch(trader.getTrade(tradeIndex).getTradeType())
		{
		case BARTER:
			if(isMouseOverSlot(1, x, y, mouseX, mouseY, inverted))
			{
				//Render tooltip for barter item
				List<ITextComponent> tooltip = getTooltipForItem(screen, tradeIndex, 1, trader);
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
				List<ITextComponent> tooltip = getTooltipForItem(screen, tradeIndex, 0, trader);
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
	
	public static List<ITextComponent> getTooltipForItem(Screen screen, int tradeIndex, int slot, IItemTrader trader)
	{
		Minecraft minecraft = Minecraft.getInstance();
		PlayerEntity player = minecraft.player;
		
		ItemTradeData trade = trader.getTrade(tradeIndex);
		ItemStack itemStack = slot == 1 ? trade.getBarterItem() : trade.getSellItem();
		if(itemStack.isEmpty())
			return null;
		//if(!trade.getCustomName().isEmpty() && (trade.isSale() || (trade.isBarter() && slot != 1)))
		//	itemStack.setDisplayName();
		List<ITextComponent> tooltips = screen.getTooltipFromItem(itemStack);
		ITextComponent originalName = null;
		if(!trade.getCustomName().isEmpty() && (trade.isSale() || (trade.isBarter() && slot != 1)))
		{
			originalName = tooltips.get(0);
			tooltips.set(0, new StringTextComponent("§6" + trade.getCustomName()));
		}
		//If this is the sell item, give tooltips otherwise do nothing
		if(slot != 1)
		{
			//Info
			tooltips.add(new TranslationTextComponent("tooltip.lightmanscurrency.trader.info"));
			//Custom Name
			if(originalName != null)
				tooltips.add(new TranslationTextComponent("tooltip.lightmanscurrency.trader.originalname", originalName));
			//Stock
			tooltips.add(new TranslationTextComponent("tooltip.lightmanscurrency.trader.stock", trader.getCoreSettings().isCreative() ? new TranslationTextComponent("tooltip.lightmanscurrency.trader.stock.infinite") : new StringTextComponent("§6" + trade.stockCount(trader))));
			//If denied, give denial reason
			PreTradeEvent pte = trader.runPreTradeEvent(player, tradeIndex);
			if(pte.isCanceled())
				pte.getDenialReasons().forEach(reason -> tooltips.add(reason));
			
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
		else if(isValid && cost.isValid())
			return cost.getString();
		else
			return "";
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
	
	protected static boolean canAfford(ItemTradeData trade, long availableCoins, IInventory itemSlots)
	{
		if(trade.isSale())
		{
			if(trade.getCost().isFree())
				return true;
			else
				return availableCoins >= trade.getCost().getRawValue();
		}
		else if(trade.isPurchase())
		{
			return InventoryUtil.GetItemCount(itemSlots, trade.getSellItem()) >= trade.getSellItem().getCount();
		}
		else if(trade.isBarter())
		{
			return InventoryUtil.GetItemCount(itemSlots, trade.getBarterItem()) >= trade.getBarterItem().getCount();
		}
		return true;
	}
	
	public static boolean isActive(ItemTradeData trade, IItemTrader trader)
	{
		if(trade.isValid())
		{
			//Return whether we have enough of the item we're selling in stock.
			return trader.getCoreSettings().isCreative() || trade.hasStock(trader);
		}
		return false;
	}

}
