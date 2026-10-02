package io.github.lightman314.lightmanscurrency.api.client.gui.widget;

import io.github.lightman314.lightmanscurrency.api.client.gui.screen.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IMultiWidget;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMultiWidget extends FancyWidget implements IMultiWidget {

    private IWidgetHolder parent = null;
    private final List<Object> children = new ArrayList<>();

    protected AbstractMultiWidget(AbstractBuilder<?,? extends AbstractMultiWidget> builder) { super(builder); }

    @Override
    public final void defineParent(IWidgetHolder screen) { this.parent = screen; }

    @Override
    public <T> T addChild(T child) {
        if(this.parent != null)
        {
            this.parent.addChild(child);
            this.children.add(child);
        }
        return child;
    }

    @Override
    public final void removeChild(Object child) {
        if(this.parent != null) {
            this.parent.removeChild(child);
        }
        this.children.remove(child);
        this.afterChildRemoved(child);
    }

    protected void afterChildRemoved(Object child) { }

    @Override
    public final void removeAllChildren() {
        if(this.parent != null)
        {
            for(Object child : new ArrayList<>(this.children))
                this.parent.removeChild(child);
            this.children.clear();
        }
    }

    @Override
    public final void addEarlyChildren() { this.addEarlyChildren(this.getArea());}
    protected abstract void addEarlyChildren(ScreenArea area);

    @Override
    public final void addLateChildren() { this.addLateChildren(this.getArea()); }
    protected abstract void addLateChildren(ScreenArea area);

    public static abstract class LateChildren extends AbstractMultiWidget {
        protected LateChildren(AbstractBuilder<?, ? extends LateChildren> builder) { super(builder); }
        @Override
        protected final void addEarlyChildren(ScreenArea area) { }
    }

    public static abstract class EarlyChildren extends AbstractMultiWidget {
        protected EarlyChildren(AbstractBuilder<?, ? extends EarlyChildren> builder) { super(builder); }
        @Override
        protected final void addLateChildren(ScreenArea area) { }
    }


}
