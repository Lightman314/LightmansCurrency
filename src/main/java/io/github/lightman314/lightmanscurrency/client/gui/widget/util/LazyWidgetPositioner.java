package io.github.lightman314.lightmanscurrency.client.gui.widget.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.client.gui.components.AbstractWidget;

public class LazyWidgetPositioner {

	public static final Function<LazyWidgetPositioner,ScreenPosition> MODE_TOPDOWN = positioner -> ScreenPosition.of(positioner.startX(), positioner.startY() + (positioner.widgetSize * positioner.getPositionIndex()));

	public static final Function<LazyWidgetPositioner,ScreenPosition> MODE_BOTTOMUP = positioner -> ScreenPosition.of(positioner.startX(), positioner.startY() - (positioner.widgetSize * positioner.getPositionIndex()));

	private final IScreen screen;
	private final Function<LazyWidgetPositioner,ScreenPosition> mode;
	private final List<AbstractWidget> widgetList = new ArrayList<>();

	private final int x1;
	private final int y1;
	public final int widgetSize;
	public final int startX() { return this.screen.getGuiLeft() + this.x1; }
	public final int startY() { return this.screen.getGuiTop() + this.y1; }

	private int posIndex;
	public int getPositionIndex() { return this.posIndex; }

	@SafeVarargs
	@Deprecated
	public static LazyWidgetPositioner create(IScreen screen, Function<LazyWidgetPositioner,ScreenPosition> mode, int x1, int y1, int widgetSize, AbstractWidget... widgets) {
		return new LazyWidgetPositioner(screen, mode, x1, y1, widgetSize, widgets);
	}

	public static LazyWidgetPositioner create(IScreen screen, Function<LazyWidgetPositioner,ScreenPosition> mode, int x1, int y1, int widgetSize) {
		return new LazyWidgetPositioner(screen, mode, x1, y1, widgetSize);
	}

	private LazyWidgetPositioner(IScreen screen, Function<LazyWidgetPositioner,ScreenPosition> mode, int x1, int y1, int widgetSize) {
		this.screen = Objects.requireNonNull(screen);
		this.mode = Objects.requireNonNull(mode);
		this.x1 = x1;
		this.y1 = y1;
		this.widgetSize = widgetSize;
		this.screen.addTickListener(this::reposition);
	}

	@SafeVarargs
	private LazyWidgetPositioner(IScreen screen, Function<LazyWidgetPositioner,ScreenPosition> mode, int x1, int y1, int widgetSize, AbstractWidget... widgets) {
		this.screen = Objects.requireNonNull(screen);
		this.mode = Objects.requireNonNull(mode);
		this.x1 = x1;
		this.y1 = y1;
		this.widgetSize = widgetSize;
		this.screen.addTickListener(this::reposition);
		for(AbstractWidget w : widgets) this.addWidget(w);
	}

	public void addWidget(AbstractWidget widget) {
		if(widget != null && !this.widgetList.contains(widget))
			this.widgetList.add(widget);
	}

	public void addWidgets(AbstractWidget... widgets) {
		for(AbstractWidget w : widgets)
			this.addWidget(w);
	}

	public void reposition() {
		this.posIndex = 0;
		for (AbstractWidget w : this.widgetList) {
			if (w.visible) {
				ScreenPosition pos = this.mode.apply(this);
				w.x = pos.x;
				w.y = pos.y;
				this.posIndex++;
			}
		}
	}

	public void clear() { this.widgetList.clear(); }

}