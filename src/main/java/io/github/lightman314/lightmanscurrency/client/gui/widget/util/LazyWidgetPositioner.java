package io.github.lightman314.lightmanscurrency.client.gui.widget.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IPreRender;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.util.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.IRotatableWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LazyWidgetPositioner implements IPreRender, IWidgetPositioner {

	@Deprecated
	public static final ISimplePositionerMode MODE_TOPDOWN = positioner -> positioner.startPos().offset(0, positioner.widgetSize * positioner.getPositionIndex());
	@Deprecated
	public static final ISimplePositionerMode MODE_BOTTOMUP = positioner -> positioner.startPos().offset(0, -positioner.widgetSize * positioner.getPositionIndex());

	@Nonnull
	public static IPositionerMode createClockwiseWraparound(@Nonnull ScreenArea edges,@Nonnull WidgetRotation startingEdge) { return new WraparoundMode(edges,startingEdge,true); }
	@Nonnull
	public static IPositionerMode createCounterClockwiseWraparound(@Nonnull ScreenArea edges, @Nonnull WidgetRotation startingEdge)  { return new WraparoundMode(edges,startingEdge,false); }

	public static IPositionerMode createTopdown() { return createTopdown(null); }
	public static IPositionerMode createTopdown(@Nullable WidgetRotation rotation) { return new StraightMode(0,1,rotation); }
	public static IPositionerMode createBottomup() { return createBottomup(null); }
	public static IPositionerMode createBottomup(@Nullable WidgetRotation rotation) { return new StraightMode(0,-1,rotation); }
	public static IPositionerMode createLeftRight() { return createLeftRight(null); }
	public static IPositionerMode createLeftRight(@Nullable WidgetRotation rotation) { return new StraightMode(1,0,rotation); }
	public static IPositionerMode createRightLeft() { return createRightLeft(null); }
	public static IPositionerMode createRightLeft(@Nullable WidgetRotation rotation) { return new StraightMode(-1,0,rotation); }

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

		@Nonnull
		default IPositionerMode andThen(@Nonnull Consumer<EasyWidget> action) { return this.andThen((p,w) -> action.accept(w)); }
		default IPositionerMode andThen(@Nonnull BiConsumer<LazyWidgetPositioner,EasyWidget> action) { return new CombinedMode(this,action); }

	}

	@Deprecated
	public interface ISimplePositionerMode extends IPositionerMode {
		@Override
		default void moveWidget(@Nonnull LazyWidgetPositioner positioner, @Nonnull EasyWidget widget) { widget.setPosition(this.getPosition(positioner)); }
		@Nonnull
		ScreenPosition getPosition(@Nonnull LazyWidgetPositioner positioner);
	}

	private static class WraparoundMode implements IPositionerMode
	{
		private final ScreenArea area;
		private final WidgetRotation startingEdge;
		private final boolean clockwise;
		private WraparoundMode(@Nonnull ScreenArea area, WidgetRotation startingEdge, boolean clockwise)
		{
			this.area = area;
			this.startingEdge = startingEdge;
			this.clockwise = clockwise;
		}
		@Override
		public void moveWidget(@Nonnull LazyWidgetPositioner positioner, @Nonnull EasyWidget widget) {

			Pair<WidgetRotation,Integer> edgeAndSlot = this.edgeAndPosForSlot(positioner.widgetSize,positioner.getPositionIndex());
			//Get the corner
			WidgetRotation edge = edgeAndSlot.getFirst();
			int slot = edgeAndSlot.getSecond();
			ScreenPosition startPos = this.startPosForEdge(edge,positioner.widgetSize);
			int xOffPer = this.isHorizEdge(edge) ? 0 : positioner.widgetSize;
			int yOffPer = this.isHorizEdge(edge) ? positioner.widgetSize : 0;
			int mult = this.moveMultForEdge(edge);
			widget.setPosition(startPos.offset(xOffPer * mult * slot, yOffPer * mult * slot));
			if(widget instanceof IRotatableWidget rw)
				rw.setRotation(edge);
		}
		private int countForHorizEdge(int widgetSize) { return this.area.height / widgetSize; }
		private int countForVertEdge(int widgetSize) { return this.area.width / widgetSize; }
		private Pair<WidgetRotation,Integer> edgeAndPosForSlot(int widgetSize, int slot)
		{
			int horizLimit = this.countForHorizEdge(widgetSize);
			int vertLimit = this.countForVertEdge(widgetSize);
			WidgetRotation edge = this.startingEdge;
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
		private WidgetRotation nextEdge(WidgetRotation edge) { return this.clockwise ? edge.clockwise() : edge.counterClockwise(); }
		private boolean isHorizEdge(WidgetRotation edge) { return edge == WidgetRotation.LEFT || edge == WidgetRotation.RIGHT; }
		private ScreenPosition startPosForEdge(WidgetRotation edge, int widgetSize)
		{
			return switch (edge) {
				case TOP -> this.clockwise ? this.area.pos.offset(0, -widgetSize) : this.area.pos.offset(this.area.width - widgetSize, -widgetSize);
				case RIGHT -> this.clockwise ? this.area.pos.offset(this.area.width, 0) : this.area.pos.offset(this.area.width, this.area.height - widgetSize);
				case BOTTOM -> this.clockwise ? this.area.pos.offset(this.area.width - widgetSize,this.area.height) : this.area.pos.offset(0, this.area.height);
				case LEFT -> this.clockwise ? this.area.pos.offset(-widgetSize, this.area.height - widgetSize) : this.area.pos.offset(-widgetSize, 0);
			};
		}
		private int moveMultForEdge(WidgetRotation edge)
		{
			return switch (edge) {
				case TOP,RIGHT -> this.clockwise ? 1 : -1;
				case BOTTOM,LEFT -> this.clockwise ? -1 : 1;
			};
		}
	}

	private static class StraightMode implements IPositionerMode
	{
		private final int xMult;
		private final int yMult;
		private final WidgetRotation rotation;
		StraightMode(int xMult,int yMult,@Nullable WidgetRotation rotation)
		{
			this.xMult = xMult;
			this.yMult = yMult;
			this.rotation = rotation;
		}

		@Override
		public void moveWidget(@Nonnull LazyWidgetPositioner positioner, @Nonnull EasyWidget widget) {
			widget.setPosition(positioner.startPos().offset(positioner.widgetSize * this.xMult * positioner.getPositionIndex(),positioner.widgetSize * this.yMult * positioner.getPositionIndex()));
			if(this.rotation != null && widget instanceof IRotatableWidget w)
				w.setRotation(this.rotation);
		}

	}

	private static class CombinedMode implements IPositionerMode
	{
		private final IPositionerMode mode;
		private final BiConsumer<LazyWidgetPositioner,EasyWidget> extraAction;
		CombinedMode(@Nonnull IPositionerMode mode, @Nonnull BiConsumer<LazyWidgetPositioner,EasyWidget> extraAction)
		{
			this.mode = mode;
			this.extraAction = extraAction;
		}

		@Override
		public void moveWidget(@Nonnull LazyWidgetPositioner positioner, @Nonnull EasyWidget widget) {
			this.mode.moveWidget(positioner,widget);
			this.extraAction.accept(positioner,widget);
		}

	}

}