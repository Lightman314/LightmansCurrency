package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea.InteractionConsumer;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil.TextFormatting;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue.CoinValuePair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class TradeButton extends Button{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/trade.png");
	public static final OnPress NULL_PRESS = button -> {};
	
	public static  final int ARROW_WIDTH = 22;
	public static  final int ARROW_HEIGHT = 18;
	
	public static  final int TEMPLATE_WIDTH = 212;
	
	public static final int BUTTON_HEIGHT = 18;
	
	private final Supplier<TradeData> tradeSource;
	public TradeData getTrade() { return this.tradeSource.get(); }
	public TradeRenderManager<?> getTradeRenderer() {
		TradeData trade = this.getTrade();
		if(trade != null)
			return trade.getButtonRenderer();
		return null;
	}
	private final Supplier<TradeContext> contextSource;
	public TradeContext getContext() { return this.contextSource.get(); }
	public boolean displayOnly = false;
	
	public TradeButton(Supplier<TradeContext> contextSource, Supplier<TradeData> tradeSource, OnPress onPress) {
		super(0, 0, 0, BUTTON_HEIGHT, Component.empty(), onPress, Button.DEFAULT_NARRATION);
		this.tradeSource = tradeSource;
		this.contextSource = contextSource;
		this.recalculateSize();
	}
	
	private void recalculateSize()
	{
		TradeRenderManager<?> tr = this.getTradeRenderer();
		if(tr != null)
		{
			TradeContext context = this.getContext();
			this.width = tr.tradeButtonWidth(context);
		}
	}
	
	public void move(int x, int y) { this.setPosition(x, y); }
	
	@Override
	public void renderButton(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		TradeRenderManager<?> tr = this.getTradeRenderer();
		if(tr == null)
			return;

		TradeContext context = this.getContext();
		
		this.recalculateSize();
		
		this.renderBackground(pose, !context.isStorageMode && !this.displayOnly && this.isHovered);

		LazyOptional<ScreenPosition> arrowPosOptional = tr.arrowPosition(context);
		arrowPosOptional.ifPresent(arrowPos -> this.renderArrow(pose, arrowPos, !context.isStorageMode && !this.displayOnly && this.isHovered));


		//Render custom display stuff in front of the arrow, not behind it.
		try { tr.renderAdditional(this, pose, mouseX, mouseY, context);
		} catch(Exception e) { LightmansCurrency.LogError("Error on additional Trade Button rendering.", e); }

		this.renderAlert(pose, tr.alertPosition(context), tr.getAlertData(context));
		
		this.renderDisplays(pose, tr, context);
		
	}
	
	private void renderBackground(PoseStack pose, boolean isHovered)
	{
		if(this.width < 8)
		{
			LightmansCurrency.LogError("Cannot render a trade button that is less than 8 pixels wide!");
			return;
		}
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		if(this.active)
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		else
			RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1f);
		
		int vOffset = isHovered ? BUTTON_HEIGHT : 0;
		
		//Render the left
		this.blit(pose, this.getX(), this.getY(), 0, vOffset, 4, BUTTON_HEIGHT);
		//Render the middle
		int xOff = 4;
		while(xOff < this.width - 4)
		{
			int xRend = Math.min(this.width - 4 - xOff, TEMPLATE_WIDTH - 8);
			this.blit(pose, this.getX() + xOff, this.getY(), 4, vOffset, xRend, BUTTON_HEIGHT);
			xOff += xRend;
		}
		//Render the right
		this.blit(pose, this.getX() + this.width - 4, this.getY(), TEMPLATE_WIDTH - 4, vOffset, 4, BUTTON_HEIGHT);
	}
	
	private void renderArrow(PoseStack pose, ScreenPosition position, boolean isHovered)
	{
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		if(this.active)
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		else
			RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1f);
		
		int vOffset = isHovered ? ARROW_HEIGHT : 0;
		
		this.blit(pose, this.getX() + position.x, this.getY() + position.y, TEMPLATE_WIDTH, vOffset, ARROW_WIDTH, ARROW_HEIGHT);
		
	}
	
	private void renderAlert(PoseStack pose, ScreenPosition position, List<AlertData> alerts)
	{
		
		if(alerts == null || alerts.size() == 0)
			return;
		alerts.sort(AlertData::compare);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		alerts.get(0).setShaderColor(this.active ? 1f : 0.5f);
		
		this.blit(pose, this.getX() + position.x, this.getY() + position.y, TEMPLATE_WIDTH + ARROW_WIDTH, 0, ARROW_WIDTH, ARROW_HEIGHT);
		
	}
	
	public void renderDisplays(PoseStack pose, TradeRenderManager<?> tr, TradeContext context)
	{
		for(Pair<DisplayEntry,DisplayData> display : getInputDisplayData(tr, context))
			display.getFirst().render(this, pose, this.getX(), this.getY(), display.getSecond());
		for(Pair<DisplayEntry,DisplayData> display : getOutputDisplayData(tr, context))
			display.getFirst().render(this, pose, this.getX(), this.getY(), display.getSecond());
	}
	
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY)
	{
		if(!this.visible || !this.isMouseOver(mouseX, mouseY))
			return;
		
		TradeRenderManager<?> tr = this.getTradeRenderer();
		if(tr == null)
			return;
		
		TradeContext context = this.getContext();
		
		List<Component> tooltips = new ArrayList<>();
		
		this.tryAddTooltip(tooltips, tr.getAdditionalTooltips(context, mouseX - this.getX(), mouseY - this.getY()));
		
		for(Pair<DisplayEntry,DisplayData> display : getInputDisplayData(tr, context))
		{
			if(display.getFirst().isMouseOver(this.getX(), this.getY(), display.getSecond(), mouseX, mouseY))
				this.tryAddTooltip(tooltips, display.getFirst().tooltip);
		}
		
		for(Pair<DisplayEntry,DisplayData> display : getOutputDisplayData(tr, context))
		{
			if(display.getFirst().isMouseOver(this.getX(), this.getY(), display.getSecond(), mouseX, mouseY))
			{
				this.tryAddTooltip(tooltips, display.getFirst().tooltip);
			}
		}


		if(this.isMouseOverAlert(mouseX, mouseY, tr, context))
		{
			List<AlertData> alerts = tr.getAlertData(context);
			if(alerts != null && alerts.size() > 0)
				this.tryAddAlertTooltips(tooltips, alerts);
		}
		
		DrawTooltip(pose, mouseX, mouseY, tooltips);
		
	}
	
	private void tryAddTooltip(List<Component> tooltips, @Nullable List<Component> add)
	{
		if(add == null)
			return;
		tooltips.addAll(add);
	}
	
	private void tryAddAlertTooltips(List<Component> tooltips, @Nullable List<AlertData> alerts)
	{
		if(alerts == null)
			return;
		alerts.sort(AlertData::compare);
		for(AlertData alert : alerts)
			tooltips.add(alert.getFormattedMessage());
	}
	
	public void onInteractionClick(int mouseX, int mouseY, int button, InteractionConsumer consumer)
	{
		if(!this.visible || !this.isMouseOver(mouseX, mouseY))
			return;

		TradeData trade = this.getTrade();
		if(trade == null)
			return;
		TradeRenderManager<?> tr = trade.getButtonRenderer();
		if(tr == null)
			return;
		
		TradeContext context = this.getContext();
		
		List<Pair<DisplayEntry,DisplayData>> inputDisplays = getInputDisplayData(tr, context);
		for(int i = 0; i < inputDisplays.size(); ++i)
		{
			Pair<DisplayEntry,DisplayData> display = inputDisplays.get(i);
			if(display.getFirst().isMouseOver(this.getX(), this.getY(), display.getSecond(), mouseX, mouseY))
			{
				consumer.onTradeButtonInputInteraction(context.getTrader(), trade, i, button);
				return;
			}
		}
		
		List<Pair<DisplayEntry,DisplayData>> outputDisplays = getOutputDisplayData(tr, context);
		for(int i = 0; i < outputDisplays.size(); ++i)
		{
			Pair<DisplayEntry,DisplayData> display = outputDisplays.get(i);
			if(display.getFirst().isMouseOver(this.getX(), this.getY(), display.getSecond(), mouseX, mouseY))
			{
				consumer.onTradeButtonOutputInteraction(context.getTrader(), trade, i, button);
				return;
			}
		}
		
		//Only run the default interaction code if you didn't hit an input or output display
		consumer.onTradeButtonInteraction(context.getTrader(), trade, mouseX - this.getX(), mouseY - this.getY(), button);
		
	}
	
	private static void DrawTooltip(PoseStack pose, int mouseX, int mouseY, List<Component> tooltips)
	{
		if(tooltips == null || tooltips.size() == 0)
			return;
		
		Minecraft mc = Minecraft.getInstance();
		if(mc.screen != null)
			mc.screen.renderComponentTooltip(pose, tooltips, mouseX, mouseY);
		
	}
	
	public boolean isMouseOverAlert(int mouseX, int mouseY, TradeRenderManager<?> tr, TradeContext context)
	{
		ScreenPosition position = tr.alertPosition(context);
		int left = this.getX() + position.x;
		int top = this.getY() + position.y;
		return mouseX >= left && mouseX < left + ARROW_WIDTH && mouseY >= top && mouseY < top + ARROW_HEIGHT;
	}
	
	public static List<Pair<DisplayEntry,DisplayData>> getInputDisplayData(TradeRenderManager<?> tr, TradeContext context)
	{
		List<Pair<DisplayEntry,DisplayData>> results = new ArrayList<>();
		List<DisplayEntry> entries = tr.getInputDisplays(context);
		List<DisplayData> display = tr.inputDisplayArea(context).divide(entries.size());
		for(int i = 0; i < entries.size() && i < display.size(); ++i)
			results.add(Pair.of(entries.get(i), display.get(i)));
		return results;
	}
	
	public static List<Pair<DisplayEntry,DisplayData>> getOutputDisplayData(TradeRenderManager<?> tr, TradeContext context)
	{
		List<Pair<DisplayEntry,DisplayData>> results = new ArrayList<>();
		List<DisplayEntry> entries = tr.getOutputDisplays(context);
		List<DisplayData> display = tr.outputDisplayArea(context).divide(entries.size());
		for(int i = 0; i < entries.size() && i < display.size(); ++i)
			results.add(Pair.of(entries.get(i), display.get(i)));
		return results;
	}
	
	@Override
	protected boolean isValidClickButton(int button) {
		if(this.getContext().isStorageMode || this.displayOnly)
			return false;
		return super.isValidClickButton(button);
	}

	public record DisplayData(int xOffset, int yOffset, int width, int height) {

		/**
		 * Divides the display area horizontally into the given number of pieces.
		 * Will always return a list of the length count
		 */
		public List<DisplayData> divide(int count) {
			if (count <= 1)
				return Lists.newArrayList(this);
			int partialWidth = this.width / count;
			int x = this.xOffset;
			List<DisplayData> result = new ArrayList<>();
			for (int i = 0; i < count; ++i) {
				result.add(new DisplayData(x, this.yOffset, partialWidth, this.height));
				x += partialWidth;
			}
			return result;
		}

	}
	
	public static abstract class DisplayEntry
	{
		
		private final List<Component> tooltip;
		
		@Deprecated
		protected DisplayEntry() { this.tooltip = null; }
		
		protected DisplayEntry (List<Component> tooltip) { this.tooltip = tooltip; }
		
		protected final Font getFont() { 
			Minecraft m = Minecraft.getInstance();
			return m.font;
		}
		
		protected List<Component> getTooltip() {
			if(this.tooltip == null)
				return new ArrayList<>();
			return this.tooltip;
		}
		
		public abstract void render(GuiComponent gui, PoseStack pose, int x, int y, DisplayData area);
		
		public abstract boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY);
		
		public static DisplayEntry of(ItemStack item, int count) { return new ItemEntry(item, count, null); }
		public static DisplayEntry of(ItemStack item, int count, List<Component> tooltip) { return new ItemEntry(item, count, tooltip); }
		public static DisplayEntry of(Pair<ResourceLocation,ResourceLocation> background) { return new EmptySlotEntry(background, null); }
		public static DisplayEntry of(Pair<ResourceLocation,ResourceLocation> background, List<Component> tooltip) { return new EmptySlotEntry(background, tooltip); }
		
		public static DisplayEntry of(Component text, TextFormatting format) { return new TextEntry(text, format, null); }
		public static DisplayEntry of(Component text, TextFormatting format, List<Component> tooltip) { return new TextEntry(text, format, tooltip); }
		
		public static DisplayEntry of(CoinValue price) { return new PriceEntry(price, null, false); }
		public static DisplayEntry of(CoinValue price, List<Component> additionalTooltips) { return new PriceEntry(price, additionalTooltips, false); }
		public static DisplayEntry of(CoinValue price, List<Component> additionalTooltips, boolean tooltipOverride) { return new PriceEntry(price, additionalTooltips, tooltipOverride); }
		
		private static class ItemEntry extends DisplayEntry
		{
			private final ItemStack item;

			private ItemEntry(ItemStack item, int count, List<Component> tooltip) { super(tooltip); this.item = item.copy(); this.item.setCount(count);  }
			
			private int getTopLeft(int xOrY, int availableWidthOrHeight) { return xOrY + (availableWidthOrHeight / 2) - 8; }
			
			@Override
			public void render(GuiComponent gui, PoseStack pose, int x, int y, DisplayData area) {
				if(this.item.isEmpty())
					return;
				Font font = this.getFont();
				//Center the x & y positions
				int left = getTopLeft(x + area.xOffset, area.width);
				int top = getTopLeft(y + area.yOffset, area.height);
				ItemRenderUtil.drawItemStack(gui, font, this.item, left, top);
			}

			@Override
			public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
				int left = getTopLeft(x + area.xOffset, area.width);
				int top = getTopLeft(y + area.yOffset, area.height);
				return mouseX >= left && mouseX < left + 16 && mouseY >= top && mouseY < top + 16;
			}
		}
		
		private static class EmptySlotEntry extends DisplayEntry
		{
			private final Pair<ResourceLocation,ResourceLocation> background;
			
			private EmptySlotEntry(Pair<ResourceLocation,ResourceLocation> background, List<Component> tooltip) { super(tooltip); this.background = background; }
			
			private int getTopLeft(int xOrY, int availableWidthOrHeight) { return xOrY + (availableWidthOrHeight / 2) - 8; }
			
			@Override
			public void render(GuiComponent gui, PoseStack pose, int x, int y, DisplayData area) {
				int left = getTopLeft(x + area.xOffset, area.width);
				int top = getTopLeft(y + area.yOffset, area.height);
				ItemRenderUtil.drawSlotBackground(pose, left, top, this.background);
			}
			
			@Override
			public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
				int left = getTopLeft(x + area.xOffset, area.width);
				int top = getTopLeft(y + area.yOffset, area.height);
				return mouseX >= left && mouseX < left + 16 && mouseY >= top && mouseY < top + 16;
			}
			
		}
		
		private static class TextEntry extends DisplayEntry
		{
			
			
			private final Component text;
			private final TextFormatting format;
			
			private TextEntry(Component text, TextFormatting format, List<Component> tooltip) { super(tooltip); this.text = text; this.format = format; }

			protected int getTextLeft(int x, int availableWidth) { 
				if(this.format.centering().isCenter())
					return x + (availableWidth / 2) - (this.getTextWidth() / 2);
				if(this.format.centering().isRight())
					return x + availableWidth - this.getTextWidth();
				return x;
			}
			
			protected int getTextTop(int y, int availableHeight) {
				if(this.format.centering().isMiddle())
					return y + (availableHeight / 2) - (this.getFont().lineHeight / 2);
				if(this.format.centering().isBottom())
					return y + availableHeight - this.getFont().lineHeight;
				return y;
			}
			
			protected int getTextWidth() { return this.getFont().width(this.text); }
			
			@Override
			public void render(GuiComponent gui, PoseStack pose, int x, int y, DisplayData area) {
				if(this.text.getString().isBlank())
					return;
				Font font = this.getFont();
				//Define the x position
				int left = this.getTextLeft(x + area.xOffset, area.width);
				//Define the y position
				int top = this.getTextTop(y + area.yOffset, area.height);
				//Draw the text
				font.drawShadow(pose, this.text, left, top, this.format.color());
			}

			@Override
			public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
				int left = this.getTextLeft(x + area.xOffset, area.width);
				int top = this.getTextTop(y + area.yOffset, area.height);
				return mouseX >= left && mouseX < left + this.getTextWidth() && mouseY >= top && mouseY < top + this.getFont().lineHeight;
			}
			
		}
		
		private static class PriceEntry extends DisplayEntry {
			private final CoinValue price;
			
			public PriceEntry(CoinValue price, List<Component> additionalTooltips, boolean tooltipOverride) {
				super(getTooltip(price, additionalTooltips, tooltipOverride));
				this.price = price;
			}
			
			private int getTopLeft(int xOrY, int availableWidthOrHeight) { return xOrY + (availableWidthOrHeight / 2) - 8; }
			
			private static List<Component> getTooltip(CoinValue price, List<Component> additionalTooltips, boolean tooltipOverride) {
				List<Component> tooltips = new ArrayList<>();
				if(tooltipOverride && additionalTooltips != null)
					return additionalTooltips;
				if(!price.isFree() && price.isValid())
					tooltips.add(Component.literal(price.getString()));
				if(additionalTooltips != null)
					tooltips.addAll(additionalTooltips);
				return tooltips;
			}
			
			@Override
			public void render(GuiComponent gui, PoseStack pose, int x, int y, DisplayData area) {
				if(this.price.isFree())
				{
					Font font = this.getFont();
					int left = x + area.xOffset + (area.width / 2) - (font.width(this.price.getString()) / 2);
					int top = y + area.yOffset + (area.height / 2) - (font.lineHeight / 2);
					font.draw(pose, Component.literal(this.price.getString()), left, top, 0xFFFFFF);
				}
				else
				{
					List<CoinValuePair> entries = this.price.getEntries();
					if(entries.size() * 16 <= area.width)
					{
						List<DisplayData> entryPositions = area.divide(entries.size());
						for(int i = 0; i < entryPositions.size() && i < entries.size(); ++i)
						{
							DisplayData pos = entryPositions.get(i);
							int left = this.getTopLeft(x + pos.xOffset, pos.width);
							int top = this.getTopLeft(y + pos.yOffset, pos.height);
							ItemStack stack = new ItemStack(entries.get(i).coin);
							stack.setCount(entries.get(i).amount);
							ItemRenderUtil.drawItemStack(gui, this.getFont(), stack, left, top);
						}
					}
					else if(entries.size() > 0)
					{
						int spacing = (area.width - 16) / entries.size();
						int top = this.getTopLeft(y + area.yOffset, area.height);
						int left = x + area.xOffset + area.width - 16;
						//Draw cheapest to most expensive
						for(int i = entries.size() - 1; i >= 0; --i)
						{
							ItemStack stack = new ItemStack(entries.get(i).coin);
							stack.setCount(entries.get(i).amount);
							ItemRenderUtil.drawItemStack(gui, this.getFont(), stack, left, top);
							left -= spacing;
						}
					}
				}
				
			}

			@Override
			public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
				int left = x + area.xOffset;
				int top = y + area.yOffset;
				return mouseX >= left && mouseX < left + area.width && mouseY >= top && mouseY < top + area.height;
			}
			
		}
		
	}
	
	
	
}
