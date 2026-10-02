package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.FancyWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TextDisplayWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;

public class SectionLabel implements ScrollingWidgetBuilder {

    private final ConfigFile.ConfigSection section;
    private final ConfigFileOption.DefaultConfigOption file;
    public SectionLabel(ConfigFile.ConfigSection section,ConfigFileOption.DefaultConfigOption file) {
        this.section = section;
        this.file = file;
    }

    @Override
    public int buildWidgets(IPositionalWidgetHolder holder,int width,int yPos) {
        FancyWidget w = holder.addChild(0,yPos + 10,
                TextDisplayWidget.builder()
                        .withTextColor(-1).withShadow(true)
                        .ofWidth(width)
                        .tooltip(TooltipSource.deferredList(() -> this.section.getTooltips(this.file.file)).withAutoWrap())
                        .withText(() -> this.section.getDisplayName(this.file.file))
                        .withCenteredText()
                        .build());
        return w.getHeight() + 10;
    }
}
