package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces.ITradeButtonContainer;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemTradeButton extends Button{
	
	public static final ResourceLocation TRADE_TEXTURES = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/traderbuttons.png");
	public static final ResourceLocation DEFAULT_BACKGROUND = new ResourceLocation(LightmansCurrency.MODID, "items/empty_item_slot");
	public static final Pair<ResourceLocation,ResourceLocation> BACKGROUND = Pair.of(InventoryMenu.BLOCK_ATLAS, DEFAULT_BACKGROUND);
	
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
	ITradeButtonContainer container;
	Screen screen;
	
	Font font;
	
	
	
	public ItemTradeButton(int x, int y, OnPress pressable, int tradeIndex, Screen screen, Font font, Supplier<IItemTrader> source, ITradeButtonContainer container)
	{
		super(x, y, WIDTH, HEIGHT, new TextComponent(""), pressable);
		this.tradeIndex = tradeIndex;
		this.screen = screen;
		this.font = font;
		this.source = source;
		this.container = container;
	}

	private ItemTradeData getTrade() { return this.source.get().getTrade(this.tradeIndex); }
	
	@Override
	public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		
		//Set active status
		this.active = isActive(this.getTrade(), this.source.get());
		renderItemTradeButton(poseStack, this.screen, this.font, this.x, this.y, this.tradeIndex, this.source.get(), this.container, this.isHovered, false, false);
		
	}
	
	public static void renderItemTradeButton(PoseStack poseStack, Screen screen, Font font, int x, int y, int tradeIndex, IItemTrader trader, @Nullable ITradeButtonContainer container, boolean hovered, boolean forceActive, boolean inverted)
	{
		ItemTradeData trade = trader.getTrade(tradeIndex);
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TRADE_TEXTURES);
		
		boolean active = forceActive ? true : isActive(trade, trader);
		
		if(active)
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		else
			RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
		int offset = getRenderYOffset(trade.getTradeType());
		if(hovered)
			offset += HEIGHT;
		//Draw Button BG
		screen.blit(poseStack, x, y, inverted ? WIDTH : 0, offset, WIDTH, HEIGHT);
		
		boolean hasPermission = forceActive ? true : false;
		List<Component> denialText = Lists.newArrayList();
		boolean hasDiscount = false;
		boolean isValid = forceActive ? true : trade.isValid();
		boolean hasStock = forceActive ? true : trade.hasStock(trader);
		boolean hasSpace = forceActive ? true : trade.hasSpace(trader);
		boolean canAfford = forceActive ? true : false;
		CoinValue cost = trade.getCost();
		if(!forceActive && container != null)
		{
			//Discount check
			TradeCostEvent event = container.TradeCostEvent(trade);
			cost = event.getCostResult();
			hasDiscount = event.getCostMultiplier() != 1d;
			//Permission
			hasPermission = container.PermissionToTrade(tradeIndex, denialText);
			//CanAfford
			canAfford = canAfford(trade, container);
		}
		
		if(trade.isBarter())
		{
			
			//Render the barter item
			int xPos = x + (inverted ? SLOT_OFFSET1_X : SLOT_OFFSET2_X);
			if(trade.getBarterItem().isEmpty() && forceActive)
			{
				//Render empty slot background for empty barter slot
				xPos = x + (inverted ? SLOT_OFFSET1_X : SLOT_OFFSET2_X);
				ItemRenderUtil.drawSlotBackground(poseStack, xPos, y + SLOT_OFFSET_Y, BACKGROUND);
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
			int textLength = font.width(text);
			
			font.draw(poseStack, text, x + (WIDTH / 2) - (textLength / 2), y + TEXTPOS_Y, textColor);
			
		}
		else
		{
			String tradeText = getTradeText(cost, trade.isFree(), isValid, hasStock, hasSpace, hasPermission);
			int tradeColor = getTradeTextColor(trade.isValid(), canAfford, hasStock, hasPermission, hasDiscount);
			if(inverted)
			{
				//Inverted now has the sell item on the left side
				font.draw(poseStack, tradeText, x + TEXTPOS2_X, y + TEXTPOS_Y, tradeColor);
			}
			else
			{
				//Default now has sell item on the right to remove the need to move it when 
				int stringLength = font.width(tradeText);
				font.draw(poseStack, tradeText, x + TEXTPOS1_X - stringLength, y + TEXTPOS_Y, tradeColor);
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
				ItemRenderUtil.drawSlotBackground(poseStack, xPos, y + SLOT_OFFSET_Y, background);
			}
		}
		else if(!trade.getSellItem().isEmpty())
			ItemRenderUtil.drawItemStack(screen, font, trade.getSellItem(), xPos, y + SLOT_OFFSET_Y, true);
		
		
	}
	
	public void tryRenderTooltip(PoseStack poseStack, Screen screen, IItemTrader trader, boolean inverted, int mouseX, int mouseY, @Nullable ITradeButtonContainer container)
	{
		if(this.isHovered)
			tryRenderTooltip(poseStack, screen, this.tradeIndex, trader, this.x, this.y, inverted, mouseX, mouseY, container);
	}
	
	public static int tryRenderTooltip(PoseStack poseStack, Screen screen, int tradeIndex, IItemTrader trader, int x, int y, boolean inverted, int mouseX, int mouseY, @Nullable ITradeButtonContainer container)
	{
		switch(trader.getTrade(tradeIndex).getTradeType())
		{
		case BARTER:
			if(isMouseOverSlot(1, x, y, mouseX, mouseY, inverted))
			{
				//Render tooltip for barter item
				List<Component> tooltip = getTooltipForItem(screen, tradeIndex, 1, trader, container);
				if(tooltip != null)
				{
					screen.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
					return 2;
				}
				return -2;
			}
		default:
			if(isMouseOverSlot(0, x, y, mouseX, mouseY, inverted))
			{
				//Render tooltip for sell item
				List<Component> tooltip = getTooltipForItem(screen, tradeIndex, 0, trader, container);
				if(tooltip != null)
				{
					screen.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
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
	
	public static List<Component> getTooltipForItem(Screen screen, int tradeIndex, int slot, IItemTrader trader, @Nullable ITradeButtonContainer container)
	{
		ItemTradeData trade = trader.getTrade(tradeIndex);
		ItemStack itemStack = slot == 1 ? trade.getBarterItem() : trade.getSellItem();
		if(itemStack.isEmpty())
			return null;
		//if(!trade.getCustomName().isEmpty() && (trade.isSale() || (trade.isBarter() && slot != 1)))
		//	itemStack.setDisplayName();
		List<Component> tooltips = screen.getTooltipFromItem(itemStack);
		Component originalName = null;
		if(!trade.getCustomName().isEmpty() && (trade.isSale() || (trade.isBarter() && slot != 1)))
		{
			originalName = tooltips.get(0);
			tooltips.set(0, new TextComponent("§6" + trade.getCustomName()));
		}
		//If this is the sell item, give tooltips otherwise do nothing
		if(slot != 1)
		{
			//Info
			tooltips.add(new TranslatableComponent("tooltip.lightmanscurrency.trader.info"));
			//Custom Name
			if(originalName != null)
				tooltips.add(new TranslatableComponent("tooltip.lightmanscurrency.trader.originalname", originalName));
			//Stock
			tooltips.add(new TranslatableComponent("tooltip.lightmanscurrency.trader.stock", trader.isCreative() ? new TranslatableComponent("tooltip.lightmanscurrency.trader.stock.infinite") : new TextComponent("§6" + trade.stockCount(trader))));
			//If denied, give denial reason
			List<Component> denialText = Lists.newArrayList();
			if(container != null)
			{
				if(!container.PermissionToTrade(tradeIndex, denialText))
				{
					denialText.forEach(reason -> tooltips.add(reason));
				}
			}
			
			//Nothing else to add yet
			
		}
		return tooltips;
	}
	
	public static String getTradeText(CoinValue cost, boolean isFree, boolean isValid, boolean hasStock, boolean hasSpace, boolean hasPermission)
	{
		if(isValid && !hasPermission)
			return new TranslatableComponent("tooltip.lightmanscurrency.denied").getString();
		else if(isValid && !hasStock)
			return new TranslatableComponent("tooltip.lightmanscurrency.outofstock").getString();
		else if(isValid && !hasSpace)
			return new TranslatableComponent("tooltip.lightmanscurrency.outofspace").getString();
		else if(isValid && isFree)
			return new TranslatableComponent("gui.button.lightmanscurrency.free").getString();
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
