package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SimpleSizedSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SizedSprite;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;

public class SpriteButton extends FancyButton {

    private final SizedSprite sprite;
    protected SpriteButton(Builder builder) {
        super(builder);
        this.sprite = builder.sprite;
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui,ScreenArea area) {
        //Update the sprites context if necessary
        if(this.sprite instanceof SizedSprite.WithContext contextSprite)
            contextSprite.defineContext(this.isActive(),this.isHoveredOrFocused());
        gui.blitSprite(this.sprite,0,0);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder extends ButtonBuilder<Builder,SpriteButton> {

        private SizedSprite sprite = new SimpleSizedSprite(LCApi.id("null"),20,20);

        private Builder() { super(20,20); }

        @Override
        protected Builder getSelf() { return this; }

        public Builder withSprite(SizedSprite sprite) {
            this.sprite = sprite;
            this.setSize(this.sprite.width(),this.sprite.height());
            return this;
        }
        public Builder withSprite(SizedSprite.Template<Void> template) { return this.withSprite(template.buildSprite(null)); }
        public <T> Builder withSprite(SizedSprite.Template<T> template,T argument) { return this.withSprite(template.buildSprite(argument)); }

        @Override
        public SpriteButton build() { return new SpriteButton(this); }
    }

}
