package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list;

import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.ListScreenSettings;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IRemovalListener;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ListEditBoxOption extends ListOptionWidget implements IRemovalListener {

    private EditBox textBox;
    private final Function<Consumer<Object>,TextInputUtil.Builder<?>> setupMethod;
    private final Consumer<EditBox> optionChangeHandler;
    protected ListEditBoxOption(Builder builder) {
        super(builder);
        this.setupMethod = builder.setupMethod;
        this.optionChangeHandler = builder.optionChangeHandler;
        this.option.addListener(this::onOptionChanged);
    }

    @Override
    protected void addMoreChildren(ScreenArea area) {
        this.textBox = this.addChildAtRelativePosition(this.setupMethod.apply(this::changeValue)
                .width(HALF_WIDTH - 25)
                .build(),
                ScreenPosition.of(HALF_WIDTH + 5,0));
    }

    @Override
    public void onRemovedFromScreen() { this.option.removeListener(this::onOptionChanged); }

    private void onOptionChanged(ConfigOption<?> option)
    {
        if(this.textBox != null)
            this.optionChangeHandler.accept(this.textBox);
    }

    public static Builder builder(ConfigOption<?> option, int index, ListScreenSettings settings) { return new Builder(option,index,settings); }

    public static class Builder extends ListOptionBuilder<Builder>
    {

        private Function<Consumer<Object>,TextInputUtil.Builder<?>> setupMethod = (c) -> TextInputUtil.stringBuilder();
        private Consumer<EditBox> optionChangeHandler = e -> {};
        private Builder(ConfigOption<?> option, int index, ListScreenSettings settings) { super(option,index,settings); }

        public Builder inputBoxSetup(Function<Consumer<Object>,TextInputUtil.Builder<?>> setupMethod) { this.setupMethod = setupMethod; return this; }
        public Builder optionChangeHandler(Consumer<EditBox> changeHandler) { this.optionChangeHandler = changeHandler; return this; }

        @Override
        protected Builder getSelf() { return this; }

        public ListEditBoxOption build() { return new ListEditBoxOption(this); }

    }

}
