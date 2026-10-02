package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.ListScreenSettings;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ListButtonOption extends AbstractListOption {

    private final Supplier<Component> optionText;
    private final BiConsumer<Boolean,Consumer<Object>> clickHandler;
    private final TooltipSource tooltip;
    protected ListButtonOption(Builder builder) {
        super(builder);
        this.optionText = builder.optionText;
        this.clickHandler = builder.clickInteraction;
        this.tooltip = builder.tooltip;
    }

    @Override
    protected int buildWidget(IPositionalWidgetHolder holder, int xPos, int yPos) {
        holder.addChild(xPos,yPos,
                TextButton.builder()
                        .ofWidth(125)
                        .withText(this.optionText)
                        .onPress(() -> this.clickHandler.accept(true,this::changeValue))
                        .onAltPress(() -> this.clickHandler.accept(false,this::changeValue))
                        .active(this.settings::canEdit)
                        .tooltip(this.tooltip)
                        .build());
        return 20;
    }

    public static Builder builder(ConfigOption<?> option,int index,ListScreenSettings settings) { return new Builder(option,index,settings); }

    public static class Builder extends AbstractBuilder<ListButtonOption> {

        protected Builder(ConfigOption<?> option, int index, ListScreenSettings settings) { super(option, index, settings); }

        private Supplier<Component> optionText = LCText.Config.CONFIG_OPTION_NOT_SUPPORTED::get;
        private BiConsumer<Boolean,Consumer<Object>> clickInteraction = (left,handler) -> {};
        private TooltipSource tooltip = TooltipSource.EMPTY;

        public Builder buttonText(Component optionText) { return this.buttonText(() -> optionText); }
        public Builder buttonText(TextEntry optionText) { return this.buttonText(optionText::get); }
        public Builder buttonText(Supplier<Component> optionText) { this.optionText = optionText; return this; }

        public Builder clickHandler(Runnable handler) { return this.clickHandler((l, c) -> handler.run()); }
        public Builder clickHandler1(Consumer<Boolean> handler) { return this.clickHandler((l, c) -> handler.accept(l)); }
        public Builder clickHandler2(Consumer<Consumer<Object>> handler) { return this.clickHandler((l, c) -> handler.accept(c)); }
        public Builder clickHandler(BiConsumer<Boolean,Consumer<Object>> handler) { this.clickInteraction = handler; return this; }

        public Builder openScreen(Function<Consumer<Object>, Screen> screenBuilder) {
            return this.tooltip(TooltipSource.simple(LCText.Config.CONFIG_OPTION_EDIT_TOOLTIP))
                    .clickHandler2(handler -> {
                        Minecraft mc = Minecraft.getInstance();
                        Screen newScreen = screenBuilder.apply(handler);
                        if(newScreen != null)
                            mc.setScreen(newScreen);
                    });
        }

        public Builder tooltip(TooltipSource tooltip) { this.tooltip = tooltip; return this; }

        @Override
        public ListButtonOption build() { return new ListButtonOption(this); }

    }

}
