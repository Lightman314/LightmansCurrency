package io.github.lightman314.lightmanscurrency.client.util.text_inputs;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IWidgetWrapper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TextBoxWrapper<T> extends EasyWidget implements IWidgetWrapper {

    private final EditBox editBox;
    private final Function<String,T> reader;
    private final Function<T,String> writer;
    protected TextBoxWrapper(Builder<T> builder)
    {
        super(builder);
        this.editBox = builder.box;
        this.reader = builder.reader;
        this.writer = builder.writer;
    }

    @Override
    public EditBox getWrappedWidget() { return this.editBox; }

    public void setValue(T value) { this.editBox.setValue(this.writer.apply(value)); }

    public static <T> Builder<T> builder(EditBox box,Function<String,T> reader,Function<T,String> writer) { return new Builder<>(box,reader,writer); }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) { return false; }

    @Override
    protected void renderWidget(EasyGuiGraphics gui) { }

    @Override
    protected void renderTick() {
        this.editBox.visible = this.isVisible();
        this.editBox.active = this.active;
    }

    public T getValue() { return this.reader.apply(this.editBox.getValue()); }

    public static final class Builder<T> extends EasyBuilder<Builder<T>>
    {
        private final EditBox box;
        private final Function<String,T> reader;
        private final Function<T,String> writer;
        private Builder(EditBox box, Function<String,T> reader,Function<T, String> writer) {
            this.box = box;
            this.reader = reader;
            this.writer = writer;
            this.position(this.box.getX(),this.box.getY());
            this.changeSize(this.box.getWidth(),this.box.getHeight());
        }

        @Override
        protected Builder<T> getSelf() { return this; }

        public TextBoxWrapper<T> build() { return new TextBoxWrapper<>(this); }

    }

}