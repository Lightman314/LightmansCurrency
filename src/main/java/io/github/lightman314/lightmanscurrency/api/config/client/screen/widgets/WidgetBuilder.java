package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets;

import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface WidgetBuilder {

    @Nullable
    AbstractWidget createWidgetForOption(Screen screen, ConfigFileOption file, ConfigOption<?> option, Consumer<Object> changeValueConsumer, Supplier<Boolean> canEdit);

}
