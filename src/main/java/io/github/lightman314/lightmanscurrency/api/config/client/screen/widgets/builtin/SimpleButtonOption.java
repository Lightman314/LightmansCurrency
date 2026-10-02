package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.ListOptionScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.ListScreenSettings;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.ListLikeOption;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.*;

public class SimpleButtonOption extends AbstractOption {

    private final Supplier<Component> optionText;
    private final BiConsumer<Boolean, Consumer<Object>> clickHandler;
    private final TooltipSource tooltip;
    private SimpleButtonOption(Builder builder) {
        super(builder);
        this.optionText = builder.optionText;
        this.clickHandler = builder.clickInteraction;
        this.tooltip = builder.tooltip;
    }

    @Override
    public int buildWidget(IPositionalWidgetHolder holder,int xPos,int yPos) {
        holder.addChild(xPos,yPos,
                TextButton.builder()
                        .ofWidth(145)
                        .withText(this.optionText)
                        .onPress(() -> this.clickHandler.accept(true,this.changeValue))
                        .onAltPress(() -> this.clickHandler.accept(false,this.changeValue))
                        .active(this.canEdit)
                        .tooltip(this.tooltip)
                        .build());
        return 20;
    }

    public static Builder builder(ConfigOption<?> option, Consumer<Object> changeValue, BooleanSupplier canEdit) { return new Builder(option,changeValue,canEdit); }

    public static SimpleButtonOption createForList(ListLikeOption<?> option, Consumer<Object> changeValue, Screen screen,ConfigFileOption file,Function<Consumer<Object>,ListScreenSettings> settingsBuilder) {
        return SimpleButtonOption.builder(option,changeValue,() -> true)
                .buttonText(() -> LCText.Config.CONFIG_OPTION_LIST_COUNT.get(option.getSize()))
                .openScreen(handler -> new ListOptionScreen(screen,file,option,settingsBuilder.apply(handler)))
                .build();
    }//*/

    public static SimpleButtonOption createUndefined(ConfigOption<?> option) { return builder(option,o -> {},() -> false).build(); }

    public static final class Builder extends AbstractBuilder<SimpleButtonOption> {

        private Builder(ConfigOption<?> option,Consumer<Object> changeValue,BooleanSupplier canEdit) {
            super(option,changeValue,canEdit);
        }

        private Supplier<Component> optionText = LCText.Config.CONFIG_OPTION_NOT_SUPPORTED::get;
        private BiConsumer<Boolean,Consumer<Object>> clickInteraction = (left,handler) -> {};
        private TooltipSource tooltip = TooltipSource.EMPTY;

        public Builder buttonText(Component optionText) { return this.buttonText(() -> optionText); }
        public Builder buttonText(TextEntry optionText) { return this.buttonText(optionText::get); }
        public Builder buttonText(Supplier<Component> optionText) { this.optionText = optionText; return this; }

        public Builder clickHandler(Runnable handler) { return this.clickHandler((l,c) -> handler.run()); }
        public Builder clickHandler1(Consumer<Boolean> handler) { return this.clickHandler((l,c) -> handler.accept(l)); }
        public Builder clickHandler2(Consumer<Consumer<Object>> handler) { return this.clickHandler((l,c) -> handler.accept(c)); }
        public Builder clickHandler(BiConsumer<Boolean,Consumer<Object>> handler) { this.clickInteraction = handler; return this; }

        public Builder tooltip(TooltipSource tooltip) { this.tooltip = tooltip; return this; }

        public Builder openScreen(Function<Consumer<Object>,Screen> screenBuilder) {
            return this.tooltip(TooltipSource.simple(LCText.Config.CONFIG_OPTION_EDIT_TOOLTIP))
                    .clickHandler2(handler -> {
                        Minecraft mc = Minecraft.getInstance();
                        Screen newScreen = screenBuilder.apply(handler);
                        if(newScreen != null)
                            mc.setScreen(newScreen);
                    });
        }

        @Override
        public SimpleButtonOption build() { return new SimpleButtonOption(this); }

    }
}

