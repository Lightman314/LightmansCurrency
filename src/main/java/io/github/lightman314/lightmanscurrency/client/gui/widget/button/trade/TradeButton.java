package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.CoinValue.CoinValuePair;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.NonNullSupplier;

public class TradeButton extends Button{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/trade.png");
	
	public final int ARROW_WIDTH = 22;
	public final int ARROW_HEIGHT = 18;
	
	public final int TEMPLATE_WIDTH = 212;
	public final int TEMPLATE_HEIGHT = 100;
	
	private final Supplier<ITradeData> tradeSource;
	private NonNullSupplier<TradeContext> contextSource;
	public TradeContext getContext() { return this.contextSource.get(); }
	public boolean horizontalFlip = false;
	
	public TradeButton(int x, int y, int width, int height, NonNullSupplier<TradeContext> contextSource, Supplier<ITradeData> tradeSource, OnPress onPress) {
		super(x, y, width, height, new TextComponent(""), onPress);
		this.tradeSource = tradeSource;
		this.contextSource = contextSource;
	}
	
	public void move(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks)
	{
		if(!this.visible)
			return;
		
		ITradeData trade = tradeSource.get();
		if(trade == null)
			return;
		
		this.renderBackground(pose);
		
		
		
		
		
	}
	
	private void renderBackground(PoseStack pose)
	{
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		if(this.active)
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		else
			RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1f);
		
		//int vOffset = this.isHovered ? TEMPLATE_HEIGHT : 0;
		
		//int centerX = this.x + (this.width / 2);
		//int centerY = this.y + (this.height / 2);
		
		
		
		
	}
	
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY)
	{
		if(!this.visible)
			return;
		
		ITradeData trade = tradeSource.get();
		if(trade == null)
			return;
		
		//Draw the background
		
		
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
	
	public static interface ITradeData
	{
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
		default boolean hasArrow() { return true; }
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
		 * List of alert text. Used for Out of Stock & misc Trade Rule messages.
		 * Return null to display no alert.
		 */
		public List<Component> getAlerts(TradeContext context);
		/**
		 * Render trade-specific icons for the trade, such as the fluid traders drainable/fillable icons.
		 * @param x The x position of the button.
		 * @param y The y position of the button.
		 * @param displayMode Whether the button is in display mode (storage screen).
		 */
		default void renderAdditional(GuiComponent gui, PoseStack pose, int x, int y, boolean displayMode) { }
		/**
		 * Render trade-specific tooltips for the trade, such as the fluid traders drainable/fillable icons.
		 * @param mouseX The mouses X position relative to the left edge of the button.
		 * @param mouseY The mouses Y position relative to the top edge of the button.
		 * @return The list of tooltip text. Return null to display no tooltip.
		 */
		default List<Component> getAdditionalTooltips(int mouseX, int mouseY) { return null; }
	}
	
	public static class DisplayData
	{
		public final int xOffset;
		public final int yOffset;
		public final int width;
		public final int height;
		public DisplayData(int xOffset, int yOffset, int width, int height) { this.xOffset = xOffset; this.yOffset = yOffset; this.width = width; this.height = height; }
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
		
		protected abstract void render(GuiComponent gui, PoseStack pose, int x, int y, int availableWidth, int availableHeight);
		
		protected abstract boolean isMouseOver(int x, int y, int availableWidth, int availableHeight, int mouseX, int mouseY);
		
		public static DisplayEntry of(ItemStack item, int count) { return new ItemEntry(item, count, null); }
		public static DisplayEntry of(ItemStack item, int count, List<Component> tooltip) { return new ItemEntry(item, count, tooltip); }
		
		public static DisplayEntry of(Component text, TextFormatting format) { return new TextEntry(text, format, null); }
		public static DisplayEntry of(Component text, TextFormatting format, List<Component> tooltip) { return new TextEntry(text, format, tooltip); }
		
		public static List<DisplayEntry> of(CoinValue value) { 
			List<DisplayEntry> entries = new ArrayList<>();
			if(value.isFree())
			{
				entries.add(DisplayEntry.of(new TranslatableComponent("gui.coinvalue.free"), TextFormatting.create(), new ArrayList<>()));
				return entries;
			}
			for(CoinValuePair coinPair : value.getEntries())
			{
				entries.add(DisplayEntry.of(new ItemStack(coinPair.coin), coinPair.amount, Lists.newArrayList(new TextComponent(value.getString()))));
			}
			return entries;
		}
		
		private static class ItemEntry extends DisplayEntry
		{
			private final ItemStack item;
			private final int count;

			private ItemEntry(ItemStack item, int count, List<Component> tooltip) { super(tooltip); this.item = item.copy(); this.item.setCount(1); this.count = count; }
			
			private int getTopLeft(int xOrY, int availableWidthOrHeight) { return xOrY + (availableWidthOrHeight / 2) - 8; }
			
			@Override
			protected void render(GuiComponent gui, PoseStack pose, int x, int y, int availableWidth, int availableHeight) {
				if(this.item.isEmpty())
					return;
				Font font = this.getFont();
				//Center the x & y positions
				int left = getTopLeft(x, availableWidth);
				int top = getTopLeft(y, availableHeight);
				ItemRenderUtil.drawItemStack(gui, font, this.item, left, top);
				if(this.count > 1)
				{
					String text = String.valueOf(this.count);
					int width = font.width(text);
					font.draw(pose, text, left + 16 - width, top + 16 - font.lineHeight, 0xFFFFFF);
				}
			}

			@Override
			protected boolean isMouseOver(int x, int y, int availableWidth, int availableHeight, int mouseX, int mouseY) {
				int left = getTopLeft(x, availableWidth);
				int top = getTopLeft(y, availableHeight);
				return mouseX >= left && mouseX < left + 16 && mouseY >= top && mouseY < top + 16;
			}
		}
		
		public static class TextFormatting
		{
			
			public enum Centering {
				TOP_LEFT(-1,1), TOP_CENTER(0,1), TOP_RIGHT(1,1),
				MIDDLE_LEFT(-1,0), MIDDLE_CENTER(0,0), MIDDLE_RIGHT(1,0),
				BOTTOM_LEFT(-1,-1), BOTTOM_CENTER(0,-1), BOTTOM_RIGHT(1,-1);
				
				private final int horiz;
				private final int vert;
				
				private Centering(int horiz, int vert) { this.horiz = horiz; this.vert = vert; }
				public boolean isTop() { return vert > 0; }
				public boolean isMiddle() { return vert == 0; }
				public boolean isBottom() { return vert < 0; }
				public boolean isLeft() { return horiz < 0; }
				public boolean isCenter() { return horiz == 0; }
				public boolean isRight() { return horiz > 1; }
				
				public Centering makeTop() { return this.of(this.horiz, 1); }
				public Centering makeMiddle() { return this.of(this.horiz, 0); }
				public Centering makeBottom() { return this.of(this.horiz, -1); }
				
				public Centering makeLeft() { return this.of(-1, this.vert); }
				public Centering makeCenter() { return this.of(0, this.vert); }
				public Centering makeRight() { return this.of(1, this.vert); }
				
				private Centering of(int horiz, int vert) {
					for(Centering c : Centering.values())
					{
						if(c.horiz == horiz && c.vert == vert)
							return c;
					}
					return this;
				}
				
			}
			
			private Centering centering = Centering.MIDDLE_CENTER;
			private int color = 0xFFFFFF;
			
			private TextFormatting() {}
			
			public static TextFormatting create() { return new TextFormatting(); }
			
			public TextFormatting topEdge() { this.centering = this.centering.makeTop(); return this; }
			public TextFormatting middle() { this.centering = this.centering.makeMiddle(); return this; }
			public TextFormatting bottomEdge() { this.centering = this.centering.makeBottom(); return this; }
			
			public TextFormatting leftEdge() { this.centering = this.centering.makeLeft(); return this; }
			public TextFormatting centered() { this.centering = this.centering.makeCenter(); return this; }
			public TextFormatting rightEdge() { this.centering = this.centering.makeRight(); return this; }
			
			public TextFormatting color(int color) { this.color = color; return this; }
			
		}
		
		private static class TextEntry extends DisplayEntry
		{
			
			
			private final Component text;
			private final TextFormatting format;
			
			private TextEntry(Component text, TextFormatting format, List<Component> tooltip) { super(tooltip); this.text = text; this.format = format; }

			protected int getTextLeft(int x, int availableWidth) { 
				if(this.format.centering.isCenter())
					return x + (availableWidth / 2) - (this.getTextWidth() / 2);
				if(this.format.centering.isRight())
					return x + availableWidth - this.getTextWidth();
				return x;
			}
			
			protected int getTextTop(int y, int availableHeight) {
				if(this.format.centering.isMiddle())
					return y + (availableHeight / 2) - (this.getFont().lineHeight / 2);
				if(this.format.centering.isBottom())
					return y + availableHeight - this.getFont().lineHeight;
				return y;
			}
			
			protected int getTextWidth() { return this.getFont().width(this.text); }
			
			@Override
			protected void render(GuiComponent gui, PoseStack pose, int x, int y, int availableWidth, int availableHeight) {
				if(this.text.getString().isBlank())
					return;
				Font font = this.getFont();
				//Define the x position
				int left = this.getTextLeft(x, availableWidth);
				//Define the y position
				int top = this.getTextTop(y, availableHeight);
				//Draw the text
				font.draw(pose, this.text, left, top, this.format.color);
			}

			@Override
			protected boolean isMouseOver(int x, int y, int availableWidth, int availableHeight, int mouseX, int mouseY) {
				int left = this.getTextLeft(mouseX, availableWidth);
				int top = this.getTextTop(y, availableHeight);
				return x >= left && x < left + this.getTextWidth() && y >= top && y < top + this.getFont().lineHeight;
			}
			
		}
		
	}
	
}
