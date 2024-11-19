package io.github.lightman314.lightmanscurrency.client.gui.widget.easy;

import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class EasyWidgetWithChildren extends EasyWidget {

    private final List<Object> children = new ArrayList<>();

    protected EasyWidgetWithChildren(@Nonnull EasyBuilder<?> builder) { super(builder); }

    public boolean addChildrenBeforeThis() { return false; }

    public void addChildren() { this.addChildren(this.getArea()); }
    public abstract void addChildren(@Nonnull ScreenArea area);
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
