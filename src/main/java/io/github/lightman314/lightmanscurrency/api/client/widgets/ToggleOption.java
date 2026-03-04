package io.github.lightman314.lightmanscurrency.api.client.widgets;

import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.client.widgets.labels.LabelData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ToggleOption extends EasyWidgetWithChildren {

    private final BooleanSupplier currentValue;
    private final Consumer<Boolean> consumer;
    private final LabelData label;
    protected ToggleOption(Builder builder) {
        super(builder);
        this.currentValue = builder.currentValue;
        this.consumer = builder.consumer;
        this.label = builder.label;
    }

    @Override
    public void addChildren(ScreenArea area) {
        this.addChild(PlainButton.builder()
                .position(area.pos)
                .sprite(SpriteUtil.createCheckbox(this.currentValue))
                .addon(EasyAddonHelper.visibleCheck(this::isVisible))
                .addon(EasyAddonHelper.activeCheck(this::isActive))
                .pressAction(() -> this.consumer.accept(!this.currentValue.getAsBoolean()))
                .build());
    }

    @Override
    protected void renderWidget(EasyGuiGraphics gui) {
        int width = this.getWidth() - 11;
        Component text = TextRenderUtil.fitString(this.label.text,width);
        gui.drawString(text,11,1,this.label.textColor,this.label.shadow);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder extends LabelData.LabelBuilder<Builder>
    {

        private BooleanSupplier currentValue = () -> false;
        private Consumer<Boolean> consumer = v -> {};

        private Builder() { super(100,10); }
        @Override
        protected Builder getSelf() { return this; }
        public Builder width(int width) { this.changeWidth(width); return this; }

        public Builder currentValue(BooleanSupplier currentValue) { this.currentValue = currentValue; return this; }
        public Builder handler(Consumer<Boolean> handler) { this.consumer = handler; return this; }
        public Builder handler(Runnable handler) { return this.handler(b -> handler.run()); }

        public ToggleOption build() { return new ToggleOption(this); }

    }

}
