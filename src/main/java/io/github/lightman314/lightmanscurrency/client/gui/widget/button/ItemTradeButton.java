package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import java.util.List;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
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
	Supplier<Long> availableCoins;
	Supplier<Container> itemSlots;
	Screen screen;
	
	Font font;
	
	public ItemTradeButton(int x, int y, OnPress pressable, int tradeIndex, Screen screen, Font font, Supplier<IItemTrader> source, Supplier<Long> availableCoins, Supplier<Container> itemSlots)
	{
		super(x, y, WIDTH, HEIGHT, new TextComponent(""), pressable);
		this.tradeIndex = tradeIndex;
		this.screen = screen;
		this.font = font;
		this.source = source;
		this.availableCoins = availableCoins;
		this.itemSlots = itemSlots;
	}

	private ItemTradeData getTrade() { return this.source.get().getTrade(this.tradeIndex); }
	
	@Override
	public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		
		//Set active status
		this.active = isActive(this.getTrade(), this.source.get());
		renderItemTradeButton(poseStack, this.screen, this.font, this.x, this.y, this.tradeIndex, this.source.get(), false, this.isHovered, false, this.availableCoins.get(), this.itemSlots.get());
		
	}
	
	/**
	 * Dummy render function for outside use.
	 * Renders a forced-active state of the button for use on Item Edit or Trader Storage screens, etc.
	 */
	public static void renderItemTradeButton(PoseStack poseStack, Screen screen, Font font, int x, int y, int tradeIndex, IItemTrader trader, boolean inverted)
	{
		renderItemTradeButton(poseStack, screen, font, x, y, tradeIndex, trader, inverted, false, true, 0, new SimpleContainer(1));
	}
	
	public static void renderItemTradeButton(PoseStack poseStack, Screen screen, Font font, int x, int y, ItemTradeData trade) {
		
		RenderSystem.setShaderTexture(0, TRADE_TEXTURES);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		int offset = getRenderYOffset(trade.getTradeType());
		
		//Draw Button BG
		screen.blit(poseStack, x, y, 0, offset, WIDTH, HEIGHT);
		
		CoinValue cost = trade.getCost();
		
		if(trade.isBarter())
		{
			
			//Render the barter item
			int xPos = x + SLOT_OFFSET2_X;
			if(!trade.getBarterItem().isEmpty())
				ItemRenderUtil.drawItemStack(screen, font, trade.getBarterItem(), xPos, y + SLOT_OFFSET_Y);
			
			//Render barter item text
			String text = getTradeText(CoinValue.EMPTY, true, true, true, true);
			int textColor = getTradeTextColor(trade.isValid(), true, true, true, false);
			int textLength = font.width(text);
			
			font.draw(poseStack, text, x + (WIDTH / 2) - (textLength / 2), y + TEXTPOS_Y, textColor);
			
		}
		else
		{
			String tradeText = getTradeText(cost, true, true, true, true);
			int tradeColor = getTradeTextColor(trade.isValid(), true, true, true, true);
			//Default now has sell item on the right to remove the need to move it when 
			int stringLength = font.width(tradeText);
			font.draw(poseStack, tradeText, x + TEXTPOS1_X - stringLength, y + TEXTPOS_Y, tradeColor);
		}
		int xPos = x + SLOT_OFFSET1_X;
		//Render the sell item
		if(!trade.getSellItem().isEmpty())
			ItemRenderUtil.drawItemStack(screen, font, trade.getSellItem(), xPos, y + SLOT_OFFSET_Y);
		
	}
	
	private static void renderItemTradeButton(PoseStack poseStack, Screen screen, Font font, int x, int y, int tradeIndex, IItemTrader trader, boolean inverted, boolean hovered, boolean forceActive, long availableCoins, Container itemSlots)
	{
		
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		
		ItemTradeData trade = trader.getTrade(tradeIndex);
		
		//RenderSystem.setShader(GameRenderer::getPositionTexShader);
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
		boolean hasDiscount = false;
		boolean isValid = forceActive ? true : trade.isValid();
		boolean hasStock = forceActive ? true : trader.getCoreSettings().isCreative() || trade.hasStock(trader);
		boolean hasSpace = forceActive ? true : trader.getCoreSettings().isCreative() || trade.hasSpace(trader);
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
				ItemRenderUtil.drawSlotBackground(poseStack, xPos, y + SLOT_OFFSET_Y, BACKGROUND);
			}
			else if(!trade.getBarterItem().isEmpty())
				ItemRenderUtil.drawItemStack(screen, font, trade.getBarterItem(), xPos, y + SLOT_OFFSET_Y);
			
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
			String tradeText = getTradeText(cost, trade.getCost().isFree(), isValid, hasStock, hasSpace, hasPermission);
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
			ItemRenderUtil.drawItemStack(screen, font, trade.getSellItem(), xPos, y + SLOT_OFFSET_Y);
		
		
	}
	
	public void tryRenderTooltip(PoseStack poseStack, Screen screen, IItemTrader trader, boolean inverted, int mouseX, int mouseY)
	{
		if(this.isHovered)
			tryRenderTooltip(poseStack, screen, this.tradeIndex, trader, this.x, this.y, inverted, mouseX, mouseY);
	}
	
	public static int tryRenderTooltip(PoseStack poseStack, Screen screen, ItemTradeData trade, int x, int y, int mouseX, int mouseY)
	{
		switch(trade.getTradeType())
		{
		case BARTER:
			if(isMouseOverSlot(1, x, y, mouseX, mouseY, false))
			{
				//Render tooltip for barter item
				List<Component> tooltip = getTooltipForItem(screen, trade, 1);
				if(tooltip != null)
				{
					screen.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
					return 2;
				}
				return -2;
			}
		default:
			if(isMouseOverSlot(0, x, y, mouseX, mouseY, false))
			{
				//Render tooltip for sell item
				List<Component> tooltip = getTooltipForItem(screen, trade, 0);
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
	
	public static int tryRenderTooltip(PoseStack poseStack, Screen screen, int tradeIndex, IItemTrader trader, int x, int y, boolean inverted, int mouseX, int mouseY)
	{
		switch(trader.getTrade(tradeIndex).getTradeType())
		{
		case BARTER:
			if(isMouseOverSlot(1, x, y, mouseX, mouseY, inverted))
			{
				//Render tooltip for barter item
				List<Component> tooltip = getTooltipForItem(screen, tradeIndex, 1, trader);
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
				List<Component> tooltip = getTooltipForItem(screen, tradeIndex, 0, trader);
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
	
	public static List<Component> getTooltipForItem(Screen screen, ItemTradeData trade, int slot)
	{
		ItemStack itemStack = slot == 1 ? trade.getBarterItem() : trade.getSellItem();
		if(itemStack.isEmpty())
			return null;
		
		List<Component> tooltips = screen.getTooltipFromItem(itemStack);
		Component originalName = null;
		if(!trade.getCustomName().isEmpty() && (trade.isSale() || (trade.isBarter() && slot != 1)))
		{
			originalName = tooltips.get(0);
			tooltips.set(0, new TextComponent("§6" + trade.getCustomName()));
			tooltips.add(new TranslatableComponent("tooltip.lightmanscurrency.trader.originalname", originalName));
		}
		return tooltips;
	}
	
	public static List<Component> getTooltipForItem(Screen screen, int tradeIndex, int slot, IItemTrader trader)
	{
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		
		ItemTradeData trade = trader.getTrade(tradeIndex);
		ItemStack itemStack = slot == 1 ? trade.getBarterItem() : trade.getSellItem();
		if(itemStack.isEmpty())
			return null;
		
		List<Component> tooltips = screen.getTooltipFromItem(itemStack);
		Component originalName = null;
		if(!trade.getCustomName().isEmpty() && (trade.isSale() || (trade.isBarter() && slot != 1)))
		{
			originalName = tooltips.get(0);
			tooltips.set(0, new TextComponent("§6" + trade.getCustomName()));
		}
		//If this is the sell item, give tooltips otherwise do nothing
		if(slot == 0)
		{
			//Info
			tooltips.add(new TranslatableComponent("tooltip.lightmanscurrency.trader.info"));
			//Custom Name
			if(originalName != null)
				tooltips.add(new TranslatableComponent("tooltip.lightmanscurrency.trader.originalname", originalName));
			//Stock
			tooltips.add(new TranslatableComponent("tooltip.lightmanscurrency.trader.stock", trader.getCoreSettings().isCreative() ? new TranslatableComponent("tooltip.lightmanscurrency.trader.stock.infinite") : new TextComponent("§6" + trade.stockCount(trader))));
			//If denied, give denial reason
			PreTradeEvent pte = trader.runPreTradeEvent(player, tradeIndex);
			if(pte.isCanceled())
				pte.getDenialReasons().forEach(reason -> tooltips.add(reason));
			
			//Nothing else to add yet
			
		}
		return tooltips;
	}
	
	@Deprecated /** @deprecated use version without isFree flag. */
	public static String getTradeText(CoinValue cost, boolean isFree, boolean isValid, boolean hasStock, boolean hasSpace, boolean hasPermission) {
		return getTradeText(cost, isValid, hasStock, hasSpace, hasPermission);
	}
	
	public static String getTradeText(CoinValue cost, boolean isValid, boolean hasStock, boolean hasSpace, boolean hasPermission)
	{
		if(isValid && !hasPermission)
			return new TranslatableComponent("tooltip.lightmanscurrency.denied").getString();
		else if(isValid && !hasStock)
			return new TranslatableComponent("tooltip.lightmanscurrency.outofstock").getString();
		else if(isValid && !hasSpace)
			return new TranslatableComponent("tooltip.lightmanscurrency.outofspace").getString();
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
	
	protected static boolean canAfford(ItemTradeData trade, long availableCoins, Container itemSlots)
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
			return trader.getCoreSettings().isCreative() || (trade.hasStock(trader) && trade.hasSpace(trader));
		}
		return false;
	}

}
