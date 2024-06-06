package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea.InteractionConsumer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;

public class TradeButton extends EasyButton implements ITooltipSource {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/trade.png");
	
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
	
	public TradeButton(@Nonnull Supplier<TradeContext> contextSource, @Nonnull Supplier<TradeData> tradeSource, Consumer<EasyButton> press) {
		super(0, 0, 0, BUTTON_HEIGHT, press);
		this.tradeSource = tradeSource;
		this.contextSource = contextSource;
		this.recalculateSize();
	}

	@Override
	public TradeButton withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

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
	 * @deprecated Use setPosition(x,y) or setPosition(ScreenPosition)
	 */
	@Deprecated
	public void move(int x, int y) { this.setPosition(x, y); }
	
	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui) {
		
		TradeRenderManager<?> tr = this.getTradeRenderer();
		if(tr == null)
			return;

		TradeContext context = this.getContext();
		
		this.recalculateSize();
		
		this.renderBackground(gui, !context.isStorageMode && !this.displayOnly && this.isHovered);

		LazyOptional<ScreenPosition> arrowPosOptional = tr.arrowPosition(context);
		arrowPosOptional.ifPresent(arrowPos -> this.renderArrow(gui, arrowPos, !context.isStorageMode && !this.displayOnly && this.isHovered));

		//Render custom display stuff in front of the arrow, not behind it.
		try { tr.renderAdditional(this, gui, context);
		} catch(Exception e) { LightmansCurrency.LogError("Error on additional Trade Button rendering.", e); }

		this.renderAlert(gui, tr.alertPosition(context), tr.getAlertData(context));
		
		this.renderDisplays(gui, tr, context);

		gui.resetColor();
		
	}
	
	private void renderBackground(@Nonnull EasyGuiGraphics gui, boolean isHovered)
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
	
	private void renderArrow(@Nonnull EasyGuiGraphics gui, @Nonnull ScreenPosition position, boolean isHovered)
	{

		if(this.active)
			gui.resetColor();
		else
			gui.setColor(0.5f,0.5f,0.5f);
		
		int vOffset = isHovered ? ARROW_HEIGHT : 0;

		gui.blit(GUI_TEXTURE, position, TEMPLATE_WIDTH, vOffset, ARROW_WIDTH, ARROW_HEIGHT);
		
	}
	
	private void renderAlert(@Nonnull EasyGuiGraphics gui, @Nonnull ScreenPosition position, @Nullable List<AlertData> alerts)
	{
		
		if(alerts == null || alerts.isEmpty())
			return;
		alerts.sort(AlertData::compare);

		alerts.get(0).setShaderColor(gui, this.active ? 1f : 0.5f);
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
	public void renderTooltip(EasyGuiGraphics gui) {

		if(!this.isMouseOver(gui.mousePos))
			return;

		TradeRenderManager<?> tr = this.getTradeRenderer();
		if(tr == null)
			return;

		TradeContext context = this.getContext();

		int mouseX = gui.mousePos.x;
		int mouseY = gui.mousePos.y;

		List<Component> tooltips = new ArrayList<>();

		this.tryAddTooltip(tooltips, tr.getAdditionalTooltips(context, mouseX - this.getX(), mouseY - this.getY()));

		for(Pair<DisplayEntry,DisplayData> display : getInputDisplayData(tr, context))
		{
			if(display.getFirst().isMouseOver(this.getX(), this.getY(), display.getSecond(), mouseX, mouseY))
			{
				if(display.getFirst().trySelfRenderTooltip(gui))
					return;
				this.tryAddTooltip(tooltips, display.getFirst().getTooltip());
			}
		}

		for(Pair<DisplayEntry,DisplayData> display : getOutputDisplayData(tr, context))
		{
			if(display.getFirst().isMouseOver(this.getX(), this.getY(), display.getSecond(), mouseX, mouseY))
			{
				if(display.getFirst().trySelfRenderTooltip(gui))
					return;
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
			return;

		gui.renderComponentTooltip(tooltips);
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


	

	
}
