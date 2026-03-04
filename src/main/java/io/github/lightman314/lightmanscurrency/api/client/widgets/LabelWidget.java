package io.github.lightman314.lightmanscurrency.api.client.widgets;

import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.client.widgets.labels.LabelData;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;

public class LabelWidget extends EasyWidget {

    private final LabelData label;
    protected LabelWidget(Builder builder) {
        super(builder);
        this.label = builder.label;
    }

    @Override
    protected void renderWidget(EasyGuiGraphics gui) {
        TextRenderUtil.drawCenteredText(gui,this.label.text,this.getArea().centerX(),0,this.label.textColor,this.label.shadow);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder extends LabelData.LabelBuilder<Builder>
    {
        private Builder() { super(100,10); }

        @Override
        protected Builder getSelf() { return this; }
        public Builder width(int width) { this.changeWidth(width); return this; }

        public LabelWidget build() { return new LabelWidget(this); }

    }

}
