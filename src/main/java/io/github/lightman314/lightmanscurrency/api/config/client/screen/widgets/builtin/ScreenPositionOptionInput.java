package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.ScreenPositionOption;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IRemovalListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ScreenPositionOptionInput extends OptionWidget implements IRemovalListener {

    private TextBoxWrapper<Integer> textBox1;
    private TextBoxWrapper<Integer> textBox2;
    private final ScreenPositionOption posOption;
    protected ScreenPositionOptionInput(Builder builder) {
        super(builder);
        this.posOption = builder.option;
        this.option.addListener(this::onOptionChanged);
    }

    private boolean ignoreInputs = false;

    @Override
    public void addChildren(ScreenArea area) {
        this.textBox1 = this.addChildAtRelativePosition(TextInputUtil.intBuilder()
                        .startingValue(this.posOption.get().x)
                        .width((HALF_WIDTH/2) - 5)
                        .handler(i -> this.onInputChanged(i,true))
                        .wrap()
                        .addon(EasyAddonHelper.activeCheck(this::isActive))
                        .build(),
                ScreenPosition.of(HALF_WIDTH + 5, 0));
        this.textBox2 = this.addChildAtRelativePosition(TextInputUtil.intBuilder()
                        .startingValue(this.posOption.get().y)
                        .width((HALF_WIDTH/2) - 5)
                        .handler(i -> this.onInputChanged(i,false))
                        .wrap()
                        .addon(EasyAddonHelper.activeCheck(this::isActive))
                        .build(),
                ScreenPosition.of(HALF_WIDTH + 10 + (HALF_WIDTH/2), 0));
    }

    @Override
    public void onRemovedFromScreen() {
        this.option.removeListener(this::onOptionChanged);
    }

    private void onInputChanged(int newValue,boolean firstInput)
    {
        if(this.ignoreInputs)
            return;
        ScreenPosition currentPos = this.posOption.get();
        ScreenPosition newPos = firstInput ? ScreenPosition.of(newValue,currentPos.y) : ScreenPosition.of(currentPos.x,newValue);
        this.changeValue(newPos);
    }

    private void onOptionChanged(ConfigOption<?> option)
    {
        if(this.textBox1 != null && this.textBox2 != null && option == this.posOption)
        {
            this.ignoreInputs = true;
            ScreenPosition newPos = this.posOption.get();
            this.textBox1.setValue(newPos.x);
            this.textBox2.setValue(newPos.y);
            this.ignoreInputs = false;
        }
    }

    public static ScreenPositionOptionInput create(ScreenPositionOption option, Consumer<Object> changeHandler, Supplier<Boolean> canEdit)
    {
        return new Builder(option,changeHandler,canEdit).build();
    }

    public static class Builder extends OptionBuilder<Builder>
    {

        private final ScreenPositionOption option;
        private Builder(ScreenPositionOption option, Consumer<Object> changeHandler, Supplier<Boolean> canEdit) { super(option,changeHandler,canEdit); this.option = option; }

        @Override
        protected Builder getSelf() { return this; }

        public ScreenPositionOptionInput build() { return new ScreenPositionOptionInput(this); }

    }

}