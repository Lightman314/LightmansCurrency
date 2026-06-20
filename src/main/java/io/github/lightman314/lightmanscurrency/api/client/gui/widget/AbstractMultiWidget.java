package io.github.lightman314.lightmanscurrency.api.client.gui.widget;

import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IMultiWidget;
import io.github.lightman314.lightmanscurrency.api.client.util.ScreenArea;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMultiWidget extends EasyWidget implements IMultiWidget {

    private IWidgetHolder parent = null;
    private final List<Object> children = new ArrayList<>();

    public AbstractMultiWidget(Builder<?,? extends AbstractMultiWidget> builder) { super(builder); }

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
        if(this.parent != null)
        {
            this.parent.removeChild(child);
        }
        this.children.remove(child);
    }

    @Override
    public final void removeChildren() {
        if(this.parent != null)
        {
            for(Object child : this.children)
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

}
