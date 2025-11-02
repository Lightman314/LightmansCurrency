package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.ListOptionScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.ListScreenSettings;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.ListLikeOption;
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
import java.util.function.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SimpleButtonOption extends OptionWidget {

    private final Supplier<Component> optionText;
    private final BiConsumer<Boolean,Consumer<Object>> clickHandler;
    protected SimpleButtonOption(Builder builder) {
        super(builder);
        this.optionText = builder.optionText;
        this.clickHandler = builder.clickInteraction;
    }

    @Override
    public void addChildren(ScreenArea area) {
        this.addChildAtRelativePosition(EasyTextButton.builder()
                        .width(HALF_WIDTH - 5)
                        .text(this.optionText)
                        .pressAction(() -> this.clickHandler.accept(true,this::changeValue))
                        .altPressAction(() -> this.clickHandler.accept(false,this::changeValue))
                        .addon(EasyAddonHelper.activeCheck(this::isActive))
                        .build(),
                ScreenPosition.of(HALF_WIDTH + 5,0));
    }


    public static Builder builder(ConfigOption<?> option, Consumer<Object> changeValue, Supplier<Boolean> canEdit) { return new Builder(option,changeValue,canEdit); }

    public static SimpleButtonOption createForList(ListLikeOption<?> option, Consumer<Object> changeValue, Screen screen, ConfigFileOption file, Function<Consumer<Object>, ListScreenSettings> settingsBuilder)
    {
        return builder(option,changeValue,() -> true)
                .buttonText(() -> LCText.CONFIG_OPTION_LIST_COUNT.get(option.getSize()))
                .openScreen(handler -> new ListOptionScreen(screen,file,option,settingsBuilder.apply(handler)))
                .build();
    }

    public static SimpleButtonOption createUndefined(ConfigOption<?> option) { return builder(option,o -> {},() -> false).build(); }

    public static class Builder extends OptionBuilder<Builder>
    {

        private Supplier<Component> optionText = LCText.CONFIG_OPTION_NOT_SUPPORTED::get;
        private BiConsumer<Boolean,Consumer<Object>> clickInteraction = (left,handler) -> {};
        private Builder(ConfigOption<?> option, Consumer<Object> changeValue, Supplier<Boolean> canEdit) { super(option,changeValue,canEdit); }

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

        @Override
        protected Builder getSelf() { return this; }

        public SimpleButtonOption build() { return new SimpleButtonOption(this); }


    }

}