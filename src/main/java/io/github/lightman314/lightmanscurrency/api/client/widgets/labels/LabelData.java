package io.github.lightman314.lightmanscurrency.api.client.widgets.labels;

import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.network.chat.Component;

public class LabelData {

    public static LabelData empty() { return new LabelData(Component.empty()); }

    public Component text;
    public int textColor = 0x404040;
    public boolean shadow = false;
    public LabelData(Component label) { this.text = label; }

    public static abstract class LabelBuilder<T extends LabelBuilder<T>> extends EasyWidget.EasyBuilder<T>
    {
        public final LabelData label;
        protected LabelBuilder() { this(Component.empty()); }
        protected LabelBuilder(int width,int height) { this(width,height,Component.empty()); }
        protected LabelBuilder(Component label) { this.label = new LabelData(label); }
        protected LabelBuilder(int width, int height, Component label) {
            super(width,height);
            this.label = new LabelData(label);
        }

        public T label(Component text) { this.label.text = text; return this.getSelf(); }
        public T label(TextEntry text) { return this.label(text.get()); }
        public T drawShadow() { return this.drawShadow(true); }
        public T drawShadow(boolean drawShadow) { this.label.shadow = drawShadow; return this.getSelf(); }
        public T textColor(int color) { this.label.textColor = color; return this.getSelf(); }

    }

}
