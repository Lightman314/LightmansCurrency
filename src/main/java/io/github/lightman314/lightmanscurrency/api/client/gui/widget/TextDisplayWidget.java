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
    private final Positioning positioning;
    protected TextDisplayWidget(Builder builder) {
        super(builder);
        this.text = builder.text;
        this.color = builder.color;
        this.shadow = builder.shadow;
        this.positioning = builder.positioning;
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        Component t = this.text.get();
        int width = gui.getFont().width(t);
        if(width > area.width)
            gui.scrollingText(t,0,0,area.width,area.height,this.color.get(),this.shadow);
        else {
            int x = this.positioning.getXPos(width,area.pos.x,area.width);
            gui.text(t,x,0,this.color.get(),this.shadow);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) { return false; }

    public static Builder builder() { return new Builder(); }

    public enum Positioning {
        LEFT,CENTER,RIGHT;
        int getXPos(int textWidth,int left,int width) {
            return switch (this) {
                case LEFT -> left;
                case CENTER -> left + (width - textWidth) / 2;
                case RIGHT -> left + width - textWidth;
            };
        }
    }

    public static final class Builder extends AbstractBuilder<Builder,TextDisplayWidget> {

        private Builder() { super(100,10); }

        private Supplier<Component> text = Component::empty;
        private Supplier<Integer> color = () -> 0xFF404040;
        private Positioning positioning = Positioning.LEFT;
        private boolean shadow = false;

        public Builder ofWidth(int width) { this.setWidth(width); return this; }

        public Builder withText(Component text) { return this.withText(() -> text); }
        public Builder withText(TextEntry text) { return this.withText(text::get); }
        public Builder withText(Supplier<Component> text) { this.text = text; return this; }

        public Builder withTextColor(int color) { return this.withTextColor(() -> color); }
        public Builder withTextColor(Supplier<Integer> color) { this.color = color; return this; }

        public Builder withShadow(boolean shadow) { this.shadow = shadow; return this; }

        public Builder withLeftText() { this.positioning = Positioning.LEFT; return this; }
        public Builder withCenteredText() { this.positioning = Positioning.CENTER; return this; }
        public Builder withRightText() { this.positioning = Positioning.RIGHT; return this; }

        public Builder withTextPositioning(Positioning positioning) { this.positioning = positioning; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public TextDisplayWidget build() { return new TextDisplayWidget(this); }
    }

}
