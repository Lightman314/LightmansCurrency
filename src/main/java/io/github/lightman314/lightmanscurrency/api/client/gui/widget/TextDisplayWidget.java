package io.github.lightman314.lightmanscurrency.api.client.gui.widget;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class TextDisplayWidget extends FancyWidget {

    private final Supplier<Component> text;
    private final Supplier<Integer> color;
    private final boolean shadow;
    private final boolean centered;
    protected TextDisplayWidget(Builder builder) {
        super(builder);
        this.text = builder.text;
        this.color = builder.color;
        this.shadow = builder.shadow;
        this.centered = builder.centered;
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        Component t = this.text.get();
        int width = gui.getFont().width(t);
        if(width > area.width)
            gui.scrollingText(t,0,0,area.width,area.height,this.color.get(),this.shadow);
        else {
            if(this.centered)
                gui.centeredText(t,area.halfWidth(),0,this.color.get(),this.shadow);
            else
                gui.text(t,0,0,this.color.get(),this.shadow);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) { return false; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<Builder,TextDisplayWidget> {

        private Builder() { super(100,10); }

        private Supplier<Component> text = Component::empty;
        private Supplier<Integer> color = () -> 0xFF404040;
        private boolean centered = false;
        private boolean shadow = false;

        public Builder ofWidth(int width) { this.setWidth(width); return this; }

        public Builder withText(Component text) { return this.withText(() -> text); }
        public Builder withText(TextEntry text) { return this.withText(text::get); }
        public Builder withText(Supplier<Component> text) { this.text = text; return this; }

        public Builder withTextColor(int color) { return this.withTextColor(() -> color); }
        public Builder withTextColor(Supplier<Integer> color) { this.color = color; return this; }

        public Builder withShadow(boolean shadow) { this.shadow = shadow; return this; }

        public Builder withCenteredText() { this.centered = true; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public TextDisplayWidget build() { return new TextDisplayWidget(this); }
    }

}
