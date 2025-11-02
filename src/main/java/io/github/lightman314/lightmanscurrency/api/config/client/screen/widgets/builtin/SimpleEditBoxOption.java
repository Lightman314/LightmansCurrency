package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IRemovalListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SimpleEditBoxOption extends OptionWidget implements IRemovalListener {

    private TextBoxWrapper<?> textBox;
    private final Function<Consumer<Object>,TextInputUtil.Builder<?>> setupMethod;
    private final Consumer<EditBox> optionChangeHandler;
    protected SimpleEditBoxOption(Builder builder) {
        super(builder);
        this.setupMethod = builder.setupMethod;
        this.optionChangeHandler = builder.optionChangeHandler;
        this.option.addListener(this::onOptionChanged);
    }

    @Override
    public void addChildren(ScreenArea area) {
        this.textBox = this.addChildAtRelativePosition(this.setupMethod.apply(this::changeValue)
                        .width(HALF_WIDTH - 5)
                        .wrap()
                        .addon(EasyAddonHelper.activeCheck(this::isActive))
                        .build(),
                ScreenPosition.of(HALF_WIDTH + 5,0)
        );
    }

    @Override
    public void onRemovedFromScreen() {
        this.option.removeListener(this::onOptionChanged);
    }

    private void onOptionChanged(ConfigOption<?> option)
    {
        if(this.textBox != null)
            this.optionChangeHandler.accept(this.textBox.getWrappedWidget());
    }

    public static Builder builder(ConfigOption<?> option, Consumer<Object> changeHandler, Supplier<Boolean> canEdit) { return new Builder(option,changeHandler,canEdit); }

    public static class Builder extends OptionBuilder<Builder>
    {

        private Function<Consumer<Object>,TextInputUtil.Builder<?>> setupMethod = (c) -> TextInputUtil.stringBuilder();
        private Consumer<EditBox> optionChangeHandler = e -> {};
        private Builder(ConfigOption<?> option, Consumer<Object> changeHandler, Supplier<Boolean> canEdit) { super(option,changeHandler,canEdit); }

        public Builder inputBoxSetup(Function<Consumer<Object>,TextInputUtil.Builder<?>> setupMethod) { this.setupMethod = setupMethod; return this; }
        public Builder optionChangeHandler(Consumer<EditBox> changeHandler) { this.optionChangeHandler = changeHandler; return this; }

        @Override
        protected Builder getSelf() { return this; }

        public SimpleEditBoxOption build() { return new SimpleEditBoxOption(this); }

    }

}