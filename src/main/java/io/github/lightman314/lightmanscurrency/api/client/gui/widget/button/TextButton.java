package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Supplier;

/**
 * Fancy variant of {@link net.minecraft.client.gui.components.Button.Plain Button.Plain}
 */
public class TextButton extends FancyButton {

    private final Supplier<Component> text;

    protected TextButton(Builder builder) { super(builder); this.text = builder.text; }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        gui.blitButtonSprite(0,0,this.width,this.height,this.active,this.isHovered);
        gui.scrollingText(this.getMessage(),2,2,this.width - 4,this.height - 4,this.getFGColor(),true);
    }

    @Override
    public Component getMessage() { return this.text.get(); }

    @Override
    protected MutableComponent createNarrationMessage() {
        return this.text.get().copy();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder extends FlexibleSizeButtonBuilder<Builder,TextButton> {
        private Builder() { super(100,20); }

        private Supplier<Component> text = Component::empty;

        public Builder withText(Component text) { return this.withText(() -> text); }
        public Builder withText(TextEntry text) { return this.withText(text::get); }
        public Builder withText(Supplier<Component> text) { this.text = text; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public TextButton build() { return new TextButton(this); }
    }

}
