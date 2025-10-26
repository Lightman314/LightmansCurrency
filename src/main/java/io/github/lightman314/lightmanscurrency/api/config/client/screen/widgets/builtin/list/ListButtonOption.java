package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.ListScreenSettings;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ListButtonOption extends ListOptionWidget {

    private final Supplier<Component> optionText;
    private final BiConsumer<Boolean,Consumer<Object>> clickHandler;
    private final Supplier<Boolean> active;
    protected ListButtonOption(Builder builder) {
        super(builder);
        this.optionText = builder.optionText;
        this.clickHandler = builder.clickInteraction;
        this.active = builder.active;
    }

    @Override
    protected void addMoreChildren(ScreenArea area) {
        this.addChildAtRelativePosition(EasyTextButton.builder()
                .width(HALF_WIDTH - 25)
                .text(this.optionText)
                .pressAction(() -> this.clickHandler.accept(true,this::changeValue))
                .altPressAction(() -> this.clickHandler.accept(false,this::changeValue))
                .addon(EasyAddonHelper.activeCheck(this.active))
                .build(),
                ScreenPosition.of(HALF_WIDTH + 5,0));
    }

    public static Builder builder(ConfigOption<?> option, int index, ListScreenSettings settings) { return new Builder(option,index,settings); }

    public static class Builder extends ListOptionBuilder<Builder>
    {

        private Supplier<Component> optionText = LCText.CONFIG_OPTION_NOT_SUPPORTED::get;
        private BiConsumer<Boolean,Consumer<Object>> clickInteraction = (left,handler) -> {};
        private Supplier<Boolean> active = () -> true;
        private Builder(ConfigOption<?> option, int index, ListScreenSettings settings) { super(option,index,settings); }

        public Builder buttonText(Component optionText) { this.optionText = () -> optionText; return this; }
        public Builder buttonText(TextEntry optionText) { this.optionText = optionText::get; return this; }
        public Builder buttonText(Supplier<Component> optionText) { this.optionText = optionText; return this; }
        public Builder clickHandler(Runnable handler) { this.clickInteraction = (left,consumer) -> handler.run(); return this; }
        public Builder clickHandler1(Consumer<Boolean> handler) { this.clickInteraction = (left,consumer) -> handler.accept(left); return this; }
        public Builder clickHandler2(Consumer<Consumer<Object>> handler) { this.clickInteraction = (left,consumer) -> handler.accept(consumer); return this; }
        public Builder clickHandler(BiConsumer<Boolean,Consumer<Object>> handler) { this.clickInteraction = handler; return this; }

        public Builder openScreen(Function<Consumer<Object>,Screen> screenBuilder)
        {
            return this.addon(EasyAddonHelper.tooltip(LCText.CONFIG_OPTION_EDIT_TOOLTIP))
                    .clickHandler2(handler -> {
                        Minecraft mc = Minecraft.getInstance();
                        Screen newScreen = screenBuilder.apply(handler);
                        if(newScreen != null)
                            mc.setScreen(newScreen);
                    });
        }

        private Builder active(Supplier<Boolean> active) { this.active = active; return this; }

        @Override
        protected Builder getSelf() { return this; }

        public ListButtonOption build() { return new ListButtonOption(this); }


    }

}
