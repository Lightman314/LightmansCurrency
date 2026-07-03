package io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IScrollListener;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.helpers.system.Consumer4;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ScrollArea implements IScrollListener {

    private final ScreenArea area;
    private final IScrollListener listener;
    private ScrollArea(Builder builder) {
        this.area = builder.area;
        this.listener = builder.listener;
    }

    @Override
    public boolean onMouseScrolled(int mouseX, int mouseY, double deltaX, double deltaY) {
        if(this.area.isMouseInArea(mouseX,mouseY))
            return this.listener.onMouseScrolled(mouseX,mouseY,deltaX,deltaY);
        return false;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder
    {
        private ScreenArea area = ScreenArea.ZERO;
        private IScrollListener listener = (x,y,dx,dy) -> false;

        public Builder atPosition(int x,int y) { return this.atPosition(ScreenPosition.of(x,y)); }
        public Builder atPosition(ScreenPosition position) { this.area = this.area.atPosition(position); return this; }

        public Builder ofSize(int width,int height) { this.area = this.area.ofSize(width,height); return this; }
        public Builder ofArea(ScreenArea area) { this.area = area; return this; }

        public Builder withYConsumer(Consumer<Double> yListener) { return this.withConsumer((x, y, dx, dy) -> yListener.accept(dy)); }
        public Builder withXConsumer(Consumer<Double> xListener) { return this.withConsumer((x, y, dx, dy) -> xListener.accept(dx)); }
        public Builder withConsumer(BiConsumer<Double,Double> listener) { return this.withConsumer((x, y, dx, dy) -> listener.accept(dx,dy)); }
        public Builder withConsumer(Consumer4<Integer,Integer,Double,Double> listener) { return this.withListener((x, y, dx, dy) -> {
            listener.accept(x,y,dx,dy);
            return true;
        });}
        public Builder withListener(IScrollListener listener) { this.listener = listener; return this; }

        public Builder forScrollable(IScrollable scrollable) { return this.withListener(scrollable.buildListener()); }

        public ScrollArea build() { return new ScrollArea(this); }
    }

}
