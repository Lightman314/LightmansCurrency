package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.ITradeData;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.ITraderSource;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class TradeButtonArea extends AbstractWidget {
	
	
	public static final Function<ITradeData,Boolean> FILTER_VALID = trade -> trade.isValid();
	public static final Function<ITradeData,Boolean> FILTER_ANY = trade -> true;
	
	private final Supplier<ITraderSource> traderSource;
	private final Function<ITrader, TradeContext> getContext;
	
	//private final int x,y,width,height;
	
	private final List<TradeButton> allButtons = new ArrayList<>();
	
	private final Font font;
	
	private final Consumer<TradeButton> addButton;
	private final Consumer<TradeButton> removeButton;
	private final BiConsumer<ITrader,ITradeData> onPress;
	private final Function<ITradeData,Boolean> tradeFilter;
	
	private final int columns;
	private int scroll = 0;
	private int lastFittableLines = 0;
	
	public TradeButtonArea(Supplier<ITraderSource> traderSource, Function<ITrader, TradeContext> getContext, int x, int y, int width, int height, int columns, Consumer<TradeButton> addButton, Consumer<TradeButton> removeButton, BiConsumer<ITrader,ITradeData> onPress, Function<ITradeData,Boolean> tradeFilter)
	{
		super(x, y, width, height, new TextComponent(""));
		this.columns = columns;
		this.traderSource = traderSource;
		this.getContext = getContext;
		this.addButton = addButton;
		this.removeButton = removeButton;
		this.onPress = onPress;
		this.tradeFilter = tradeFilter;
		
		Minecraft mc = Minecraft.getInstance();
		this.font = mc.font;
		
	}
	
	public void init() {
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
	
	public Pair<ITrader,ITradeData> getTradeAndTrader(int displayIndex) {
		int ignoreCount = this.scroll * this.columns;
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
		if(this.scroll > 0 || this.canScrollDown())
		{
			RenderSystem.setShaderTexture(0, TraderScreen.GUI_TEXTURE);
			int xPos = this.x + (this.width / 2) - 4;
			if(this.scroll > 0)
			{
				int yPos = this.y - 6;
				this.blit(pose, xPos, yPos, TraderScreen.WIDTH, 18, 8, 6);
			}
			if(this.canScrollDown())
			{
				int yPos = this.y + this.height - 3;
				this.blit(pose, xPos, yPos, TraderScreen.WIDTH + 8, 18, 8, 6);
			}
		}
	}
	
	//Confirms each trades validity
	public void tick() {
		if(this.lastFittableLines < this.fittableLines())
		{
			//If we need to add more lines, recreate the buttons
			this.resetButtons();
		}
		else
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
				this.addButton.accept(newButton);
				this.allButtons.add(newButton);
				displayIndex++;
			}
		}
		
		this.repositionButtons();
		
	}
	
	private int validTrades() { 
		int count = 0;
		ITraderSource ts = this.traderSource.get();
		if(ts == null)
			return 0;
		List<ITrader> traders = ts.getTraders();
		for(ITrader trader : traders)
		{
			List<? extends ITradeData> trades = trader.getTradeInfo();
			for(ITradeData trade : trades)
			{
				if(trade != null && trade.isValid())
					count++;
			}
		}
		return count;
	}
	
	private int fittableLines() {
		int lineCount = 0;
		int displayIndex = 0;
		int yOffset = 0;
		while(yOffset < this.height)
		{
			int lineHeight = 0;
			//Get relevant info for the buttons in this row
			for(int c = 0; c < this.columns; ++c)
			{
				Pair<ITrader,ITradeData> trade = this.getTradeAndTrader(displayIndex);
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
		int yOffset = 0;
		int fittableLines = this.fittableLines();
		for(int line = 0; line < fittableLines; ++line)
		{
			int lineHeight = 0;
			int visibleButtons = 0;
			int totalWidth = 0;
			int queryIndex = displayIndex;
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
				queryIndex ++;
			}
			//Position the buttons in this row
			int spacing = (this.width - totalWidth)/(visibleButtons + 1);
			int xOffset = spacing;
			for(int c = 0; c < this.columns; ++c)
			{
				Pair<ITrader,ITradeData> trade = this.getTradeAndTrader(displayIndex);
				if(trade.getFirst() != null && trade.getSecond() != null)
				{
					TradeButton button = this.allButtons.get(displayIndex);
					button.move(this.x + xOffset, this.y + yOffset);
					button.visible = true;
					xOffset += trade.getSecond().tradeButtonWidth(this.getContext.apply(trade.getFirst())) + spacing;
				}
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
	
	public void renderTraderName(PoseStack pose, int x, int y, int maxWidth)
	{
		ITraderSource ts = this.traderSource.get();
		if(ts == null)
			return;
		
		String text = "";
		for(ITrader trader : ts.getTraders())
		{
			if(text.isEmpty())
				text = trader.getName().getString();
			else
				text += new TranslatableComponent("gui.lightmanscurrency.trading.listseperator").getString() + trader.getName().getString();
		}
		
		this.font.draw(pose, this.fitText(text, maxWidth), x, y, 0x404040);
		
	}
	
	private String fitText(String text, int width)
	{
		if(this.font.width(text) <= width)
			return text;
		while(this.font.width(text + "...") > width)
		{
			text = text.substring(0,text.length() - 1);
		}
		return text + "...";
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
	
	private boolean canScrollDown() {
		return this.validTrades() - (this.scroll * this.columns) > this.fittableLines() * this.columns;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if(delta < 0)
		{			
			if(this.canScrollDown())
				this.scroll++;
			else
				return false;
		}
		else if(delta > 0)
		{
			if(this.scroll > 0)
				scroll--;
			else
				return false;
		}
		return true;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for(TradeButton b : this.allButtons)
		{
			if(b.isMouseOver(mouseX, mouseY) && b.mouseClicked(mouseX, mouseY, button))
				return true;
		}
		return false;
	}

	@Override
	public void updateNarration(NarrationElementOutput narrator) { }
	
	@Override
	public NarrationPriority narrationPriority() { return NarrationPriority.NONE; }
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) { return true; }
	
}
