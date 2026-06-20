package io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.IFancyScreen;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.client.util.ScreenPosition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

//Implements scrollable
public final class WidgetPositioner implements IWidgetPositioner, IScrollable {

    private final List<IMoveableWidget> widgets = new ArrayList<>();
    private final PositionCalculator positioner;
    private final IntSupplier displayable;
    private int scroll = 0;
    public WidgetPositioner(PositionCalculator positioner,IntSupplier displayable) {
        this.positioner = positioner;
        this.displayable = displayable;
    }

    @Override
    public void addWidget(IMoveableWidget widget) { this.widgets.add(widget); }

    @Override
    public void clearWidgets() { this.widgets.clear(); }

    //Scrollable implementation
    @Override
    public int getScroll() { return this.scroll; }
    @Override
    public void setScroll(int scroll) { this.scroll = scroll; }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll((int)this.widgets.stream().filter(IMoveableWidget::isVisible).count(),this.displayable.getAsInt()); }
    @Override
    public void renderTick() {
        //Validate the scroll count
        this.validateScroll();
        int index = 0;
        int ignoreCount = this.scroll;
        int displayable = this.displayable.getAsInt();
        for(IMoveableWidget w : new ArrayList<>(this.widgets))
        {
            if(w.isVisible())
            {
                if(ignoreCount > 0)
                {
                    ignoreCount--;
                    w.hideThisFrame();
                }
                else if(index > displayable)
                    w.hideThisFrame();
                else
                {
                    Pair<ScreenPosition,WidgetFacing> position = this.positioner.calculatePosition(index++);
                    if(position != null)
                        w.move(position.getFirst(),position.getSecond());
                    else //If no position could be calculated for the widget, hide it until the next frame
                        w.hideThisFrame();
                }
            }
        }
        this.positioner.reset();
    }

    public static WidgetPositioner leftEdge(IFancyScreen screen,int widgetSize) {
        return new WidgetPositioner(index -> Pair.of(screen.getCorner().offset(widgetSize * -1,widgetSize * index),WidgetFacing.LEFT),vertialLimit(screen,widgetSize));
    }

    public static WidgetPositioner rightEdge(IFancyScreen screen,int widgetSize) {
        return new WidgetPositioner(index -> Pair.of(screen.getCorner().offset(screen.getWidth(),widgetSize * index),WidgetFacing.RIGHT),vertialLimit(screen,widgetSize));
    }

    public static WidgetPositioner topEdge(IFancyScreen screen,int widgetSize) {
        return new WidgetPositioner(index -> Pair.of(screen.getCorner().offset(widgetSize * index,widgetSize * -1),WidgetFacing.TOP),horizontalLimit(screen,widgetSize));
    }

    public static WidgetPositioner bottomEdge(IFancyScreen screen,int widgetSize) {
        return new WidgetPositioner(index -> Pair.of(screen.getCorner().offset(widgetSize * index,screen.getHeight()),WidgetFacing.BOTTOM),horizontalLimit(screen,widgetSize));
    }

    private static IntSupplier vertialLimit(IFancyScreen screen,int widgetSize) { return () -> screen.getHeight() / widgetSize; }
    private static IntSupplier horizontalLimit(IFancyScreen screen,int widgetSize) { return () -> screen.getWidth() / widgetSize; }

    public static WidgetPositioner allEdgesClockwise(IFancyScreen screen,int widgetSize,WidgetFacing start) {
        RotationCalculator calculator = new ClockwiseCalculator(screen,widgetSize,start);
        return new WidgetPositioner(calculator,calculator);
    }
    public static WidgetPositioner allEdgesCounterClockwise(IFancyScreen screen, int widgetSize,WidgetFacing start) {
        RotationCalculator calculator = new CounterClockwiseCalculator(screen,widgetSize,start);
        return new WidgetPositioner(calculator,calculator);
    }

    public interface PositionCalculator
    {
        @Nullable
        Pair<ScreenPosition,WidgetFacing> calculatePosition(int index);
        default void reset() {}
    }

    private abstract static class RotationCalculator implements PositionCalculator, IntSupplier
    {
        private final IFancyScreen screen;
        private final int widgetSize;
        private final WidgetFacing startingFace;

        private WidgetFacing currentFace;
        private int partialIndex = 0;
        private int faceCount = 0;

        protected RotationCalculator(IFancyScreen screen,int widgetSize,WidgetFacing startingFace) {
            this.screen = screen;
            this.widgetSize = widgetSize;
            this.startingFace = this.currentFace = startingFace;
        }

        protected abstract int getFacingDirection(WidgetFacing facing);
        protected abstract int getFacingStartPos(IFancyScreen screen,WidgetFacing facing,int widgetSize);
        protected abstract WidgetFacing getNextFace(WidgetFacing oldFace);

        protected ScreenPosition getCorner(IFancyScreen screen,WidgetFacing facing,int widgetSize) {
            return switch (facing) {
                case LEFT -> screen.getCorner().offset(widgetSize * -1,0);
                case TOP -> screen.getCorner().offset(0,widgetSize * -1);
                case RIGHT -> screen.getCorner().offset(screen.getWidth(),0);
                case BOTTOM -> screen.getCorner().offset(screen.getHeight(),0);
            };
        }

        protected boolean offsetX(WidgetFacing facing) {
            return switch (facing) {
                case TOP,BOTTOM -> true;
                case LEFT,RIGHT -> false;
            };
        }

        protected ScreenPosition getOffset(IFancyScreen screen,WidgetFacing facing,int widgetSize,int index)
        {
            if(this.offsetX(facing))
                return ScreenPosition.of(this.getFacingStartPos(screen,facing,widgetSize) + (widgetSize * index * this.getFacingDirection(facing)),0);
            else
                return ScreenPosition.of(0,this.getFacingStartPos(screen,facing,widgetSize) + (widgetSize * index * this.getFacingDirection(facing)));
        }

        protected boolean shouldMoveToNextFace(IFancyScreen screen,int usedSpace,WidgetFacing facing) {
            return switch (facing) {
                case TOP,BOTTOM -> usedSpace > screen.getWidth();
                default -> usedSpace > screen.getHeight();
            };
        }

        @Override
        @Nullable
        public Pair<ScreenPosition, WidgetFacing> calculatePosition(int index) {
            //If we've gone through all four faces, flag the positioner as changed
            if(this.faceCount >= 4)
                return null;
            //Assume that this
            Pair<ScreenPosition,WidgetFacing> result = Pair.of(
                    this.getCorner(this.screen,this.currentFace,this.widgetSize)
                            .offset(this.getOffset(this.screen,this.currentFace,widgetSize,this.partialIndex++)),
                    this.currentFace);
            if(this.shouldMoveToNextFace(this.screen,this.widgetSize * this.partialIndex,this.currentFace))
            {
                this.currentFace = this.getNextFace(this.currentFace);
                this.partialIndex = 0;
                this.faceCount++;
            }
            return result;
        }

        @Override
        public final void reset() {
            this.currentFace = this.startingFace;
            this.partialIndex = 0;
            this.faceCount = 0;
        }

        //Calculates the renderable limit
        @Override
        public int getAsInt() {
            int horiz = this.screen.getWidth() / this.widgetSize;
            int vert = this.screen.getHeight() / this.widgetSize;
            return horiz + horiz + vert + vert;
        }

    }

    private static class ClockwiseCalculator extends RotationCalculator
    {
        protected ClockwiseCalculator(IFancyScreen screen, int widgetSize, WidgetFacing startingFace) { super(screen, widgetSize, startingFace); }
        @Override
        protected int getFacingDirection(WidgetFacing facing) {
            return switch (facing) {
                case TOP, RIGHT -> 1;
                case LEFT,BOTTOM -> -1;
            };
        }
        @Override
        protected int getFacingStartPos(IFancyScreen screen, WidgetFacing facing,int widgetSize) {
            return switch (facing) {
                case TOP,RIGHT -> 0;
                case BOTTOM -> screen.getWidth() - widgetSize;
                case LEFT -> screen.getHeight() - widgetSize;
            };
        }
        @Override
        protected WidgetFacing getNextFace(WidgetFacing oldFace) { return oldFace.nextClockwise(); }
    }

    private static class CounterClockwiseCalculator extends RotationCalculator
    {

        protected CounterClockwiseCalculator(IFancyScreen screen, int widgetSize, WidgetFacing startingFace) { super(screen, widgetSize, startingFace); }

        @Override
        protected int getFacingDirection(WidgetFacing facing) {
            return switch (facing) {
                case TOP,RIGHT -> -1;
                case LEFT,BOTTOM -> 1;
            };
        }

        @Override
        protected int getFacingStartPos(IFancyScreen screen, WidgetFacing facing, int widgetSize) {
            return 0;
        }

        @Override
        protected WidgetFacing getNextFace(WidgetFacing oldFace) {
            return null;
        }
    }

}