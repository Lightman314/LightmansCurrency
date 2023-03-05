package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class TradeButtonArea extends AbstractWidget implements IScrollable{
	
	public static final Function<TradeData,Boolean> FILTER_VALID = TradeData::isValid;
	public static final Function<TradeData,Boolean> FILTER_ANY = trade -> true;
	
	private final Supplier<? extends ITraderSource> traderSource;
	private final Function<TraderData, TradeContext> getContext;
	
	private BiFunction<TraderData,TradeData,Boolean> isSelected = (trader,trade) -> false;
	public void setSelectionDefinition(@Nonnull BiFunction<TraderData,TradeData,Boolean> isSelected) { this.isSelected = isSelected; }
	
	private InteractionConsumer interactionConsumer = null;
	public void setInteractionConsumer(InteractionConsumer consumer) { this.interactionConsumer = consumer; }
	
	private final List<TradeButton> allButtons = new ArrayList<>();
	
	private final Font font;
	
	private final Consumer<AbstractWidget> addWidget;
	private final Consumer<TradeButton> removeButton;
	private final BiConsumer<TraderData,TradeData> onPress;
	private final Function<TradeData,Boolean> tradeFilter;
	
	private int scroll = 0;
	
	ScrollBarWidget scrollBar;
	public ScrollBarWidget getScrollBar() { return this.scrollBar; }
	
	private int scrollBarXOffset = 0;
	
	//Variant of getAvailableWidth that assumes we'll have the smallest amount of available space.
	//Assumption is made so that we don't get into an infinite loop calculating the whether we can scroll -> how many rows -> whether we can scroll...
	public int getMinAvailableWidth() { return this.scrollBarXOffset < 0 ? this.width + this.scrollBarXOffset : this.width; }
	public int getAvailableWidth() { return this.scrollBar.visible() ? (this.scrollBarXOffset < 0 ? this.width + this.scrollBarXOffset : this.width) : this.width; }
	
	public TradeButtonArea(Supplier<? extends ITraderSource> traderSource, Function<TraderData, TradeContext> getContext, int x, int y, int width, int height, Consumer<AbstractWidget> addWidget, Consumer<TradeButton> removeButton, BiConsumer<TraderData,TradeData> onPress, Function<TradeData,Boolean> tradeFilter)
	{
		super(x, y, width, height, Component.empty());
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
		this.addWidget.accept(this.scrollBar);
		this.resetButtons();
	}

	@Nullable
	public TradeButton getHoveredButton(int mouseX, int mouseY) {
		for(TradeButton button : this.allButtons)
		{
			if(button.isMouseOver(mouseX, mouseY))
				return button;
		}
		return null;
	}
	
	public TraderData getTrader(int traderIndex) {
		ITraderSource source = this.traderSource.get();
		if(source == null)
			return null;
		List<TraderData> traders = source.getTraders();
		if(traderIndex < 0 || traderIndex >= traders.size())
			return null;
		return traders.get(traderIndex);
	}
	
	public List<List<Pair<TraderData,TradeData>>> getTradesInRows() {
		List<List<Pair<TraderData,TradeData>>> result = new ArrayList<>();
		List<TraderData> traders = this.traderSource.get().getTraders();
		
		int currentRowWidth = 0;
		List<Pair<TraderData,TradeData>> currentRow = new ArrayList<>();

		for (TraderData trader : traders) {
			TradeContext context = this.getContext.apply(trader);
			List<? extends TradeData> trades = trader.getTradeData();
			for (TradeData trade : trades) {
				if (this.tradeFilter.apply(trade)) {
					TradeRenderManager<?> trm = trade.getButtonRenderer();
					int tradeWidth = trm.tradeButtonWidth(context);
					if (currentRowWidth + tradeWidth > this.getMinAvailableWidth() && currentRow.size() > 0) {
						//Start new row
						result.add(currentRow);
						currentRow = new ArrayList<>();
						currentRowWidth = 0;
					}
					//Add button to row
					currentRow.add(Pair.of(trader, trade));
					currentRowWidth += tradeWidth;
				}
			}
		}
		result.add(currentRow);
		return result;
	}
	
	public Pair<TraderData,TradeData> getTradeAndTrader(int displayIndex) { return getTradeAndTrader(this.scroll, displayIndex); }
	
	public Pair<TraderData,TradeData> getTradeAndTrader(int assumedScroll, int displayIndex) {
		ITraderSource source = this.traderSource.get();
		if(source == null)
			return Pair.of(null, null);
		List<List<Pair<TraderData,TradeData>>> rows = this.getTradesInRows();
		for(int r = assumedScroll; r < rows.size(); ++r)
		{
			List<Pair<TraderData,TradeData>> row = rows.get(r);
			for (Pair<TraderData, TradeData> traderDataTradeDataPair : row) {
				if (displayIndex <= 0)
					return traderDataTradeDataPair;
				else
					displayIndex--;
			}
		}
		return Pair.of(null, null);
	}
	
	@Override
	public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		if(this.validTrades() <= 0)
		{
			int textWidth = this.font.width(Component.translatable("gui.lightmanscurrency.notrades"));
			this.font.draw(pose, Component.translatable("gui.lightmanscurrency.notrades"), this.x + (this.width / 2) - (textWidth / 2), this.x + (this.height / 2) - (this.font.lineHeight / 2), 0x404040);
		}
	}
	
	//Confirms each trades validity
	public void tick() {
		this.validateScroll();
		if(this.allButtons.size() < this.requiredButtons())
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
		
		this.allButtons.forEach(this.removeButton);
		this.allButtons.clear();
		
		int requiredButtons = this.requiredButtons();
		for(int i = 0; i < requiredButtons; i++)
		{
			final int di = i;
			//Create the trade button
			TradeButton newButton = new TradeButton(() -> this.getContext.apply(this.getTradeAndTrader(di).getFirst()), () -> this.getTradeAndTrader(di).getSecond(), button -> this.OnTraderPress(di));
			this.addWidget.accept(newButton);
			this.allButtons.add(newButton);
		}
		
		this.repositionButtons();
		
	}
	
	private int validTrades() {
		ITraderSource ts = this.traderSource.get();
		if(ts == null)
			return 0;
		int count = 0;
		List<TraderData> traders = ts.getTraders();
		for(TraderData trader : traders)
		{
			List<? extends TradeData> trades = trader.getTradeData();
			for(TradeData trade : trades)
			{
				if(trade != null && this.tradeFilter.apply(trade))
					count++;
			}
		}
		return count;
	}
	
	private int requiredButtons() {
		List<List<Pair<TraderData,TradeData>>> rows = this.getTradesInRows();
		int count = 0;
		int lines = this.fittableLines();
		for(int r = this.scroll; r < rows.size() && r < this.scroll + lines; ++r)
			count += rows.get(r).size();
		return count;
	}
	
	private int fittableLines() { return this.height / (TradeButton.BUTTON_HEIGHT + 4); }


	private void repositionButtons() {
		
		int displayIndex = 0;
		int yOffset = 0;
		int fittableLines = this.fittableLines();
		List<List<Pair<TraderData,TradeData>>> rows = this.getTradesInRows();
		for(int line = 0; line < fittableLines && line + this.scroll < rows.size(); ++line)
		{
			List<Pair<TraderData,TradeData>> row = rows.get(line + this.scroll);
			int visibleButtons = 0;
			int totalWidth = 0;
			//Get relevant info for the buttons in this row
			for (Pair<TraderData, TradeData> trade : row) {
				if (trade.getFirst() != null && trade.getSecond() != null) {
					TradeContext context = this.getContext.apply(trade.getFirst());
					visibleButtons++;
					totalWidth += trade.getSecond().getButtonRenderer().tradeButtonWidth(context);
				}
			}
			//Position the buttons in this row
			int spacing = (this.getAvailableWidth() - totalWidth)/(visibleButtons + 1);
			int xOffset = spacing;
			for (Pair<TraderData, TradeData> trade : row) {
				TradeButton button = this.allButtons.get(displayIndex);
				if (trade.getFirst() != null && trade.getSecond() != null) {
					TradeContext context = this.getContext.apply(trade.getFirst());
					button.move(this.x + xOffset, this.y + yOffset);
					button.visible = true;
					button.active = !this.isSelected.apply(trade.getFirst(), trade.getSecond());
					xOffset += trade.getSecond().getButtonRenderer().tradeButtonWidth(context) + spacing;

				} else
					button.visible = false;
				displayIndex++;

			}
			yOffset += TradeButton.BUTTON_HEIGHT + 4;
		}
		//Hide spare/extra buttons
		for(int i = displayIndex; i < this.allButtons.size(); ++i)
		{
			this.allButtons.get(i).visible = false;
		}
		
	}
	
	private void OnTraderPress(int displayIndex)
	{
		if(this.onPress != null)
		{
			Pair<TraderData,TradeData> data = this.getTradeAndTrader(displayIndex);
			this.onPress.accept(data.getFirst(), data.getSecond());
		}
	}
	
	public void renderTraderName(PoseStack pose, int x, int y, int maxWidth, boolean renderTitle)
	{
		ITraderSource ts = this.traderSource.get();
		if(ts == null)
			return;
		
		StringBuilder text = new StringBuilder();
		for(TraderData trader : ts.getTraders())
		{
			if(text.length() == 0)
				text = new StringBuilder(renderTitle ? trader.getTitle().getString() : trader.getName().getString());
			else
				text.append(Component.translatable("gui.lightmanscurrency.trading.listseperator").getString()).append(renderTitle ? trader.getTitle().getString() : trader.getName().getString());
		}
		
		this.font.draw(pose, TextRenderUtil.fitString(text.toString(), maxWidth), x, y, 0x404040);
		
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
			for(TraderData trader : ts.getTraders())
				tooltips.add(trader.getTitle());
			
			if(tooltips.size() == 0)
				return;
			
			screen.renderComponentTooltip(pose, tooltips, mouseX, mouseY);
		}
	}
	
	private boolean canScrollDown() { return this.canScrollDown(this.scroll); }
	
	private boolean canScrollDown(int assumedScroll) {
		return this.getTradesInRows().size() - assumedScroll > this.fittableLines();
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
				Pair<TraderData,TradeData> traderPair = this.getTradeAndTrader(i);
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
	public void updateNarration(@NotNull NarrationElementOutput narrator) { }

	@Override
	public @NotNull NarrationPriority narrationPriority() { return NarrationPriority.NONE; }
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) { return true; }
	
	public interface InteractionConsumer {
		void onTradeButtonInputInteraction(TraderData trader, TradeData trade, int index, int mouseButton);
		void onTradeButtonOutputInteraction(TraderData trader, TradeData trade, int index, int mouseButton);
		void onTradeButtonInteraction(TraderData trader, TradeData trade, int localMouseX, int localMouseY, int mouseButton);
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
