package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public interface OptionWidgetBuilder {

    @Nullable
    ScrollingWidgetBuilder createBuilderForOption(Screen screen,ConfigFileOption file,ConfigOption<?> option,Consumer<Object> changeValueConsumer,BooleanSupplier canEdit);

}
