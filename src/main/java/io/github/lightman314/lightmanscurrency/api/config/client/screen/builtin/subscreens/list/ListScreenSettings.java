package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class ListScreenSettings {

    private final Consumer<Object> changeHandler;
    public ListScreenSettings(Consumer<Object> changeHandler) { this.changeHandler = changeHandler; }

    private ListOptionScreen screen;
    protected final ListOptionScreen getScreen() { return this.screen; }
    public final void setScreen(@Nullable ListOptionScreen screen) { this.screen = screen; }

    public abstract ScrollingWidgetBuilder buildEntry(int index);

    public abstract int getListSize();
    public abstract void addEntry();
    public boolean canAddEntry() { return this.screen != null && this.screen.canEdit(); }
    public abstract  void removeEntry(int index);
    public boolean canRemoveEntry() { return this.screen != null && this.screen.canEdit(); }

    public boolean canEdit() { return this.screen != null && this.screen.canEdit();}

    public abstract void setEntry(int index,Object newValue);

    protected final void setValue(Object newValue) { this.changeHandler.accept(newValue); }

}
