package io.github.lightman314.lightmanscurrency.client.gui.widget;

import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;

import java.util.function.Consumer;
import java.util.function.Function;

public class ScrollListener implements IScrollListener {

	public ScreenArea area;
	private final Function<Double,Boolean> listener;
	private final IScrollListener deprecatedListener;
	
	public boolean active = true;

	private static Function<Double,Boolean> convertConsumer(Consumer<Double> consumer) { return d -> { consumer.accept(d); return false; }; }

	public ScrollListener(ScreenPosition position, int width, int height, IScrollable scrollable) { this(ScreenArea.of(position, width, height), scrollable::handleScrollWheel); }
	public ScrollListener(ScreenPosition position, int width, int height, Function<Double,Boolean> listener) { this(ScreenArea.of(position, width, height), listener); }
	public ScrollListener(ScreenPosition position, int width, int height, Consumer<Double> listener) { this(ScreenArea.of(position, width, height), convertConsumer(listener)); }
	public ScrollListener(int x, int y, int width, int height, IScrollable scrollable) { this(ScreenArea.of(x,y,width,height), scrollable::handleScrollWheel); }
	public ScrollListener(int x, int y, int width, int height, Function<Double,Boolean> listener) { this(ScreenArea.of(x,y,width,height), listener); }
	public ScrollListener(int x, int y, int width, int height, Consumer<Double> listener) { this(ScreenArea.of(x,y,width,height), convertConsumer(listener)); }
	public ScrollListener(ScreenArea area, IScrollable scrollable) { this(area, scrollable::handleScrollWheel); }
	public ScrollListener(ScreenArea area, Consumer<Double> listener) { this(area, convertConsumer(listener)); }
	public ScrollListener(ScreenArea area, Function<Double,Boolean> listener)
	{
		this.area = area;
		this.deprecatedListener = null;
		this.listener = listener;
	}


	@Deprecated
	public ScrollListener(ScreenPosition position, int width, int height, IScrollListener listener) { this(ScreenArea.of(position, width, height), listener); }
	@Deprecated
	public ScrollListener(int x, int y, int width, int height, IScrollListener listener) { this(ScreenArea.of(x, y, width, height), listener); }
	@Deprecated
	public ScrollListener(ScreenArea area, IScrollListener listener) {
		this.area = area;
		this.deprecatedListener = listener;
		this.listener = d -> this.deprecatedListener.mouseScrolled(0d,0d,d);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta)
	{
		if(this.active && this.area.isMouseInArea(mouseX, mouseY))
		{
			if(this.deprecatedListener != null)
				return this.deprecatedListener.mouseScrolled(mouseX, mouseY, delta);
			else
				return this.listener.apply(delta);
		}
		return false;
	}

}
