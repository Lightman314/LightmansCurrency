package io.github.lightman314.lightmanscurrency.client.util.text_inputs;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IWidgetWrapper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TextBoxWrapper<T> extends EasyWidget implements IWidgetWrapper {

    private boolean ignoreChanges = false;
    private final EditBox editBox;
    private final Function<String,T> reader;
    private final Function<T,String> writer;
    protected TextBoxWrapper(Builder<T> builder)
    {
        super(builder);
        this.editBox = builder.box;
        this.reader = builder.reader;
        this.writer = builder.writer;
        //Setup change consumer if we want to wrap it
        if(builder.wrapConsumer && builder.handler != null)
            this.editBox.setResponder(wrappedResponder(TextInputUtil.stringResponder(builder.handler,this.reader),this));
    }

    private static Consumer<String> wrappedResponder(Consumer<String> original,TextBoxWrapper<?> wrapper)
    {
        return s -> { if(!wrapper.ignoreChanges) original.accept(s); };
    }

    @Override
    public EditBox getWrappedWidget() { return this.editBox; }

    public void setStringValue(String value) {
        this.ignoreChanges = true;
        this.editBox.setValue(value);
        this.ignoreChanges = false;
    }
    public void setValue(T value) {
        this.ignoreChanges = true;
        this.editBox.setValue(this.writer.apply(value));
        this.ignoreChanges = false;
    }

    public static <T> Builder<T> builder(EditBox box,Consumer<T> handler, Function<String,T> reader, Function<T,String> writer) { return new Builder<>(box,handler,reader,writer); }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) { return false; }

    @Override
    protected void renderWidget(EasyGuiGraphics gui) { }

    @Override
    protected void renderTick() {
        this.editBox.visible = this.isVisible();
        this.editBox.active = this.active;
        this.editBox.setPosition(this.getX(),this.getY());
    }

    @Nullable
    public T getValue() { return this.reader.apply(this.editBox.getValue()); }

    public String getString() { return this.editBox.getValue(); }

    public static final class Builder<T> extends EasyBuilder<Builder<T>>
    {
        private boolean wrapConsumer = true;
        private final EditBox box;
        private final Consumer<T> handler;
        private final Function<String,T> reader;
        private final Function<T,String> writer;
        private Builder(EditBox box, Consumer<T> handler, Function<String,T> reader,Function<T, String> writer) {
            this.box = box;
            this.handler = handler;
            this.reader = reader;
            this.writer = writer;
            this.position(this.box.getX(),this.box.getY());
            this.changeSize(this.box.getWidth(),this.box.getHeight());
        }

        public Builder<T> handleWhenSet() { this.wrapConsumer = false; return this; }

        @Override
        protected Builder<T> getSelf() { return this; }

        public TextBoxWrapper<T> build() { return new TextBoxWrapper<>(this); }

    }

}
