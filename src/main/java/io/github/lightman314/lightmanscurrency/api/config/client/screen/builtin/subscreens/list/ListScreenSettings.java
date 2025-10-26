package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.AbstractWidget;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ListScreenSettings {

    private final Consumer<Object> changeHandler;
    public ListScreenSettings(Consumer<Object> changeHandler) { this.changeHandler = changeHandler; }

    private ListOptionScreen screen;
    protected final ListOptionScreen getScreen() { return this.screen; }
    public void setScreen(@Nullable ListOptionScreen screen) { this.screen = screen; }

    public abstract AbstractWidget buildEntry(int index);

    public abstract int getListSize();
    public abstract void addEntry();
    public boolean canAddEntry() { return this.screen != null && this.screen.canEdit(); }
    public abstract void removeEntry(int index);
    public boolean canRemoveEntry() { return this.screen != null && this.screen.canEdit(); }

    public abstract void setEntry(int index, Object newValue);

    protected final void setValue(Object newValue) { this.changeHandler.accept(newValue); }

}
