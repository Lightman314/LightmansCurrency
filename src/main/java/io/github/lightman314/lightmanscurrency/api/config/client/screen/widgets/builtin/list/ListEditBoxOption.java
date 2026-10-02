package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IRemovalListener;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers.DoubleParser;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers.FloatParser;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.ListScreenSettings;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ListEditBoxOption extends AbstractListOption {

    private final Function<Consumer<Object>,TextBoxWrapper.Builder<?>> setupMethod;
    private final Consumer<TextBoxWrapper<?>> optionChangeHandler;

    protected ListEditBoxOption(Builder builder) {
        super(builder);
        this.setupMethod = builder.setupMethod;
        this.optionChangeHandler = builder.optionChangeHandler;
    }

    @Override
    protected int buildWidget(IPositionalWidgetHolder holder, int xPos, int yPos) {
        TextBoxWrapper<?> wrapper = this.setupMethod.apply(this::changeValue)
                .ofWidth(125)
                .active(this.settings::canEdit)
                .build();
        holder.addChild(xPos,yPos,wrapper);
        Consumer<ConfigOption<?>> listener = o -> this.optionChangeHandler.accept(wrapper);
        this.option.addListener(listener);
        holder.addUnpositionedChild(IRemovalListener.simple(() -> this.option.removeListener(listener)));
        return 20;
    }

    public static Builder builder(ConfigOption<?> option, int index, ListScreenSettings settings) { return new Builder(option,index,settings); }

    public static final class Builder extends AbstractBuilder<ListEditBoxOption> {

        private Builder(ConfigOption<?> option, int index, ListScreenSettings settings) { super(option, index, settings); }

        private Function<Consumer<Object>,TextBoxWrapper.Builder<?>> setupMethod = h -> TextBoxWrapper.stringBuilder();
        private Consumer<TextBoxWrapper<?>> optionChangeHandler = e -> {};

        public Builder inputBoxSetup(Supplier<TextBoxWrapper.Builder<?>> setupMethod) { return this.inputBoxSetup(handler -> setupMethod.get().withHandler(handler::accept)); }
        public Builder inputBoxSetup(Function<Consumer<Object>,TextBoxWrapper.Builder<?>> setupMethod) { this.setupMethod = setupMethod; return this; }
        public Builder optionChangeHandler(Consumer<TextBoxWrapper<?>> changeHandler) { this.optionChangeHandler = changeHandler; return this; }
        public Builder numberChangeHandler(Supplier<Number> number) { return this.optionChangeHandler(
                text -> {
                Number n = number.get();
                if(n.doubleValue() == 0 && n.longValue() == 0 & text.getString().isEmpty())
                    return;
                if(n instanceof Double d)
                    text.setStringValue(DoubleParser.DEFAULT_WRITER.apply(d));
                else if(n instanceof Float f)
                    text.setStringValue(FloatParser.FLOAT_WRITER.apply(f));
                else
                    text.setStringValue(String.valueOf(n));
            });
        }

        @Override
        public ListEditBoxOption build() { return new ListEditBoxOption(this); }

    }

}
