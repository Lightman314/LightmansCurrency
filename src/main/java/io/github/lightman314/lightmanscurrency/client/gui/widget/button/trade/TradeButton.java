package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;

public class TradeButton extends EasyButton implements ITooltipSource {

	public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/trade.png");

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
	private final BiFunction<TraderData,TradeData,Boolean> isSelected;
	private final BiFunction<TraderData,TradeData,List<Component>> extraTooltips;

	private final boolean displayOnly;

	private TradeButton(@Nonnull Builder builder)
	{
		super(builder);
		this.tradeSource = builder.trade;
		this.contextSource = builder.context;
		this.displayOnly = builder.displayOnly;
		this.isSelected = builder.isSelected;
		this.extraTooltips = builder.extraTooltips;
		this.recalculateSize();
	}

	private void recalculateSize()
	{
		TradeRenderManager<?> tr = this.getTradeRenderer();
		if(tr != null)
		{
			TradeContext context = this.getContext();
			this.setWidth(tr.tradeButtonWidth(context));
		}
	}

	/**
	 * @deprecated Use {@link #setPosition(int, int)}, {@link #setPosition(ScreenPosition)}, or set the position in {@link Builder}
	 */
	@Deprecated
	public void move(int x, int y) { this.setPosition(x, y); }

	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui) {

		TradeRenderManager<?> tr = this.getTradeRenderer();
		if(tr == null)
			return;

		TradeContext context = this.getContext();
		if(context == null)
			return;

		boolean selected = this.isSelected.apply(context.getTrader(),tr.trade);
		boolean hovered = !context.isStorageMode && !this.displayOnly && this.isHovered;

		this.recalculateSize();

		this.renderBackground(gui,hovered,selected);

		LazyOptional<ScreenPosition> arrowPosOptional = tr.arrowPosition(context);
		arrowPosOptional.ifPresent(arrowPos -> this.renderArrow(gui,arrowPos,hovered,selected));

		//Render custom display stuff in front of the arrow, not behind it.
		try { tr.renderAdditional(this, gui, context);
		} catch(Exception e) { LightmansCurrency.LogError("Error on additional Trade Button rendering.", e); }

		this.renderAlert(gui, tr.alertPosition(context), tr.getAlertData(context), hovered);

		this.renderDisplays(gui, tr, context);

		gui.resetColor();

	}

	private void renderBackground(@Nonnull EasyGuiGraphics gui, boolean isHovered, boolean selected)
	{
		if(this.width < 8)
		{
			LightmansCurrency.LogError("Cannot renderBG a trade button that is less than 8 pixels wide!");
			return;
		}
		if(this.active)
			gui.resetColor();
		else
			gui.setColor(0.5f,0.5f,0.5f);

		int vOffset = isHovered ? BUTTON_HEIGHT : 0;
		if(selected)
			vOffset += 2 * BUTTON_HEIGHT;

		//Render the left
		gui.blit(GUI_TEXTURE, 0, 0, 0, vOffset, 4, BUTTON_HEIGHT);
		//Render the middle
		int xOff = 4;
		while(xOff < this.width - 4)
		{
			int xRend = Math.min(this.width - 4 - xOff, TEMPLATE_WIDTH - 8);
			gui.blit(GUI_TEXTURE, xOff, 0, 4, vOffset, xRend, BUTTON_HEIGHT);
			xOff += xRend;
		}
		//Render the right
		gui.blit(GUI_TEXTURE, this.width - 4, 0, TEMPLATE_WIDTH - 4, vOffset, 4, BUTTON_HEIGHT);

	}

	private void renderArrow(@Nonnull EasyGuiGraphics gui, @Nonnull ScreenPosition position, boolean isHovered, boolean selected)
	{

		if(this.active)
			gui.resetColor();
		else
			gui.setColor(0.5f,0.5f,0.5f);

		int vOffset = isHovered || selected ? ARROW_HEIGHT : 0;

		gui.blit(GUI_TEXTURE, position, TEMPLATE_WIDTH, vOffset, ARROW_WIDTH, ARROW_HEIGHT);

	}

	private void renderAlert(@Nonnull EasyGuiGraphics gui, @Nonnull ScreenPosition position, @Nullable List<AlertData> alerts, boolean isHovered)
	{

		if(alerts == null || alerts.isEmpty())
			return;
		alerts.sort(AlertData::compare);

		alerts.get(0).setShaderColor(gui, this.active ? 1f : 0.5f, isHovered);
		gui.blit(GUI_TEXTURE, position, TEMPLATE_WIDTH + ARROW_WIDTH, 0, ARROW_WIDTH, ARROW_HEIGHT);

	}

	public void renderDisplays(EasyGuiGraphics gui, TradeRenderManager<?> tr, TradeContext context)
	{
		for(Pair<DisplayEntry,DisplayData> display : getInputDisplayData(tr, context))
			display.getFirst().render(gui, 0, 0, display.getSecond());
		for(Pair<DisplayEntry,DisplayData> display : getOutputDisplayData(tr, context))
			display.getFirst().render(gui, 0, 0, display.getSecond());
	}

	@Override
	public boolean renderTooltip(@Nonnull EasyGuiGraphics gui) {

		if(!this.isMouseOver(gui.mousePos))
			return false;

		TradeRenderManager<?> tr = this.getTradeRenderer();
		if(tr == null)
			return false;

		TradeContext context = this.getContext();
		if(context == null)
			return false;

		int mouseX = gui.mousePos.x;
		int mouseY = gui.mousePos.y;

		List<Component> tooltips = new ArrayList<>();

		List<Component> extra = this.extraTooltips.apply(context.getTrader(),tr.trade);
		if(extra != null && !extra.isEmpty())
			tooltips.addAll(extra);

		this.tryAddTooltip(tooltips, tr.getAdditionalTooltips(context, mouseX - this.getX(), mouseY - this.getY()));

		for(Pair<DisplayEntry,DisplayData> display : getInputDisplayData(tr, context))
		{
			if(display.getFirst().isMouseOver(this.getX(), this.getY(), display.getSecond(), mouseX, mouseY))
			{
				if(display.getFirst().trySelfRenderTooltip(gui))
					return true;
				this.tryAddTooltip(tooltips, display.getFirst().getTooltip());
			}
		}

		for(Pair<DisplayEntry,DisplayData> display : getOutputDisplayData(tr, context))
		{
			if(display.getFirst().isMouseOver(this.getX(), this.getY(), display.getSecond(), mouseX, mouseY))
			{
				if(display.getFirst().trySelfRenderTooltip(gui))
					return true;
				this.tryAddTooltip(tooltips, display.getFirst().getTooltip());
			}
		}


		if(this.isMouseOverAlert(mouseX, mouseY, tr, context))
		{
			List<AlertData> alerts = tr.getAlertData(context);
			if(alerts != null && !alerts.isEmpty())
				this.tryAddAlertTooltips(tooltips, alerts);
		}


		if(tooltips.isEmpty())
			return false;

		gui.renderComponentTooltip(tooltips);
        return true;
	}

	@Override
	public List<Component> getTooltipText(int mouseX, int mouseY) { return null; }

	private void tryAddTooltip(@Nonnull List<Component> tooltips, @Nullable List<Component> add)
	{
		if(add == null)
			return;
		tooltips.addAll(add);
	}

	private void tryAddAlertTooltips(@Nonnull List<Component> tooltips, @Nullable List<AlertData> alerts)
	{
		if(alerts == null)
			return;
		alerts.sort(AlertData::compare);
		for(AlertData alert : alerts)
			tooltips.add(alert.getFormattedMessage());
	}

	public void HandleInteractionClick(int mouseX, int mouseY, int button, @Nonnull TradeInteractionHandler handler)
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

		TradeInteractionData data = new TradeInteractionData(mouseX - this.getX(), mouseY - this.getY(), button, Screen.hasShiftDown(), Screen.hasControlDown(), Screen.hasAltDown());

		List<Pair<DisplayEntry,DisplayData>> inputDisplays = getInputDisplayData(tr, context);
		for(int i = 0; i < inputDisplays.size(); ++i)
		{
			Pair<DisplayEntry,DisplayData> display = inputDisplays.get(i);
			if(display.getFirst().isMouseOver(this.getX(), this.getY(), display.getSecond(), mouseX, mouseY))
			{
				handler.HandleTradeInputInteraction(context.getTrader(), trade, data, i);
				return;
			}
		}

		List<Pair<DisplayEntry,DisplayData>> outputDisplays = getOutputDisplayData(tr, context);
		for(int i = 0; i < outputDisplays.size(); ++i)
		{
			Pair<DisplayEntry,DisplayData> display = outputDisplays.get(i);
			if(display.getFirst().isMouseOver(this.getX(), this.getY(), display.getSecond(), mouseX, mouseY))
			{
				handler.HandleTradeOutputInteraction(context.getTrader(), trade, data, i);
				return;
			}
		}

		//Only run the default interaction code if you didn't hit an input or output display
		handler.HandleOtherTradeInteraction(context.getTrader(), trade, data);

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
		TradeContext context = this.getContext();
		if(context == null || context.isStorageMode || this.displayOnly)
			return false;
		return super.isValidClickButton(button);
	}

	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	public static class Builder extends EasyButtonBuilder<Builder>
	{
		private Builder() { super(0,BUTTON_HEIGHT); }

		@Override
		protected Builder getSelf() { return this; }

		Supplier<TradeContext> context = () -> null;
		Supplier<TradeData> trade = () -> null;
		boolean displayOnly = false;
		BiFunction<TraderData,TradeData,Boolean> isSelected = (a,b) -> false;
		BiFunction<TraderData,TradeData,List<Component>> extraTooltips = (a,b) -> null;

		public Builder context(Supplier<TradeContext> context) { this.context = context; return this; }
		public Builder trade(TradeData trade) { return this.trade(() -> trade); }
		public Builder trade(Supplier<TradeData> trade) { this.trade = trade; return this; }
		public Builder displayOnly() { this.displayOnly = true; return this; }
		public Builder selectedState(BiFunction<TraderData,TradeData,Boolean> isSelected) { this.isSelected = isSelected; return this; }
		public Builder extraTooltips(Component tooltip) { this.extraTooltips = (a,b) -> ImmutableList.of(tooltip); return this;}
		public Builder extraTooltips(TextEntry tooltip) { this.extraTooltips = (a, b) -> tooltip.getAsList(); return this;}
		public Builder extraTooltips(List<Component> tooltip) { this.extraTooltips = (a, b) -> tooltip; return this;}
		public Builder extraTooltips(Supplier<List<Component>> tooltip) { this.extraTooltips = (a, b) -> tooltip.get(); return this; }
		public Builder extraTooltips(BiFunction<TraderData,TradeData,List<Component>> tooltip) { this.extraTooltips = tooltip; return this; }

		public TradeButton build() { return new TradeButton(this); }

	}

}