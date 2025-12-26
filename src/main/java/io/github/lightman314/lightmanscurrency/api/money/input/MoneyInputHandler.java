package io.github.lightman314.lightmanscurrency.api.money.input;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class MoneyInputHandler {

    public abstract Component inputName();
    public abstract String getUniqueName();
    public boolean isForValue(MoneyValue value) { return value.getUniqueName().equals(this.getUniqueName()); }

    private MoneyValueWidget parent;
    protected boolean isVisible() { return this.parent.isVisible(); }
    protected boolean canChangeHandler() { return this.parent.canChangeHandlers(); }
    protected boolean isLocked() { return this.parent.isLocked(); }
    protected void onInternalHandlerChange() { this.parent.markHandlerChanged(); }
    protected Font getFont() { return this.parent.getFont(); }
    
    protected MoneyValue currentValue() { return this.parent.getCurrentValue(); }
    protected boolean isEmpty() { return this.currentValue().isEmpty(); }
    protected boolean isFree() { return this.currentValue().isFree(); }
    private Consumer<Object> addChild;
    private Consumer<Object> removeChild;
    private Consumer<MoneyValue> changeConsumer;

    private final List<Object> children = new ArrayList<>();

    public final void setup(MoneyValueWidget parent, Consumer<Object> addChild, Consumer<Object> removeChild, Consumer<MoneyValue> changeConsumer) {
        this.parent = parent;
        this.addChild = addChild;
        this.removeChild = removeChild;
        this.changeConsumer = changeConsumer;
    }

    protected final <T> T addChild(T child) {
        if(this.addChild == null)
            return child;
        this.addChild.accept(child);
        this.children.add(child);
        return child;
    }

    protected final void removeChild(Object child) { if(this.removeChild == null) return; this.removeChild.accept(child); this.children.remove(child); }

    public abstract void initialize(ScreenArea widgetArea);

    public void renderTick() {}

    public final void renderBG(EasyGuiGraphics gui) { this.renderBG(this.parent.getArea(), gui, this.parent); }

    @Deprecated(since = "2.3.0.4")
    protected void renderBG(ScreenArea widgetArea, EasyGuiGraphics gui) {}
    protected void renderBG(ScreenArea widgetArea, EasyGuiGraphics gui, MoneyValueWidget parent) { this.renderBG(widgetArea,gui); }

    protected final void changeValue(MoneyValue newValue) { this.changeConsumer.accept(newValue); }

    public abstract void onValueChanged(MoneyValue newValue);

    public final void close()
    {
        if(this.removeChild != null)
        {
            for(Object child : this.children)
                this.removeChild.accept(child);
        }
        this.children.clear();
        this.onClose();
    }

    protected void onClose() { }

}
