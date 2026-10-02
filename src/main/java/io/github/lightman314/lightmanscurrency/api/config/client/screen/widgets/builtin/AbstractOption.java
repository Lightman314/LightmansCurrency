package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TextDisplayWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public abstract class AbstractOption implements ScrollingWidgetBuilder {

    protected final ConfigOption<?> option;
    protected final Consumer<Object> changeValue;
    protected final BooleanSupplier canEdit;
    protected AbstractOption(AbstractBuilder<?> builder) {
        this.option = builder.option;
        this.changeValue = builder.changeValue;
        this.canEdit = builder.canEdit;
    }

    @Override
    public final int buildWidgets(IPositionalWidgetHolder holder,int width, int yPos) {
        int halfWidth = width / 2;
        holder.addChild(0,yPos + 5,
                TextDisplayWidget.builder()
                        .ofWidth(halfWidth - 5)
                        .withText(this.option.getDisplayName())
                        .withTextColor(-1)
                        .withShadow(true)
                        .withRightText()
                        .tooltip(TooltipSource.deferredList(this.option::getCommentTooltips).withAutoWrap())
                        .build());
        int height = this.buildWidget(holder,halfWidth + 5,yPos);
        //Force a minimum of 20 pixels tall for the label
        return Math.max(height,20);
    }

    protected abstract int buildWidget(IPositionalWidgetHolder holder,int xPos,int yPos);

    protected boolean canEdit() { return this.canEdit.getAsBoolean(); }

    public static abstract class AbstractBuilder<T extends AbstractOption> {

        private final ConfigOption<?> option;
        private final Consumer<Object> changeValue;
        private final BooleanSupplier canEdit;
        protected AbstractBuilder(ConfigOption<?> option, Consumer<Object> changeValue, BooleanSupplier canEdit) {
            this.option = option;
            this.changeValue = changeValue;
            this.canEdit = canEdit;
        }

        public abstract T build();

    }

}
