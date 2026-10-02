package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TextDisplayWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.ListScreenSettings;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.text.LCText;

public abstract class AbstractListOption implements ScrollingWidgetBuilder {

    protected final ConfigOption<?> option;
    protected final int index;
    protected final ListScreenSettings settings;
    protected AbstractListOption(AbstractBuilder<?> builder) {
        this.option = builder.option;
        this.index = builder.index;
        this.settings = builder.settings;
    }

    @Override
    public final int buildWidgets(IPositionalWidgetHolder holder, int width, int yPos) {
        int halfWidth = width / 2;
        holder.addChild(0,yPos + 5,
                TextDisplayWidget.builder()
                        .ofWidth(halfWidth - 5)
                        .withText(LCText.Config.CONFIG_OPTION_LIST_ENTRY.get(this.index + 1))
                        .withTextColor(-1).withShadow(true)
                        .withRightText()
                        .tooltip(TooltipSource.deferredList(this.option::getCommentTooltips).withAutoWrap())
                        .build());
        holder.addChild(halfWidth + 130,yPos, IconButton.builder()
                .withIcon(SpriteIcon.of(LCApi.id("icon/sign_x")))
                .onPress(this::removeValue)
                .tooltip(TooltipSource.simple(LCText.Config.CONFIG_OPTION_LIST_REMOVE))
                .visible(this.settings::canRemoveEntry)
                .build());
        int height = this.buildWidget(holder,halfWidth + 5,yPos);
        //Force a minimum of 20 pixels tall for the label
        return Math.max(height,20);
    }

    protected abstract int buildWidget(IPositionalWidgetHolder holder,int xPos,int yPos);

    protected final void changeValue(Object newValue) {
        this.settings.setEntry(this.index,newValue);
    }
    protected final void removeValue() {
        if(this.settings.canRemoveEntry())
            this.settings.removeEntry(this.index);
    }

    public static abstract class AbstractBuilder<T extends AbstractListOption> {

        private final ConfigOption<?> option;
        private final int index;
        private final ListScreenSettings settings;
        protected AbstractBuilder(ConfigOption<?> option,int index,ListScreenSettings settings) {
            this.option = option;
            this.index = index;
            this.settings = settings;
        }

        public abstract T build();
    }

}
