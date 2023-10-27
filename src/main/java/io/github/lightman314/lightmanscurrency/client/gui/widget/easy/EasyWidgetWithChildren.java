package io.github.lightman314.lightmanscurrency.client.gui.widget.easy;

import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class EasyWidgetWithChildren extends EasyWidget {

    private final List<Object> children = new ArrayList<>();

    protected EasyWidgetWithChildren(int x, int y, int width, int height) { super(x, y, width, height); }

    protected EasyWidgetWithChildren(ScreenPosition position, int width, int height) { super(position, width, height); }

    protected EasyWidgetWithChildren(ScreenPosition position, int width, int height, Component title) { super(position, width, height, title); }

    protected EasyWidgetWithChildren(ScreenArea area) { super(area); }

    protected EasyWidgetWithChildren(ScreenArea area, Component title) { super(area, title); }

    public boolean addChildrenBeforeThis() { return false; }

    public abstract void addChildren();
    public final void removeChildren()
    {
        for(Object c : this.children)
            this.removeConsumer.accept(c);
        this.children.clear();
    }

    private Consumer<Object> addConsumer;
    private Consumer<Object> removeConsumer;

    public final void pairWithScreen(@Nonnull Consumer<Object> addChildren, @Nonnull Consumer<Object> removeChildren) { this.addConsumer = addChildren; this.removeConsumer = removeChildren; }

    protected final <T> T addChild(@Nonnull T widget)
    {
        if(!this.children.contains(widget))
            this.children.add(widget);
        this.addConsumer.accept(widget);
        return widget;
    }
    protected final void removeChild(@Nonnull Object widget)
    {
        this.children.remove(widget);
        this.removeConsumer.accept(widget);
    }


}
