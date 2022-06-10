package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.ITradeData;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.ITraderSource;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TradeButtonArea extends AbstractWidget implements IScrollable{
	
	public static final Function<ITradeData,Boolean> FILTER_VALID = trade -> trade.isValid();
	public static final Function<ITradeData,Boolean> FILTER_ANY = trade -> true;
	
	private final Supplier<? extends ITraderSource> traderSource;
	private final Function<ITrader, TradeContext> getContext;
	
	private BiFunction<ITrader,ITradeData,Boolean> isSelected = (trader,trade) -> false;
	public void setSelectionDefinition(@Nonnull BiFunction<ITrader,ITradeData,Boolean> isSelected) { this.isSelected = isSelected; }
	
	private InteractionConsumer interactionConsumer = null;
	public void setInteractionConsumer(InteractionConsumer consumer) { this.interactionConsumer = consumer; }
	
	private final List<TradeButton> allButtons = new ArrayList<>();
	
	private final Font font;
	
	private final Consumer<AbstractWidget> addWidget;
	private final Consumer<TradeButton> removeButton;
	private final BiConsumer<ITrader,ITradeData> onPress;
	private final Function<ITradeData,Boolean> tradeFilter;
	
	
	private final int columns;
	private int scroll = 0;
	private int lastFittableLines = 0;
	
	ScrollBarWidget scrollBar;
	public ScrollBarWidget getScrollBar() { return this.scrollBar; }
	
	private int scrollBarXOffset = 0;
	
	public int getAvailableWidth() { return this.scrollBar.visible() && this.scrollBarXOffset < 0 ? this.width + this.scrollBarXOffset : this.width; }
	
	public TradeButtonArea(Supplier<? extends ITraderSource> traderSource, Function<ITrader, TradeContext> getContext, int x, int y, int width, int height, int columns, Consumer<AbstractWidget> addWidget, Consumer<TradeButton> removeButton, BiConsumer<ITrader,ITradeData> onPress, Function<ITradeData,Boolean> tradeFilter)
	{
		super(x, y, width, height, Component.empty());
		this.columns = columns;
		this.traderSource = traderSource;
		this.getContext = getContext;
		this.addWidget = addWidget;
		this.removeButton = removeButton;
		this.onPress = onPress;
		this.tradeFilter = tradeFilter;
		
		Minecraft mc = Minecraft.getInstance();
		this.font = mc.font;
		
	}
	
	public void init() { this.init(-9, 0, this.height - 5); }
	
	public void init(int scrollBarXOffset, int scrollBarYOffset, int scrollBarHeight) {
		this.scrollBarXOffset = scrollBarXOffset;
		this.scrollBar = new ScrollBarWidget(this.x + this.width + scrollBarXOffset, this.y + scrollBarYOffset, scrollBarHeight, this);
		this.addWidget.accept(scrollBar);
		this.resetButtons();
		this.tick();
	}
	
	public ITrader getTrader(int traderIndex) {
		ITraderSource source = this.traderSource.get();
		if(source == null)
			return null;
		List<ITrader> traders = source.getTraders();
		if(traderIndex < 0 || traderIndex >= traders.size())
			return null;
		return traders.get(traderIndex);
	}
	
	public Pair<ITrader,ITradeData> getTradeAndTrader(int displayIndex) { return getTradeAndTrader(this.scroll, displayIndex); }
	
	public Pair<ITrader,ITradeData> getTradeAndTrader(int assumedScroll, int displayIndex) {
		int ignoreCount = assumedScroll * this.columns;
		ITraderSource source = this.traderSource.get();
		if(source == null)
			return Pair.of(null, null);
		List<ITrader> traders = source.getTraders();
		for(int t = 0; t < traders.size(); ++t)
		{
			ITrader trader = traders.get(t);
			List<? extends ITradeData> trades = trader.getTradeInfo();
			for(int i = 0; i < trades.size(); ++i)
			{
				ITradeData trade = trades.get(i);
				if(trade != null && this.tradeFilter.apply(trade))
				{
					if(ignoreCount > 0)
						ignoreCount--;
					else if(displayIndex <= 0)
						return Pair.of(trader, trade);
					else
						displayIndex--;
				}
			}
		}
		return Pair.of(null, null);
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		if(this.validTrades() <= 0)
		{
			int textWidth = this.font.width(Component.translatable("gui.lightmanscurrency.notrades"));
			this.font.draw(pose, Component.translatable("gui.lightmanscurrency.notrades"), this.x + (this.width / 2) - (textWidth / 2), this.y + (this.height / 2) - (this.font.lineHeight / 2), 0x404040);
		}
	}
	
	//Confirms each trades validity
	public void tick() {
		this.validateScroll();
		if(this.lastFittableLines < this.fittableLines())
		{
			//If we need to add more lines, recreate the buttons
			this.resetButtons();
		}
		else
			this.repositionButtons();
	}
	
	private void validateScroll() {
		if(this.canScrollDown())
			return;
		int oldScroll = this.scroll;
		this.scroll = MathUtil.clamp(this.scroll, 0, this.getMaxScroll());
		if(this.scroll != oldScroll)
			this.repositionButtons();
	}
	
	private void resetButtons() {
		
		this.allButtons.forEach(button -> this.removeButton.accept(button));
		this.allButtons.clear();
		
		int displayIndex = 0;
		this.lastFittableLines = this.fittableLines();
		for(int line = 0; line < this.lastFittableLines; ++line)
		{
			for(int c = 0; c < this.columns; ++c)
			{
				final int di = displayIndex;
				//Create the trade button
				TradeButton newButton = new TradeButton(() -> this.getContext.apply(this.getTradeAndTrader(di).getFirst()), () -> this.getTradeAndTrader(di).getSecond(), button -> this.OnTraderPress(di));
				this.addWidget.accept(newButton);
				this.allButtons.add(newButton);
				displayIndex++;
			}
		}
		
		this.repositionButtons();
		
	}
	
	private int validTrades() { 
		ITraderSource ts = this.traderSource.get();
		if(ts == null)
			return 0;
		int count = 0;
		List<ITrader> traders = ts.getTraders();
		for(ITrader trader : traders)
		{
			List<? extends ITradeData> trades = trader.getTradeInfo();
			for(ITradeData trade : trades)
			{
				if(trade != null && this.tradeFilter.apply(trade))
					count++;
			}
		}
		return count;
	}
	
	private int fittableLines()
	{
		return fittableLines(this.scroll);
	}
	
	private int fittableLines(int assumedScroll) {
		int lineCount = 0;
		int displayIndex = 0;
		int yOffset = 0;
		while(yOffset < this.height)
		{
			int lineHeight = 0;
			//Get relevant info for the buttons in this row
			for(int c = 0; c < this.columns; ++c)
			{
				Pair<ITrader,ITradeData> trade = this.getTradeAndTrader(assumedScroll, displayIndex);
				if(trade.getFirst() != null && trade.getSecond() != null)
				{
					TradeContext context = this.getContext.apply(trade.getFirst());
					lineHeight = Math.max(trade.getSecond().tradeButtonHeight(context), lineHeight);
				}
				displayIndex ++;
			}
			if(lineHeight <= 0) //Assume some height to a line even if the trade doesn't exist to prevent the calculation of "infinite" fittable lines.
				lineHeight = TradeButton.ASSUME_HEIGHT;
			if(yOffset + lineHeight > this.height)
				return lineCount;
			yOffset += lineHeight + 4;
			lineCount++;
		}
		return lineCount;
	}
	
	private void repositionButtons() {
		
		int displayIndex = 0;
		int queryIndex = 0;
		int yOffset = 0;
		int fittableLines = this.fittableLines();
		for(int line = 0; line < fittableLines; ++line)
		{
			int lineHeight = 0;
			int visibleButtons = 0;
			int totalWidth = 0;
			//Get relevant info for the buttons in this row
			for(int c = 0; c < this.columns; ++c)
			{
				Pair<ITrader,ITradeData> trade = this.getTradeAndTrader(queryIndex);
				if(trade.getFirst() != null && trade.getSecond() != null)
				{
					TradeContext context = this.getContext.apply(trade.getFirst());
					visibleButtons++;
					totalWidth += trade.getSecond().tradeButtonWidth(context);
					lineHeight = Math.max(trade.getSecond().tradeButtonHeight(context), lineHeight);
				}
				queryIndex++;
			}
			//Position the buttons in this row
			int spacing = (this.getAvailableWidth() - totalWidth)/(visibleButtons + 1);
			int xOffset = spacing;
			for(int c = 0; c < this.columns; ++c)
			{
				Pair<ITrader,ITradeData> trade = this.getTradeAndTrader(displayIndex);
				TradeButton button = this.allButtons.get(displayIndex);
				if(trade.getFirst() != null && trade.getSecond() != null)
				{
					TradeContext context = this.getContext.apply(trade.getFirst());
					button.move(this.x + xOffset, this.y + yOffset);
					button.visible = true;
					button.active = !this.isSelected.apply(trade.getFirst(), trade.getSecond());
					xOffset += trade.getSecond().tradeButtonWidth(context) + spacing;
				}
				else
					button.visible = false;
				displayIndex ++;
			}
			yOffset += lineHeight + 4;
		}
		//Hide spare/extra buttons
		for(int i = fittableLines * this.columns; i < this.allButtons.size(); ++i)
		{
			this.allButtons.get(i).visible = false;
		}
		
	}
	
	private void OnTraderPress(int displayIndex)
	{
		if(this.onPress != null)
		{
			Pair<ITrader,ITradeData> data = this.getTradeAndTrader(displayIndex);
			this.onPress.accept(data.getFirst(), data.getSecond());
		}
	}
	
	public void renderTraderName(PoseStack pose, int x, int y, int maxWidth, boolean renderTitle)
	{
		ITraderSource ts = this.traderSource.get();
		if(ts == null)
			return;
		
		String text = "";
		for(ITrader trader : ts.getTraders())
		{
			if(text.isEmpty())
				text = renderTitle ? trader.getTitle().getString() : trader.getName().getString();
			else
				text += Component.translatable("gui.lightmanscurrency.trading.listseperator").getString() + (renderTitle ? trader.getTitle().getString() : trader.getName().getString());
		}
		
		this.font.draw(pose, TextRenderUtil.fitString(text, maxWidth), x, y, 0x404040);
		
	}
	
	public void renderTooltips(Screen screen, PoseStack pose, int nameX, int nameY, int nameWidth, int mouseX, int mouseY)
	{
		for(TradeButton button : this.allButtons)
		{
			button.renderTooltips(pose, mouseX, mouseY);
		}
		this.renderTraderNameTooltip(screen, pose, nameX, nameY, nameWidth, mouseX, mouseY);
	}
	
	public void renderTraderNameTooltip(Screen screen, PoseStack pose, int x, int y, int maxWidth, int mouseX, int mouseY)
	{
		if(mouseX >= x && mouseX < x + maxWidth && mouseY >= y && mouseY < y + this.font.lineHeight)
		{
			List<Component> tooltips = new ArrayList<>();
			ITraderSource ts = this.traderSource.get();
			if(ts == null)
				return;
			for(ITrader trader : ts.getTraders())
				tooltips.add(trader.getTitle());
			
			if(tooltips.size() <= 0)
				return;
			
			screen.renderComponentTooltip(pose, tooltips, mouseX, mouseY);
		}
	}
	
	private boolean canScrollDown() { return this.canScrollDown(this.scroll); }
	
	private boolean canScrollDown(int assumedScroll) {
		return this.validTrades() - (assumedScroll * this.columns) > this.fittableLines(assumedScroll) * this.columns;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if(delta < 0)
		{			
			if(this.canScrollDown())
			{
				this.scroll++;
				this.resetButtons();
			}
			else
				return false;
		}
		else if(delta > 0)
		{
			if(this.scroll > 0)
			{
				scroll--;
				this.resetButtons();
			}	
			else
				return false;
		}
		return true;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for(int i = 0; i < this.allButtons.size(); ++i)
		{
			TradeButton b = this.allButtons.get(i);
			if(b.isMouseOver(mouseX, mouseY))
			{
				Pair<ITrader,ITradeData> traderPair = this.getTradeAndTrader(i);
				TradeContext context = this.getContext.apply(traderPair.getFirst());
				if(context.isStorageMode)
				{
					if(this.interactionConsumer != null)
					{
						b.onInteractionClick((int)mouseX, (int)mouseY, button, this.interactionConsumer);
						return true;
					}
				}
				else
					return b.mouseClicked(mouseX, mouseY, button);
			}
		}
		return false;
	}

	@Override
	public void updateNarration(NarrationElementOutput narrator) { }
	
	@Override
	public NarrationPriority narrationPriority() { return NarrationPriority.NONE; }
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) { return true; }
	
	public interface InteractionConsumer {
		public void onTradeButtonInputInteraction(ITrader trader, ITradeData trade, int index, int mouseButton);
		public void onTradeButtonOutputInteraction(ITrader trader, ITradeData trade, int index, int mouseButton);
		public void onTradeButtonInteraction(ITrader trader, ITradeData trade, int localMouseX, int localMouseY, int mouseButton);
	}

	@Override
	public int currentScroll() { return this.scroll; }

	@Override
	public void setScroll(int newScroll) {
		if(newScroll == this.scroll)
			return;
		this.scroll = MathUtil.clamp(newScroll, 0, this.getMaxScroll());
		this.resetButtons();
	}

	@Override
	public int getMaxScroll() {
		for(int s = 0; true; s++)
		{
			if(!this.canScrollDown(s))
				return s;
		}
	}
	
}
