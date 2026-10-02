package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IRemovalListener;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.ScreenPositionOption;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ScreenPositionOptionInput extends AbstractOption {

    private final ScreenPositionOption option;
    protected ScreenPositionOptionInput(Builder builder) {
        super(builder);
        this.option = builder.option;
    }

    @Override
    protected int buildWidget(IPositionalWidgetHolder holder,int xPos,int yPos) {
        TextBoxWrapper<Integer> box1 = holder.addChild(xPos,yPos, TextBoxWrapper.intBuilder()
                .withStartingValue(this.option.get().x)
                .ofWidth(70)
                .withHandler(i -> this.onInputChanged(i,true))
                .active(this.canEdit)
                .build());
        TextBoxWrapper<Integer> box2 = holder.addChild(xPos + 80,yPos,TextBoxWrapper.intBuilder()
                .withStartingValue(this.option.get().y)
                .ofWidth(70)
                .withHandler(i -> this.onInputChanged(i,false))
                .active(this.canEdit)
                .build());
        Consumer<ConfigOption<?>> listener = o -> {
            box1.setValue(this.option.get().x);
            box2.setValue(this.option.get().y);
        };
        this.option.addListener(listener);
        holder.addUnpositionedChild(IRemovalListener.simple(() -> this.option.removeListener(listener)));
        return 20;
    }

    private void onInputChanged(int newValue,boolean first) {
        ScreenPosition currentPos = this.option.get();
        ScreenPosition newPos = first ? ScreenPosition.of(newValue,currentPos.y) : ScreenPosition.of(currentPos.x,newValue);
        this.changeValue.accept(newPos);
    }

    public static ScreenPositionOptionInput create(ScreenPositionOption option, Consumer<Object> changeHandler,BooleanSupplier canEdit) {
        return new Builder(option,changeHandler,canEdit).build();
    }

    protected static final class Builder extends AbstractBuilder<ScreenPositionOptionInput> {

        private final ScreenPositionOption option;
        private Builder(ScreenPositionOption option, Consumer<Object> changeValue, BooleanSupplier canEdit) {
            super(option,changeValue,canEdit);
            this.option = option;
        }

        @Override
        public ScreenPositionOptionInput build() { return new ScreenPositionOptionInput(this); }

    }

}
