package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.List;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class ScrollTextDisplay extends EasyWidget {

	private final Supplier<List<? extends Component>> textSource;
	public boolean invertText = false;
	public int backgroundColor = 0xFF000000;
	public int textColor = 0xFFFFFF;
	private int columnCount = 1;
	public void setColumnCount(int columnCount) { this.columnCount = MathUtil.clamp(columnCount, 1, Integer.MAX_VALUE); }
	
	public ScrollTextDisplay(ScreenPosition pos, int width, int height, Supplier<List<? extends Component>> textSource) { this(pos.x, pos.y, width, height, textSource); }
	public ScrollTextDisplay(int x, int y, int width, int height, Supplier<List<? extends Component>> textSource)
	{
		super(x, y, width, height);
		this.textSource = textSource;
	}

	@Override
	public ScrollTextDisplay withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

	private int scroll = 0;
	
	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui)
	{
		
		if(!this.visible)
			return;
		
		//Render the background
		gui.fill(this.getArea().atPosition(ScreenPosition.ZERO), this.backgroundColor);
		
		//Start rendering the text
		List<? extends Component> text = this.textSource.get();
		
		this.validateScroll(text.size());
		int i = this.getStartingIndex(text.size());
		int columnWidth = this.getColumnWidth();
		int bottom = this.getY() + this.height;
		for(int yPos = 2; yPos < bottom && i >= 0 && i < text.size();)
		{
			int rowHeight = 0;
			for(int col = 0; col < this.columnCount && i >= 0 && i < text.size(); ++col)
			{
				int xPos = this.getXPos(col);
				Component thisText = text.get(i);
				int thisHeight = gui.font.wordWrapHeight(thisText.getString(), columnWidth);
				if(yPos + thisHeight < bottom)
					gui.drawWordWrap(thisText, xPos, yPos, columnWidth, this.textColor);
				if(thisHeight > rowHeight)
					rowHeight = thisHeight;
				//Increment the text index
				i = this.invertText ? i - 1 : i + 1;
			}
			yPos += rowHeight;
		}
		
	}
	
	private void validateScroll(int listSize)
	{
		if(this.scroll * columnCount >= listSize)
			this.scroll = MathUtil.clamp(this.scroll, 0, (listSize / columnCount - 1));
	}
	
	private int getStartingIndex(int listSize)
	{
		return this.invertText ? listSize - 1 - (this.scroll * this.columnCount) : this.scroll * this.columnCount;
	}
	
	private int getColumnWidth()
	{
		return ((this.width - 4) / this.columnCount);
	}
	
	private int getXPos(int column)
	{
		int columnSpacing = this.width / this.columnCount;
		return 2 + column * columnSpacing;
	}
	
	private boolean canScrollDown()
	{
		return this.scroll < this.textSource.get().size();
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta)
	{
		if(!this.visible)
			return false;
		
		if(delta < 0)
		{			
			if(this.canScrollDown())
				scroll++;
			else
				return false;
		}
		else if(delta > 0)
		{
			if(scroll > 0)
				scroll--;
			else
				return false;
		}
		
		return true;
	}

	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narrator) { }


}
