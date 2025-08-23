package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class ScrollTextDisplay extends EasyWidget {

	private final Supplier<List<? extends Component>> textSource;
	public boolean invertText = false;
	public int backgroundColor = 0xFF000000;
	public int textColor = 0xFFFFFF;
	private final int columnCount;

	private ScrollTextDisplay(@Nonnull Builder builder)
	{
		super(builder);
		this.textSource = builder.text;
		this.columnCount = builder.columns;
	}

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
		int bottom = this.getHeight();
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
	public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY)
	{
		if(!this.visible)
			return false;
		
		if(deltaY < 0)
		{			
			if(this.canScrollDown())
				scroll++;
			else
				return false;
		}
		else if(deltaY > 0)
		{
			if(scroll > 0)
				scroll--;
			else
				return false;
		}
		
		return true;
	}

	@Nonnull
	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	@ParametersAreNonnullByDefault
	public static class Builder extends EasySizableBuilder<Builder>
	{
		private Builder() { }
		@Override
		protected Builder getSelf() { return this; }

		int columns = 1;
		private Supplier<List<? extends Component>> text = ArrayList::new;

		public Builder text(Supplier<List<? extends Component>> text) { this.text = text; return this; }
		public Builder columns(int columns) { this.columns = columns; return this; }

		public ScrollTextDisplay build() { return new ScrollTextDisplay(this); }

	}

}
