package io.github.lightman314.lightmanscurrency.client.gui.widget.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IPreRender;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;

public class LazyWidgetPositioner implements IPreRender {

	public static final Function<LazyWidgetPositioner,ScreenPosition> MODE_TOPDOWN = positioner -> positioner.startPos().offset(0, positioner.widgetSize * positioner.getPositionIndex());
	
	public static final Function<LazyWidgetPositioner,ScreenPosition> MODE_BOTTOMUP = positioner -> positioner.startPos().offset(0, -positioner.widgetSize * positioner.getPositionIndex());
	
	private final IEasyScreen screen;
	private final Function<LazyWidgetPositioner,ScreenPosition> mode;
	private final List<EasyWidget> widgetList = new ArrayList<>();

	private final ScreenPosition offset;
	public final int widgetSize;
	public final ScreenPosition startPos() { return this.screen.getCorner().offset(this.offset); }
	
	private int posIndex;
	public int getPositionIndex() { return this.posIndex; }

	public static LazyWidgetPositioner create(IEasyScreen screen, Function<LazyWidgetPositioner,ScreenPosition> mode, ScreenPosition offset, int widgetSize) { return new LazyWidgetPositioner(screen, mode, offset, widgetSize); }
	public static LazyWidgetPositioner create(IEasyScreen screen, Function<LazyWidgetPositioner,ScreenPosition> mode, int x1, int y1, int widgetSize) {
		return new LazyWidgetPositioner(screen, mode, ScreenPosition.of(x1, y1), widgetSize);
	}

	private LazyWidgetPositioner(IEasyScreen screen, Function<LazyWidgetPositioner,ScreenPosition> mode, ScreenPosition offset, int widgetSize) {
		this.screen = Objects.requireNonNull(screen);
		this.mode = Objects.requireNonNull(mode);
		this.offset = offset;
		this.widgetSize = widgetSize;
	}
	
	public void addWidget(EasyWidget widget) {
		if(widget != null && !this.widgetList.contains(widget))
			this.widgetList.add(widget);
	}

	public void addWidgets(EasyWidget... widgets) {
		for(EasyWidget w : widgets)
			this.addWidget(w);
	}

	@Override
	public void preRender(EasyGuiGraphics gui) {
		this.posIndex = 0;
		for (EasyWidget w : this.widgetList) {
			if (w.isVisible()) {
				w.setPosition(this.mode.apply(this));
				this.posIndex++;
			}
		}
	}

	public void clear() { this.widgetList.clear(); }
	
}
