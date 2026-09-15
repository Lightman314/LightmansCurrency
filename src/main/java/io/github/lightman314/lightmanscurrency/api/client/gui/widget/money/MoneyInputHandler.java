package io.github.lightman314.lightmanscurrency.api.client.gui.widget.money;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TextSettings;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown.DropdownOption;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class MoneyInputHandler implements IWidgetHolder {

    public abstract DropdownOption inputOption();
    public abstract MoneyKey getKey();
    public boolean isForValue(MoneyValue value) { return value.getKey().equals(this.getKey()); }
    public void tryMatchValue(MoneyValue value) {}

    protected Font getFont() { return Minecraft.getInstance().font; }

    private MoneyValueWidget parent;
    private IWidgetHolder widgetHolder;
    private Consumer<MoneyValue> changeConsumer;
    private BooleanSupplier visible;
    protected boolean isVisible() { return this.visible != null && this.visible.getAsBoolean(); }
    protected boolean canChangeHandler() { return this.parent == null || this.parent.canChangeHandlers(); }
    protected boolean isLocked() { return this.parent != null && this.parent.isLocked(); }
    protected void onInternalHandlerChanged() { this.parent.markHandlerChanged(); }

    private Supplier<MoneyValue> currentValue = MoneyValue::empty;
    protected MoneyValue currentValue() { return this.currentValue.get(); }
    protected boolean isEmpty() { return this.currentValue().isEmpty(); }
    protected boolean isFree() { return this.currentValue().isFree(); }

    public final void setup(MoneyValueWidget parent,Consumer<MoneyValue> changeConsumer,@Nullable MoneyInputHandler lastHandler) {
        this.parent = parent;
        this.internalSetup(parent::isVisible,parent,parent::getCurrentValue,changeConsumer);
        if(lastHandler != null)
            this.copyHandlerState(lastHandler);
    }

    //Internal setup method to allow Price Input's to still function when wrapping Money Input Handlers
    protected final void internalSetup(BooleanSupplier visible,IWidgetHolder widgetHolder,Supplier<MoneyValue> currentValue, Consumer<MoneyValue> changeConsumer) {
        this.visible = visible;
        this.widgetHolder = widgetHolder;
        this.currentValue = currentValue;
        this.changeConsumer = changeConsumer;
    }

    protected void copyHandlerState(MoneyInputHandler oldHandler) { }

    private final List<Object> children = new ArrayList<>();

    @Override
    public final <T> T addChild(T child) {
        if(this.widgetHolder != null)
            this.widgetHolder.addChild(child);
        this.children.add(child);
        return child;
    }

    @Override
    public final void removeChild(Object child) {
        if(this.widgetHolder != null)
            this.widgetHolder.removeChild(child);
        this.children.remove(child);
    }

    @Override
    public final void removeAllChildren() {
        for(Object child : new ArrayList<>(this.children))
            this.removeChild(child);
        this.children.clear();
    }

    public abstract void initialize(ScreenArea area);

    public void renderTick(ScreenPosition mousePos) {}

    public final void extractBG(FancyGuiExtractor gui,ScreenArea area) {
        if(this.parent != null)
            this.extractBG(gui,area,this.parent.getTextSettings());
    }

    protected abstract void extractBG(FancyGuiExtractor gui,ScreenArea area,TextSettings parent);

    protected final void changeValue(MoneyValue newValue) { this.changeConsumer.accept(newValue); }

    public abstract void onValueChanged(MoneyValue newValue);

    public final void close() {
        this.removeAllChildren();
        this.onClose();
    }

    protected void onClose() {}

}