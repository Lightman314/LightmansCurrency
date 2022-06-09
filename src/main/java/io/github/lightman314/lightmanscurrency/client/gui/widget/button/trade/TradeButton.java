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
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil.TextFormatting;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu.IClientMessage;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.CoinValue.CoinValuePair;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class TradeButton extends Button{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/trade.png");
	public static final OnPress NULL_PRESS = button -> {};
	
	public static  final int ARROW_WIDTH = 22;
	public static  final int ARROW_HEIGHT = 18;
	
	public static  final int TEMPLATE_WIDTH = 212;
	public static  final int TEMPLATE_HEIGHT = 100;
	
	public static final int ASSUME_HEIGHT = 18;
	
	private final Supplier<ITradeData> tradeSource;
	public ITradeData getTrade() { return this.tradeSource.get(); }
	private Supplier<TradeContext> contextSource;
	public TradeContext getContext() { return this.contextSource.get(); }
	public boolean displayOnly = false;
	
	public TradeButton(Supplier<TradeContext> contextSource, Supplier<ITradeData> tradeSource, OnPress onPress) {
		super(0, 0, 0, 0, new TextComponent(""), onPress);
		this.tradeSource = tradeSource;
		this.contextSource = contextSource;
		this.recalculateSize();
	}
	
	private void recalculateSize()
	{
		ITradeData trade = this.getTrade();
		if(trade != null)
		{
			TradeContext context = this.getContext();
			this.width = trade.tradeButtonWidth(context);
			this.height = trade.tradeButtonHeight(context);
		}
	}
	
	public void move(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		ITradeData trade = this.getTrade();
		if(trade == null)
			return;

		TradeContext context = this.getContext();
		
		this.recalculateSize();
		
		this.renderBackground(pose, context.isStorageMode || this.displayOnly ? false : this.isHovered);
		
		if(trade.hasArrow(context))
			this.renderArrow(pose, trade.arrowPosition(context), context.isStorageMode || this.displayOnly ? false : this.isHovered);
		
		try {
			trade.renderAdditional(this, pose, mouseX, mouseY, context);
		} catch(Exception e) { LightmansCurrency.LogError("Error on additional Trade Button rendering.", e); }
		
		if(trade.hasAlert(context))
			this.renderAlert(pose, trade.alertPosition(context));
		
		this.renderDisplays(pose, trade, context);
		
	}
	
	private void renderBackground(PoseStack pose, boolean isHovered)
	{
		if(this.width < 8)
		{
			LightmansCurrency.LogError("Cannot render a trade button that is less than 8 pixels wide!");
			return;
		}
		if(this.height < 8)
		{
			LightmansCurrency.LogError("Cannot render a trade button that is less than 8 pixels tall!");
			return;
		}
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		if(this.active)
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		else
			RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1f);
		
		int vOffset = isHovered ? TEMPLATE_HEIGHT : 0;
		
		//Render the top-left
		this.blit(pose, this.x, this.y, 0, vOffset, 4, 4);
		//Render the top-middle
		int xOff = 4;
		while(xOff < this.width - 4)
		{
			int xRend = Math.min(this.width - 4 - xOff, TEMPLATE_WIDTH - 8);
			this.blit(pose, this.x + xOff, this.y, 4, vOffset, xRend, 4);
			xOff += xRend;
		}
		//Render the top-right
		this.blit(pose, this.x + this.width - 4, this.y, TEMPLATE_WIDTH - 4, vOffset, 4, 4);
		//Render the middle
		int yOff = 4;
		while(yOff < this.height - 4)
		{
			int yRend = Math.min(this.height - 4 - yOff, TEMPLATE_HEIGHT - 8);
			//Middle-left
			this.blit(pose, this.x, this.y + yOff, 0, vOffset + 4, 4, yRend);
			xOff = 4;
			while(xOff < this.width - 4)
			{
				int xRend = Math.min(this.width - 4 - xOff, TEMPLATE_WIDTH - 8);
				//Render the middle
				this.blit(pose, this.x + xOff, this.y + yOff, 4, vOffset + 4, xRend, yRend);
				xOff += xRend;
			}
			//Middle-right
			this.blit(pose, this.x + this.width - 4, this.y + yOff, TEMPLATE_WIDTH - 4, vOffset + 4, 4, yRend);
			yOff += yRend;
		}
		//Render the bottom - left
		this.blit(pose, this.x, this.y + this.height - 4, 0, vOffset + TEMPLATE_HEIGHT - 4, 4, 4);
		//Render the bottom-middle
		xOff = 4;
		while(xOff < this.width - 4)
		{
			int xRend = Math.min(this.width - 4 - xOff, TEMPLATE_WIDTH - 8);
			this.blit(pose, this.x + xOff, this.y + this.height - 4, 4, vOffset + TEMPLATE_HEIGHT - 4, xRend, 4);
			xOff += xRend;
		}
		//Render the bottom-right
		this.blit(pose, this.x + this.width - 4, this.y + this.height - 4, TEMPLATE_WIDTH - 4, vOffset + TEMPLATE_HEIGHT - 4, 4, 4);
		
	}
	
	private void renderArrow(PoseStack pose, Pair<Integer,Integer> position, boolean isHovered)
	{
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		if(this.active)
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		else
			RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1f);
		
		int vOffset = isHovered ? ARROW_HEIGHT : 0;
		
		this.blit(pose, this.x + position.getFirst(), this.y + position.getSecond(), TEMPLATE_WIDTH, vOffset, ARROW_WIDTH, ARROW_HEIGHT);
		
	}
	
	private void renderAlert(PoseStack pose, Pair<Integer,Integer> position)
	{
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		if(this.active)
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		else
			RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1f);
		
		this.blit(pose, this.x + position.getFirst(), this.y + position.getSecond(), TEMPLATE_WIDTH + ARROW_WIDTH, 0, ARROW_WIDTH, ARROW_HEIGHT);
		
	}
	
	public void renderDisplays(PoseStack pose, ITradeData trade, TradeContext context)
	{
		for(Pair<DisplayEntry,DisplayData> display : getInputDisplayData(trade, context))
			display.getFirst().render(this, pose, this.x, this.y, display.getSecond());
		for(Pair<DisplayEntry,DisplayData> display : getOutputDisplayData(trade, context))
			display.getFirst().render(this, pose, this.x, this.y, display.getSecond());
	}
	
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY)
	{
		if(!this.visible || !this.isMouseOver(mouseX, mouseY))
			return;
		
		ITradeData trade = tradeSource.get();
		if(trade == null)
			return;
		
		TradeContext context = this.getContext();
		
		for(Pair<DisplayEntry,DisplayData> display : getInputDisplayData(trade, context))
		{
			if(display.getFirst().isMouseOver(this.x, this.y, display.getSecond(), mouseX, mouseY))
			{
				DrawTooltip(pose, mouseX, mouseY, display.getFirst().tooltip);
			}
		}
		
		for(Pair<DisplayEntry,DisplayData> display : getOutputDisplayData(trade, context))
		{
			if(display.getFirst().isMouseOver(this.x, this.y, display.getSecond(), mouseX, mouseY))
			{
				DrawTooltip(pose, mouseX, mouseY, display.getFirst().tooltip);
			}
		}
		
		if(trade.hasAlert(context) && this.isMouseOverAlert(mouseX, mouseY, trade, context))
		{
			DrawTooltip(pose, mouseX, mouseY, trade.getAlerts(context));
		}
		
		DrawTooltip(pose, mouseX, mouseY, trade.getAdditionalTooltips(context, mouseX - this.x, mouseY - this.y));
		
	}
	
	public void onInteractionClick(int mouseX, int mouseY, int button, InteractionConsumer consumer)
	{
		if(!this.visible || !this.isMouseOver(mouseX, mouseY))
			return;
		
		ITradeData trade = tradeSource.get();
		if(trade == null)
			return;
		
		TradeContext context = this.getContext();
		
		List<Pair<DisplayEntry,DisplayData>> inputDisplays = getInputDisplayData(trade, context);
		for(int i = 0; i < inputDisplays.size(); ++i)
		{
			Pair<DisplayEntry,DisplayData> display = inputDisplays.get(i);
			if(display.getFirst().isMouseOver(this.x, this.y, display.getSecond(), mouseX, mouseY))
			{
				consumer.onTradeButtonInputInteraction(context.getTrader(), trade, i, button);
				return;
			}
		}
		
		List<Pair<DisplayEntry,DisplayData>> outputDisplays = getOutputDisplayData(trade, context);
		for(int i = 0; i < outputDisplays.size(); ++i)
		{
			Pair<DisplayEntry,DisplayData> display = outputDisplays.get(i);
			if(display.getFirst().isMouseOver(this.x, this.y, display.getSecond(), mouseX, mouseY))
			{
				consumer.onTradeButtonOutputInteraction(context.getTrader(), trade, i, button);
				return;
			}
		}
		
		//Only run the default interaction code if you didn't hit an input or output display
		consumer.onTradeButtonInteraction(context.getTrader(), trade, mouseX - this.x, mouseY - this.y, button);
		
	}
	
	private static void DrawTooltip(PoseStack pose, int mouseX, int mouseY, List<Component> tooltips)
	{
		if(tooltips == null || tooltips.size() <= 0)
			return;
		
		Minecraft mc = Minecraft.getInstance();
		mc.screen.renderComponentTooltip(pose, tooltips, mouseX, mouseY);
		
	}
	
	public int isMouseOverInput(int mouseX, int mouseY)
	{
		ITradeData trade = this.getTrade();
		if(trade == null)
			return -1;
		List<Pair<DisplayEntry,DisplayData>> inputDisplays = getInputDisplayData(trade, this.getContext());
		for(int i = 0; i < inputDisplays.size(); ++i)
		{
			if(inputDisplays.get(i).getFirst().isMouseOver(this.x, this.y, inputDisplays.get(i).getSecond(), mouseX, mouseY))
				return i;
		}
		return -1;
	}
	
	public int isMouseOverOutput(int mouseX, int mouseY)
	{
		ITradeData trade = this.getTrade();
		if(trade == null)
			return -1;
		List<Pair<DisplayEntry,DisplayData>> inputDisplays = getInputDisplayData(trade, this.getContext());
		for(int i = 0; i < inputDisplays.size(); ++i)
		{
			if(inputDisplays.get(i).getFirst().isMouseOver(this.x, this.y, inputDisplays.get(i).getSecond(), mouseX, mouseY))
				return i;
		}
		return -1;
	}
	
	public boolean isMouseOverAlert(int mouseX, int mouseY, ITradeData trade, TradeContext context)
	{
		Pair<Integer,Integer> position = trade.alertPosition(context);
		int left = this.x + position.getFirst();
		int top = this.y + position.getSecond();
		return mouseX >= left && mouseX < left + ARROW_WIDTH && mouseY >= top && mouseY < top + ARROW_HEIGHT;
	}
	
	public static List<Pair<DisplayEntry,DisplayData>> getInputDisplayData(ITradeData trade, TradeContext context)
	{
		List<Pair<DisplayEntry,DisplayData>> results = new ArrayList<>();
		List<DisplayEntry> entries = trade.getInputDisplays(context);
		List<DisplayData> display = trade.inputDisplayArea(context).divide(entries.size());
		for(int i = 0; i < entries.size() && i < display.size(); ++i)
			results.add(Pair.of(entries.get(i), display.get(i)));
		return results;
	}
	
	public static List<Pair<DisplayEntry,DisplayData>> getOutputDisplayData(ITradeData trade, TradeContext context)
	{
		List<Pair<DisplayEntry,DisplayData>> results = new ArrayList<>();
		List<DisplayEntry> entries = trade.getOutputDisplays(context);
		List<DisplayData> display = trade.outputDisplayArea(context).divide(entries.size());
		for(int i = 0; i < entries.size() && i < display.size(); ++i)
			results.add(Pair.of(entries.get(i), display.get(i)));
		return results;
	}
	
	public DisplayEntry getInputDisplay(int index) {
		ITradeData trade = tradeSource.get();
		if(trade == null)
			return DisplayEntry.EMPTY;
		List<DisplayEntry> inputDisplays = trade.getInputDisplays(this.getContext());
		if(index < 0 || index >= inputDisplays.size())
			return DisplayEntry.EMPTY;
		return inputDisplays.get(index);
	}
	
	private int inputDisplayCount() {
		ITradeData trade = tradeSource.get();
		if(trade == null)
			return 0;
		return trade.getInputDisplays(this.getContext()).size();
	}
	
	public Pair<Integer,Integer> getInputDisplayPosition(int index) {
		ITradeData trade = tradeSource.get();
		if(trade == null)
			return Pair.of(0, 0);
		
		int xPos = 0;
		int yPos = 0;
		int count = this.inputDisplayCount();
		if(index < 0 || index >= count)
			return Pair.of(0, 0);
		
		return Pair.of(this.x + xPos, this.y + yPos);
		
	}
	
	public boolean isMouseOverInputDisplay(int mouseX, int mouseY, int index) {
		if(!this.visible)
			return false;
		ITradeData trade = tradeSource.get();
		if(trade == null)
			return false;
		List<DisplayEntry> inputDisplays = trade.getInputDisplays(this.getContext());
		for(int i = 0; i < inputDisplays.size(); ++i)
		{
			
		}
		return false;
	}
	
	@Override
	protected boolean isValidClickButton(int button) {
		if(this.getContext().isStorageMode || this.displayOnly)
			return false;
		return super.isValidClickButton(button);
	}
	
	public static interface ITradeData
	{
		
		/**
		 * Whether the trade is fully defined and capable of being interacted with.
		 */
		public boolean isValid();
		/**
		 * The width of the trade button.
		 */
		public int tradeButtonWidth(TradeContext context);
		/**
		 * The height of the trade button.
		 */
		public int tradeButtonHeight(TradeContext context);
		/**
		 * Whether the trade should render an arrow pointing from the inputs to the outputs.
		 */
		default boolean hasArrow(TradeContext context) { return true; }
		
		/**
		 * Where on the button the arrow should be drawn.
		 */
		public Pair<Integer,Integer> arrowPosition(TradeContext context);
		
		public Pair<Integer,Integer> alertPosition(TradeContext context);
		
		/**
		 * The position and size of the input displays
		 */
		public DisplayData inputDisplayArea(TradeContext context);
		/**
		 * The position and size of the output displays
		 */
		public DisplayData outputDisplayArea(TradeContext context);
		/**
		 * The input display entries. For a sale this would be the trades price.
		 */
		public List<DisplayEntry> getInputDisplays(TradeContext context);
		/**
		 * The output display entries. For a sale this would be the product being sold.
		 */
		public List<DisplayEntry> getOutputDisplays(TradeContext context);
		
		/**
		 * Whether the trade has any alerts
		 */
		public default boolean hasAlert(TradeContext context) { List<Component> alerts = this.getAlerts(context); return alerts != null && alerts.size() > 0; }
		
		/**
		 * List of alert text. Used for Out of Stock & misc Trade Rule messages.
		 * Return null to display no alert.
		 */
		public List<Component> getAlerts(TradeContext context);
		/**
		 * Render trade-specific icons for the trade, such as the fluid traders drainable/fillable icons.
		 * @param button The button that is rendering the trade
		 * @param pose The pose stack
		 * @param mouseX The x position of the mouse.
		 * @param mouseY The y position of the mouse.
		 * @param context The context of the trade.
		 */
		default void renderAdditional(AbstractWidget button, PoseStack pose, int mouseX, int mouseY, TradeContext context) { }
		/**
		 * Render trade-specific tooltips for the trade, such as the fluid traders drainable/fillable icons.
		 * @param context The context of the trade.
		 * @param mouseX The mouses X position relative to the left edge of the button.
		 * @param mouseY The mouses Y position relative to the top edge of the button.
		 * @return The list of tooltip text. Return null to display no tooltip.
		 */
		default List<Component> getAdditionalTooltips(TradeContext context, int mouseX, int mouseY) { return null; }
		
		/**
		 * 
		 * Called when an input display is clicked on in display mode.
		 * Runs on the client, but can be called on the server by running tab.sendInputInteractionMessage for consistent execution
		 * 
		 * @param tab The Trade Edit tab that is being used to display this tab.
		 * @param clientHandler The client handler that can be used to send custom client messages to the currently opened tab. Will be null on the server.
		 * @param index The index of the input display that was clicked.
		 * @param button The mouse button that was clicked.
		 * @param heldItem The item being held by the player.
		 */
		public void onInputDisplayInteraction(BasicTradeEditTab tab, @Nullable IClientMessage clientHandler, int index, int button, ItemStack heldItem);
		
		/**
		 * 
		 * Called when an output display is clicked on in display mode.
		 * Runs on the client, but can be called on the server by running tab.sendOutputInteractionMessage for consistent execution
		 * 
		 * @param tab The Trade Edit tab that is being used to display this tab.
		 * @param clientHandler The client handler that can be used to send custom client messages to the currently opened tab. Will be null on the server.
		 * @param index The index of the input display that was clicked.
		 * @param button The mouse button that was clicked.
		 * @param heldItem The item being held by the player.
		 */
		public void onOutputDisplayInteraction(BasicTradeEditTab tab, @Nullable IClientMessage clientHandler, int index, int button, ItemStack heldItem);
		
		/**
		 * Called when the trade is clicked on in display mode, but the mouse wasn't over any of the input or output slots.
		 * Runs on the client, but can be called on the server by running tab.sendOtherInteractionMessage for consistent code execution.
		 * 
		 * @param tab The Trade Edit tab that is being used to display this tab.
		 * @param clientHandler The client handler that can be used to send custom client messages to the currently opened tab. Will be null on the server.
		 * @param mouseX The local X position of the mouse button when it was clicked. [0,tradeButtonWidth)
		 * @param mouseY The local Y position of the mouse button when it was clicked. [0,tradeButtonHeight)
		 * @param button The mouse button that was clicked.
		 * @param heldItem The item currently being held by the player.
		 */
		public void onInteraction(BasicTradeEditTab tab, @Nullable IClientMessage clientHandler, int mouseX, int mouseY, int button, ItemStack heldItem);
		
	}
	
	public static class DisplayData
	{
		public final int xOffset;
		public final int yOffset;
		public final int width;
		public final int height;
		public DisplayData(int xOffset, int yOffset, int width, int height) { this.xOffset = xOffset; this.yOffset = yOffset; this.width = width; this.height = height; }
		
		/**
		 * Divides the display area horizontally into the given number of pieces.
		 * Will always return a list of the length count
		 */
		public List<DisplayData> divide(int count)
		{
			if(count <= 1)
				return Lists.newArrayList(this);
			int partialWidth = this.width / count;
			int x = this.xOffset;
			List<DisplayData> result = new ArrayList<>();
			for(int i = 0; i < count; ++i)
			{
				result.add(new DisplayData(x, this.yOffset, partialWidth, this.height));
				x += partialWidth;
			}
			return result;
		}
		
	}
	
	public static abstract class DisplayEntry
	{
		
		private static final DisplayEntry EMPTY = of(new TextComponent(""), TextFormatting.create());
		
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
					tooltips.add(new TextComponent(price.getString()));
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
					font.draw(pose, new TextComponent(this.price.getString()), left, top, 0xFFFFFF);
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
