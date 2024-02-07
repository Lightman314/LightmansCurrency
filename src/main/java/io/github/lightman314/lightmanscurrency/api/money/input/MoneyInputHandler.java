package io.github.lightman314.lightmanscurrency.api.money.input;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public abstract class MoneyInputHandler {

    @Nonnull
    public abstract MutableComponent inputName();
    @Nonnull
    public abstract String getUniqueName();

    private MoneyValueWidget parent;
    protected boolean isVisible() { return this.parent.isVisible(); }
    protected boolean isLocked() { return this.parent.isLocked(); }
    protected Font getFont() { return this.parent.getFont(); }
    @Nonnull
    protected MoneyValue currentValue() { return this.parent.getCurrentValue(); }
    protected boolean isEmpty() { return this.currentValue().isEmpty(); }
    protected boolean isFree() { return this.currentValue().isFree(); }
    private Consumer<Object> addChild;
    private Consumer<Object> removeChild;
    private Consumer<MoneyValue> changeConsumer;

    private final List<Object> children = new ArrayList<>();

    public final void setup(@Nonnull MoneyValueWidget parent, @Nonnull Consumer<Object> addChild, @Nonnull Consumer<Object> removeChild, @Nonnull Consumer<MoneyValue> changeConsumer) {
        this.parent = parent;
        this.addChild = addChild;
        this.removeChild = removeChild;
        this.changeConsumer = changeConsumer;
    }

    protected final <T> T addChild(@Nonnull T child) {
        if(this.addChild == null)
            return child;
        this.addChild.accept(child);
        this.children.add(child);
        return child;
    }

    protected final void removeChild(@Nonnull Object child) { if(this.removeChild == null) return; this.removeChild.accept(child); this.children.remove(child); }

    public abstract void initialize(@Nonnull ScreenArea widgetArea);

    public void renderTick() {}

    public final void renderBG(@Nonnull EasyGuiGraphics gui) { this.renderBG(this.parent.getArea(), gui); }

    protected void renderBG(@Nonnull ScreenArea widgetArea, @Nonnull EasyGuiGraphics gui) {}

    protected final void changeValue(@Nonnull MoneyValue newValue) { this.changeConsumer.accept(newValue); }

    public abstract void onValueChanged(@Nonnull MoneyValue newValue);

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
