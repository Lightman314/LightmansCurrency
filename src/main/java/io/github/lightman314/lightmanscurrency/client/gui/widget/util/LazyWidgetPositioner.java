package io.github.lightman314.lightmanscurrency.client.gui.widget.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IPreRender;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.util.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.IRotatableWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;

import javax.annotation.Nonnull;

public class LazyWidgetPositioner implements IPreRender, IWidgetPositioner {

	public static final ISimplePositionerMode MODE_TOPDOWN = positioner -> positioner.startPos().offset(0, positioner.widgetSize * positioner.getPositionIndex());
	
	public static final ISimplePositionerMode MODE_BOTTOMUP = positioner -> positioner.startPos().offset(0, -positioner.widgetSize * positioner.getPositionIndex());

	@Nonnull
	public static IPositionerMode createClockwiseWraparound(@Nonnull ScreenArea edges,int startingEdge) { return new WraparoundMode(edges,startingEdge,true); }
	@Nonnull
	public static IPositionerMode createCounterClockwiseWraparound(@Nonnull ScreenArea edges, int startingEdge)  { return new WraparoundMode(edges,startingEdge,false); }

	private final IEasyScreen screen;
	private final IPositionerMode mode;
	private final List<EasyWidget> widgetList = new ArrayList<>();

	private final ScreenPosition offset;
	public final int widgetSize;
	public final ScreenPosition startPos() { return this.screen.getCorner().offset(this.offset); }
	
	private int posIndex;
	public int getPositionIndex() { return this.posIndex; }

	public static LazyWidgetPositioner create(IEasyScreen screen, IPositionerMode mode, int widgetSize) { return new LazyWidgetPositioner(screen, mode, ScreenPosition.ZERO, widgetSize); }
	public static LazyWidgetPositioner create(IEasyScreen screen, IPositionerMode mode, ScreenPosition offset, int widgetSize) { return new LazyWidgetPositioner(screen, mode, offset, widgetSize); }
	public static LazyWidgetPositioner create(IEasyScreen screen, IPositionerMode mode, int x1, int y1, int widgetSize) {
		return new LazyWidgetPositioner(screen, mode, ScreenPosition.of(x1, y1), widgetSize);
	}

	private LazyWidgetPositioner(IEasyScreen screen, IPositionerMode mode, ScreenPosition offset, int widgetSize) {
		this.screen = Objects.requireNonNull(screen);
		this.mode = Objects.requireNonNull(mode);
		this.offset = offset;
		this.widgetSize = widgetSize;
	}

	@Override
	public void addWidget(@Nonnull EasyWidget widget) {
		if(widget != null && !this.widgetList.contains(widget))
			this.widgetList.add(widget);
	}

	@Override
	public void preRender(@Nonnull EasyGuiGraphics gui) {
		this.posIndex = 0;
		for (EasyWidget w : this.widgetList) {
			if (w.isVisible()) {
				this.mode.moveWidget(this,w);
				//w.setPosition(this.mode.apply(this));
				this.posIndex++;
			}
		}
	}

	public void clear() { this.widgetList.clear(); }

	public interface IPositionerMode
	{
		void moveWidget(@Nonnull LazyWidgetPositioner positioner,@Nonnull EasyWidget widget);
	}

	public interface ISimplePositionerMode extends IPositionerMode {
		@Override
		default void moveWidget(@Nonnull LazyWidgetPositioner positioner, @Nonnull EasyWidget widget) { widget.setPosition(this.getPosition(positioner)); }
		@Nonnull
		ScreenPosition getPosition(@Nonnull LazyWidgetPositioner positioner);
	}

	private static class WraparoundMode implements IPositionerMode
	{
		private final ScreenArea area;
		private final int startingEdge;
		private final boolean clockwise;
		private WraparoundMode(@Nonnull ScreenArea area, int startingEdge, boolean clockwise)
		{
			this.area = area;
			this.startingEdge = startingEdge;
			this.clockwise = clockwise;
		}
		@Override
		public void moveWidget(@Nonnull LazyWidgetPositioner positioner, @Nonnull EasyWidget widget) {

			Pair<Integer,Integer> edgeAndSlot = this.edgeAndPosForSlot(positioner.widgetSize,positioner.getPositionIndex());
			//Get the corner
			int edge = edgeAndSlot.getFirst();
			int slot = edgeAndSlot.getSecond();
			ScreenPosition startPos = this.startPosForEdge(edge,positioner.widgetSize);
			int xOffPer = this.isHorizEdge(edge) ? positioner.widgetSize : 0;
			int yOffPer = this.isHorizEdge(edge) ? 0 : positioner.widgetSize;
			int mult = this.moveMultForEdge(edge);
			widget.setPosition(startPos.offset(xOffPer * mult * slot, yOffPer * mult * slot));
			if(widget instanceof IRotatableWidget rw)
				rw.setRotation(edge);
		}
		private int countForHorizEdge(int widgetSize) { return this.area.width / widgetSize; }
		private int countForVertEdge(int widgetSize) { return this.area.height / widgetSize; }
		private Pair<Integer,Integer> edgeAndPosForSlot(int widgetSize, int slot)
		{
			int horizLimit = this.countForHorizEdge(widgetSize);
			int vertLimit = this.countForVertEdge(widgetSize);
			int edge = this.startingEdge;
			while(true)
			{
				if(this.isHorizEdge(edge))
				{
					if(slot < horizLimit)
						return Pair.of(edge,slot);
					slot -= horizLimit;
                }
				else
				{
					if(slot < vertLimit)
						return Pair.of(edge,slot);
					slot -= vertLimit;
                }
                edge = this.nextEdge(edge);
            }
		}
		private int nextEdge(int edge) { return this.clockwise ? (edge + 1) % 4 : (edge - 1) % 4; }
		private boolean isHorizEdge(int edge) { return edge % 2 == 0; }
		private ScreenPosition startPosForEdge(int edge, int widgetSize)
		{
            return switch (edge) {
                case 0 -> this.clockwise ? this.area.pos.offset(0, -widgetSize) : this.area.pos.offset(this.area.width - widgetSize, -widgetSize);
                case 1 -> this.clockwise ? this.area.pos.offset(this.area.width, 0) : this.area.pos.offset(this.area.width, this.area.height - widgetSize);
                case 2 -> this.clockwise ? this.area.pos.offset(this.area.width - widgetSize,this.area.height) : this.area.pos.offset(0, this.area.height);
                case 3 -> this.clockwise ? this.area.pos.offset(-widgetSize, this.area.height - widgetSize) : this.area.pos.offset(-widgetSize, 0);
                default -> ScreenPosition.ZERO;
            };
		}
		private int moveMultForEdge(int edge)
		{
			return switch (edge) {
				case 0, 1 -> this.clockwise ? 1 : -1;
                case 2, 3 -> this.clockwise ? -1 : 1;
				default -> 1;
            };
		}
	}

}
