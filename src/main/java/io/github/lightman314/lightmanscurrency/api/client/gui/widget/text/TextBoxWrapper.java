package io.github.lightman314.lightmanscurrency.api.client.gui.widget.text;

import com.mojang.datafixers.util.Either;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.*;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers.*;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.mixin_helpers.EditBoxAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.function.*;

public final class TextBoxWrapper<T> implements FancyRenderable, IRenderTick, IWidgetWrapper, IKeyboardInterceptor {

    private boolean ignoreChanged = false;
    private final EditBox editBox;
    private final Consumer<T> handler;
    private final Function<String,T> reader;
    private final Function<T,String> writer;
    private final Function<EditBox,Boolean> visible;
    private final Function<EditBox,Boolean> active;

    public boolean isVisible() { return this.editBox.isVisible(); }

    @Override
    public void setVisible(boolean visible) { this.editBox.setVisible(visible); }

    @Override
    public void alreadyRendered() {
        if(this.editBox instanceof EditBoxAccessor m)
            m.lightmanscurrencySetAlreadyRenderered(true);
    }

    public boolean isActive() { return this.editBox.isActive(); }

    private TextBoxWrapper(Builder<T> builder) {
        this.reader = builder.parser;
        this.writer = builder.writer;
        this.handler = builder.handler;
        this.active = builder.active;
        this.visible = builder.visible;
        this.editBox = new EditBox(builder.font,builder.area.x,builder.area.y,builder.area.width,builder.area.height,builder.oldTextBox,builder.message);
        //Set max length *before* setting starting value
        this.editBox.setMaxLength(builder.maxLength);
        //Set starting value
        builder.startingValue.ifLeft(this.editBox::setValue)
                .ifRight(startingValue -> this.editBox.setValue(this.writer.apply(startingValue)));
        //Set color
        if(builder.color != null)
            this.editBox.setTextColor(builder.color);
        this.editBox.setBordered(builder.renderBG);

        this.editBox.setResponder(this::onTextChanged);
    }

    @Override
    public EditBox getWrappedWidget() { return this.editBox; }

    public void setX(int x) { this.editBox.setX(x); }
    public void setY(int y) { this.editBox.setY(y); }
    public void setPosition(int x,int y) { this.editBox.setPosition(x,y); }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> widgetVisitor) { }

    public void setPosition(ScreenPosition position) { this.editBox.setPosition(position.x,position.y); }

    @Override
    public void setScissorArea(@Nullable ScreenArea area) {
        if(this.editBox instanceof EditBoxAccessor a)
            a.lightmanscurrencySetScissorArea(area);
    }

    public int getX() { return this.editBox.getX(); }
    public int getY() { return this.editBox.getY(); }
    public ScreenPosition getPosition() { return ScreenPosition.of(this.getX(),this.getY()); }
    public int getWidth() { return this.editBox.getWidth(); }
    public int getHeight() { return this.editBox.getHeight(); }
    public ScreenArea getArea() { return ScreenArea.of(this.getPosition(),this.getWidth(),this.getHeight()); }

    private void onTextChanged(String text) {
        if(this.ignoreChanged)
            return;
        T newValue = this.reader.apply(text);
        if(newValue != null)
            this.handler.accept(newValue);
    }

    @Override
    public void renderTick(ScreenPosition mousePos) {
        this.editBox.visible = this.visible.apply(this.editBox);
        this.editBox.active = this.active.apply(this.editBox);
        if(this.editBox instanceof EditBoxAccessor a) {
            a.lightmanscurrencySetAlreadyRenderered(false);
            a.lightmanscurrencySetScissorArea(null);
        }
    }

    public void setStringValue(String value) {
        this.ignoreChanged = true;
        this.editBox.setValue(value);
        this.ignoreChanged = false;
    }

    public void setValue(T value) {
        this.ignoreChanged = true;
        this.editBox.setValue(this.writer.apply(value));
        this.ignoreChanged = false;
    }

    @Nullable
    public T getValue() { return this.reader.apply(this.editBox.getValue()); }
    public String getString() { return this.editBox.getValue(); }

    public static <T> Builder<T> builder(Function<String,T> parser) { return new Builder<>(parser); }
    public static <X extends Function<String,T>,T> Builder<T> builder(X parser,Function<X,Function<T,String>> writer) { return builder(parser).writer(writer.apply(parser)); }

    public static Builder<String> stringBuilder() { return new Builder<>(UnaryOperator.identity()); }
    public static Builder<Component> textBuilder() { return new Builder<>(ComponentParser.INSTANCE).writer(ComponentParser::write).withMaxLength(1024); }
    public static Builder<Integer> intBuilder() { return new Builder<>(IntParser.DEFAULT); }
    public static Builder<Long> longBuilder() { return new Builder<>(LongParser.DEFAULT); }
    public static Builder<Float> floatBuilder() { return new Builder<>(FloatParser.DEFAULT).writer(FloatParser.FLOAT_WRITER); }
    public static Builder<Double> doubleBuilder() { return new Builder<>(DoubleParser.DEFAULT).writer(DoubleParser.DEFAULT_WRITER); }
    public static Builder<Identifier> identifierBuilder() { return identifierBuilder(false); }
    public static Builder<Identifier> identifierBuilder(boolean requireNamespace) { return new Builder<>(requireNamespace ? IdentifierParser.DEFAULT : IdentifierParser.REQUIRE_NAMESPACE); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.editBox.extractRenderState(graphics,mouseX,mouseY,a);
    }

    public static final class Builder<T> implements IWidgetBuilder<TextBoxWrapper<T>>
    {
        private Builder(Function<String,T> parser) { this.parser = parser; }

        private Font font = Minecraft.getInstance().font;
        private ScreenArea area = ScreenArea.of(0,0,100,20);
        private Consumer<T> handler = v -> {};
        private Function<String,T> parser;
        private Function<T,String> writer = String::valueOf;
        private Either<String,T> startingValue = Either.left("");
        private int maxLength = 32;
        @Nullable
        private Integer color = null;
        private Component message = Component.empty();
        private boolean renderBG = true;

        private Function<EditBox,Boolean> active = b -> b.active;
        private Function<EditBox,Boolean> visible = b -> b.visible;

        private EditBox oldTextBox = null;

        public Builder<T> withFont(Font font) { this.font = font; return this; }
        public Builder<T> copyValue(@Nullable EditBox box) { if(box != null) this.withStartingString(box.getValue()); return this; }
        public Builder<T> copyValue(@Nullable TextBoxWrapper<T> wrapper) { if(wrapper != null) this.withStartingString(wrapper.getString()); return this; }
        public Builder<T> withStartingString(String value) { this.startingValue = Either.left(value); return this; }
        public Builder<T> withStartingValue(T value) { this.startingValue = Either.right(value); return this; }
        public Builder<T> withMaxLength(int maxLength) { this.maxLength = maxLength; return this; }
        public Builder<T> withTextColor(int textColor) { this.color = textColor; return this; }
        public Builder<T> withMessage(Component message) { this.message = message; return this; }

        public Builder<T> atPos(int x,int y) { this.area = this.area.atPosition(x,y); return this; }
        public Builder<T> atPos(ScreenPosition pos) { this.area = this.area.atPosition(pos); return this; }

        public Builder<T> ofWidth(int width) { this.area = this.area.ofSize(width,this.area.height); return this; }
        public Builder<T> ofHeight(int height) { this.area = this.area.ofSize(this.area.width,height); return this; }
        public Builder<T> ofSize(int width,int height) { this.area = this.area.ofSize(width,height); return this; }
        public Builder<T> ofArea(ScreenArea area) { this.area = area; return this; }

        public Builder<T> withHandler(Consumer<T> handler) { this.handler = handler; return this; }
        public Builder<T> parser(Function<String,T> parser) { this.parser = parser; return this; }
        public Builder<T> writer(Function<T,String> writer) { this.writer = writer; return this; }

        public Builder<T> noBorder() { this.renderBG = false; return this; }

        public Builder<T> active(BooleanSupplier active) { return this.active1(w -> active.getAsBoolean()); }
        public Builder<T> active(UnaryOperator<Boolean> active) { return this.active1(w -> active.apply(w.active)); }
        public Builder<T> active1(Function<EditBox,Boolean> active) { this.active = active; return this; }

        public Builder<T> visible(BooleanSupplier visible) { return this.visible1(w -> visible.getAsBoolean()); }
        public Builder<T> visible(UnaryOperator<Boolean> visible) { return this.visible1(w -> visible.apply(w.visible)); }
        public Builder<T> visible1(Function<EditBox,Boolean> visible) { this.visible = visible; return this; }

        public Builder<T> withOldWidget(@Nullable TextBoxWrapper<T> oldWidget) {
            if(oldWidget != null)
                return this.withOldWidget(oldWidget.editBox);
            return this;
        }
        public Builder<T> withOldWidget(@Nullable EditBox oldWidget) { this.oldTextBox = oldWidget; return this; }

        public TextBoxWrapper<T> build() { return new TextBoxWrapper<>(this); }

        /**
         * Note: When built like this, any {@link #visible} and {@link #active} definitions won't be defined!
         */
        public EditBox buildEditBox() { return this.build().getWrappedWidget(); }

    }

}
