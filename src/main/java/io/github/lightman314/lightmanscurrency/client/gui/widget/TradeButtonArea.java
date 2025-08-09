package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import io.github.lightman314.lightmanscurrency.api.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TradeButtonArea extends EasyWidgetWithChildren implements IScrollable, ITooltipSource, IEasyTickable {

	@Deprecated
	public static final Function<TradeData,Boolean> FILTER_VALID = TradeData::isValid;
	@Deprecated
	public static final Function<TradeData,Boolean> FILTER_ANY = trade -> true;

	private final Supplier<? extends ITraderSource> traderSource;
	private final Function<TraderData, TradeContext> getContext;

	private final BiFunction<TraderData,TradeData,Boolean> isSelected;

	private final TradeInteractionHandler interactionHandler;

	private final List<TradeButton> allButtons = new ArrayList<>();

	private final Font font;

	private final BiConsumer<TraderData,TradeData> onPress;
	private final Predicate<TradeData> tradeFilter;

	private int scroll = 0;


	ScrollBarWidget scrollBar;
	private final boolean allowSearching;
	EditBox searchBox;
	private String lastSearch = "";

	private boolean hasTitlePosition = false;
	private ScreenPosition titlePosition = ScreenPosition.ZERO;
	private int titleWidth = 0;
	private int actualTitleWidth() { return this.isSearchBoxRelevant() ? this.titleWidth - 90 : this.titleWidth; }
	private boolean renderNameOnly = false;
	private ScreenArea searchBoxArea = ScreenArea.of(ScreenPosition.ZERO,90,12);

	private final BiFunction<TraderData,TradeData,List<Component>> extraTooltips;

	/**
	 * @deprecated Use {@link Builder#title(ScreenPosition, int, boolean)} instead
	 */
	@Deprecated
	public TradeButtonArea withTitle(ScreenPosition titlePosition, int titleWidth, boolean renderNameOnly) { this.hasTitlePosition = true; this.titlePosition = titlePosition; this.titleWidth = titleWidth; this.renderNameOnly = renderNameOnly; return this; }

	public boolean isSearchBoxRelevant() { return this.searchBox != null && this.searchBox.isVisible(); }

	private final ScreenPosition scrollBarOffset;

	private int scrollBarHeight;

	private String searchStartText = "";

	/**
	 * @deprecated Use {@link Builder#scrollBarHeight(int)} instead
	 */
	@Deprecated
	public TradeButtonArea withScrollBarHeight(int height) { this.scrollBarHeight = height; return this; }

	//Variant of getAvailableWidth that assumes we'll have the smallest amount of available space.
	//Assumption is made so that we don't get into an infinite loop calculating whether we can scroll -> how many rows -> whether we can scroll...
	public int getMinAvailableWidth() { return this.scrollBarOffset.x < 0 ? this.width + this.scrollBarOffset.x : this.width; }
	public int getAvailableWidth() { return this.scrollBar.visible() ? (this.scrollBarOffset.x < 0 ? this.width + this.scrollBarOffset.x : this.width) : this.width; }

	private TradeButtonArea(@Nonnull Builder builder)
	{
		super(builder);
		//Trades & Traders
		this.traderSource = builder.traderSource;
		this.getContext = builder.context;
		this.onPress = builder.pressAction;
		this.tradeFilter = builder.tradeFilter;
		this.extraTooltips = builder.extraTooltips;

		//Font Collection
		Minecraft mc = Minecraft.getInstance();
		this.font = mc.font;

		//Title
		if(builder.titlePosition != null)
		{
			this.hasTitlePosition = true;
			this.titlePosition = builder.titlePosition;
			this.titleWidth = builder.titleWidth;
			this.renderNameOnly = builder.titleNameOnly;
		}

		//Search Box
		this.allowSearching = builder.allowSearching;

		//Scroll Bar
		this.scrollBarOffset = builder.scrollBarOffset;
		if(builder.scrollBarHeight > 0)
			this.scrollBarHeight = builder.scrollBarHeight;
		else
			this.scrollBarHeight = this.height - 5;

		//Selection State
		this.isSelected = builder.selectionTrigger;

		//Interaction Handler
		this.interactionHandler = builder.interactionHandler;

		//Copy inputs from old instance
		if(builder.old != null)
		{
			//Copy scroll value
			this.scroll = builder.old.scroll;
			if(builder.old.searchBox != null)
				this.searchStartText = builder.old.searchBox.getValue();
		}

	}

	@Override
	public void addChildren(@Nonnull ScreenArea area) {
		this.addChild(ScrollListener.builder()
				.area(area)
				.listener(this)
				.build());
		this.scrollBar = this.addChild(ScrollBarWidget.builder()
				.position(area.pos.offset(area.width + this.scrollBarOffset.x,this.scrollBarOffset.y))
				.height(this.scrollBarHeight)
				.scrollable(this)
				.build());
		if(this.hasTitlePosition && this.allowSearching)
		{
			//Make search box take 1/3 of the width
			this.searchBoxArea = ScreenArea.of(this.titlePosition.x + this.titleWidth - 90, this.titlePosition.y - 2, 90, 12);
			this.searchBox = this.addChild(new EditBox(this.font, this.searchBoxArea.pos.x + 2, this.searchBoxArea.pos.y + 2, this.searchBoxArea.width - 10, 10, LCText.GUI_TRADER_SEARCH_TRADES.get()));
			this.searchBox.setBordered(false);
			this.searchBox.setValue(this.searchStartText);
			this.searchBox.setResponder(s -> this.lastSearch = s);
			this.tickSearchBox();
		}
		this.resetButtons();
	}

	@Nullable
	public TradeButton getHoveredButton(ScreenPosition mousePos) {
		for(TradeButton button : this.allButtons)
		{
			if(button.isMouseOver(mousePos.x, mousePos.y))
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

	public List<List<Pair<TraderData,TradeData>>> getTradesInRows(boolean search) {
		List<List<Pair<TraderData,TradeData>>> result = new ArrayList<>();
		ITraderSource source = this.traderSource.get();
		if(source == null)
			return new ArrayList<>();
		List<TraderData> traders = source.getTraders();

		int currentRowWidth = 0;
		List<Pair<TraderData,TradeData>> currentRow = new ArrayList<>();

		for (TraderData trader : traders) {
			TradeContext context = this.getContext.apply(trader);
			List<? extends TradeData> trades = trader.getTradeData();
			for (TradeData trade : trades) {
				if (this.tradeFilter.test(trade) && this.tradeMatchesSearch(source, trade,search)) {
					TradeRenderManager<?> trm = trade.getButtonRenderer();
					int tradeWidth = trm.tradeButtonWidth(context);
					if (currentRowWidth + tradeWidth > this.getMinAvailableWidth() && !currentRow.isEmpty()) {
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

	@Nonnull
	private String searchText() { return this.lastSearch; }

	private boolean tradeMatchesSearch(@Nonnull ITraderSource source, @Nonnull TradeData trade, boolean search)
	{
		if(!search || !this.allowSearching)
			return true;
		if(this.isSearchBoxRelevant() && !this.searchText().isBlank())
			return TraderAPI.API.FilterTrade(trade,this.searchText().toLowerCase());
		return true;
	}

	public Pair<TraderData,TradeData> getTradeAndTrader(int displayIndex) { return getTradeAndTrader(this.scroll, displayIndex); }

	public Pair<TraderData,TradeData> getTradeAndTrader(int assumedScroll, int displayIndex) {
		ITraderSource source = this.traderSource.get();
		if(source == null)
			return Pair.of(null, null);
		List<List<Pair<TraderData,TradeData>>> rows = this.getTradesInRows(true);
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
	public void renderWidget(@Nonnull EasyGuiGraphics gui) {
		if(!this.hasValidTrade())
		{
			TextRenderUtil.drawCenteredText(gui, LCText.GUI_TRADER_NO_TRADES.get(), this.width / 2, (this.height / 2) - (gui.font.lineHeight / 2), 0x404040);
		}
		//Render title
		if(this.hasTitlePosition)
		{
			ITraderSource ts = this.traderSource.get();
			if(ts == null)
				return;

			Component title = ts.getCustomTitle();
			if(title == null)
			{
				MutableComponent text = EasyText.empty();
				for(TraderData trader : ts.getTraders())
				{
					if(text.getString().isEmpty())
						text.append(this.renderNameOnly ? trader.getName() : trader.getTitle());
					else
						text.append(LCText.GUI_SEPERATOR.get()).append(this.renderNameOnly ? trader.getName() : trader.getTitle());
				}
				title = text;
			}

			gui.pushOffsetZero();
			gui.drawString(TextRenderUtil.fitString(title, this.actualTitleWidth()), this.titlePosition, 0x404040);


			if(this.isSearchBoxRelevant())
			{
				//Render Search Box Background
				gui.blit(ItemEditWidget.GUI_TEXTURE, this.searchBoxArea, 18, 0);
			}
			gui.popOffset();
		}
	}

	//Confirms each trades validity
	@Override
	public void renderTick() { this.validateScroll(); }

	@Override
	public void tick()
	{
		if(this.allButtons.size() < this.requiredButtons())
		{
			//If we need to add more lines, recreate the buttons
			this.resetButtons();
		}
		else
			this.repositionButtons();
		this.tickSearchBox();
	}

	private void tickSearchBox()
	{
		if(this.searchBox != null)
		{
			ITraderSource source = this.traderSource.get();
			this.searchBox.setVisible(source != null && source.showSearchBox());
		}
	}

	private void resetButtons() {

		this.allButtons.forEach(this::removeChild);
		this.allButtons.clear();

		int requiredButtons = this.requiredButtons();
		for(int i = 0; i < requiredButtons; i++)
		{
			final int di = i;
			//Create the trade button
			TradeButton newButton = this.addChild(TradeButton.builder()
					.pressAction(() -> this.OnTraderPress(di))
					.context(() -> this.getContext.apply(this.getTradeAndTrader(di).getFirst()))
					.trade(() -> this.getTradeAndTrader(di).getSecond())
					.selectedState(this.isSelected)
					.extraTooltips(this.extraTooltips)
					.build());
			this.allButtons.add(newButton);
		}

		this.repositionButtons();

	}

	private boolean hasValidTrade() {
		ITraderSource ts = this.traderSource.get();
		if(ts == null)
			return false;
		int count = 0;
		List<TraderData> traders = ts.getTraders();
		for(TraderData trader : traders)
		{
			List<? extends TradeData> trades = trader.getTradeData();
			for(TradeData trade : trades)
			{
				if(trade != null && this.tradeFilter.test(trade) && this.tradeMatchesSearch(ts,trade,true))
					return true;
			}
		}
		return false;
	}

	private int requiredButtons() {
		List<List<Pair<TraderData,TradeData>>> rows = this.getTradesInRows(false);
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
		List<List<Pair<TraderData,TradeData>>> rows = this.getTradesInRows(true);
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
				if(displayIndex >= this.allButtons.size())
					break;
				TradeButton button = this.allButtons.get(displayIndex);
				if (trade.getFirst() != null && trade.getSecond() != null) {
					TradeContext context = this.getContext.apply(trade.getFirst());
					button.setPosition(this.getPosition().offset(xOffset, yOffset));
					button.visible = true;
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

	@Override
	public List<Component> getTooltipText(int mouseX, int mouseY) {
		//Don't need to renderBG button tooltips as that's handled by the button itself now
		if(this.hasTitlePosition)
		{
			if(this.titlePosition.isMouseInArea(mouseX, mouseY, this.actualTitleWidth(), this.font.lineHeight))
			{
				List<Component> tooltips = new ArrayList<>();
				ITraderSource ts = this.traderSource.get();
				if(ts == null)
					return null;
				for(TraderData trader : ts.getTraders())
					tooltips.add(trader.getTitle());
				return tooltips;
			}
		}
		return null;
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
					if(this.interactionHandler != null)
					{
						b.HandleInteractionClick((int)mouseX,(int)mouseY, button, this.interactionHandler);
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
	public boolean isMouseOver(double mouseX, double mouseY) { return true; }

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
	public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.fittableLines(), this.getTradesInRows(true).size()); }

	@Nonnull
	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	public static class Builder extends EasySizableBuilder<Builder>
	{

		private Builder() {}
		@Override
		protected Builder getSelf() { return this; }

		private Supplier<ITraderSource> traderSource = () -> null;
		private Function<TraderData,TradeContext> context = t -> null;
		private BiConsumer<TraderData,TradeData> pressAction = (t,d) -> {};
		private Predicate<TradeData> tradeFilter = t -> true;
		@Nullable
		private ScreenPosition titlePosition = null;
		int titleWidth = 0;
		boolean titleNameOnly = false;
		boolean allowSearching = true;
		ScreenPosition scrollBarOffset = ScreenPosition.of(-9,0);
		int scrollBarHeight = 0;
		BiFunction<TraderData,TradeData,Boolean> selectionTrigger = (t,d) -> false;
		@Nullable
		TradeInteractionHandler interactionHandler = null;
		BiFunction<TraderData,TradeData,List<Component>> extraTooltips = (a,b) -> null;
		@Nullable
		TradeButtonArea old = null;

		public Builder old(@Nullable TradeButtonArea old) { this.old = old; return this; }
		public Builder traderSource(Supplier<ITraderSource> source) { this.traderSource = source; return this; }
		public Builder context(Supplier<TradeContext> contextSource) { return this.context(t -> contextSource.get()); }
		public Builder context(Function<TraderData,TradeContext> contextBuilder) { this.context = contextBuilder; return this; }
		public Builder pressAction(BiConsumer<TraderData,TradeData> pressAction) { this.pressAction = pressAction; return this; }
		public Builder tradeFilter(Predicate<TradeData> tradeFilter) { this.tradeFilter = tradeFilter; return this; }
		public Builder tradeFilter(@Nullable TraderData trader, ITraderStorageMenu menu) {
			if(trader != null)
				return this.tradeFilter(trader.getStorageDisplayFilter(menu));
			return this;
		}
		public Builder title(ScreenPosition titlePosition, int titleWidth, boolean renderNameOnly) { this.titlePosition = titlePosition; this.titleWidth = titleWidth; this.titleNameOnly = renderNameOnly; return this; }
		public Builder blockSearchBox() { this.allowSearching = false; return this; }
		public Builder scrollBarOffset(ScreenPosition offset) { this.scrollBarOffset = offset; return this; }
		public Builder scrollBarHeight(int height) { this.scrollBarHeight = height; return this; }
		public Builder selectedState(BiFunction<TraderData,TradeData,Boolean> selectedState) { this.selectionTrigger = selectedState; return this; }
		public Builder interactionHandler(TradeInteractionHandler handler) { this.interactionHandler = handler; return this; }
		public Builder extraTooltips(Component tooltip) { this.extraTooltips = (a, b) -> ImmutableList.of(tooltip); return this;}
		public Builder extraTooltips(TextEntry tooltip) { this.extraTooltips = (a, b) -> tooltip.getAsList(); return this;}
		public Builder extraTooltips(List<Component> tooltip) { this.extraTooltips = (a, b) -> tooltip; return this;}
		public Builder extraTooltips(Supplier<List<Component>> tooltip) { this.extraTooltips = (a, b) -> tooltip.get(); return this; }
		public Builder extraTooltips(BiFunction<TraderData,TradeData,List<Component>> tooltip) { this.extraTooltips = tooltip; return this; }

		public TradeButtonArea build() { return new TradeButtonArea(this); }

	}

}