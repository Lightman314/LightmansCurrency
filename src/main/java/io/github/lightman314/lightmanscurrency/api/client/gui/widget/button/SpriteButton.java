package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.ScreenHelper;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.IFancyScreen;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SimpleSizedSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SizedSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.FancyWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.ILateRenderer;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.function.Function;

public class SpriteButton extends FancyButton {

    private final SizedSprite sprite;
    private final Function<FancyWidget,Integer> color;
    protected SpriteButton(Builder builder) {
        super(builder);
        this.sprite = builder.sprite;
        this.color = builder.color;
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui,ScreenArea area) {
        gui.blitSprite(this.sprite,0,0,this.color.apply(this),this.isActive(),this.isHovered());
    }

    private static class LateRendering extends SpriteButton implements ILateRenderer {
        protected LateRendering(Builder builder) { super(builder); }
        @Override
        protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) { }
        @Override
        public void extractLateRender(FancyGuiExtractor gui) {
            if(!this.visible)
                return;
            boolean isVanillaScreen = !(Minecraft.getInstance().screen instanceof IFancyScreen);
            ScreenPosition offset = ScreenPosition.ZERO;
            if(isVanillaScreen && Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> s) {
                //Offset by the inverse of the screens corner to offset the vanilla pose stack push
                offset = ScreenHelper.getScreenCorner(s).invert();
            }
            gui.push(this.getPosition().offset(offset));
            super.extractRenderState(gui,this.getArea());
            gui.pop();
        }
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder extends ButtonBuilder<Builder,SpriteButton> {

        private Builder() { super(20,20); }

        private SizedSprite sprite = new SimpleSizedSprite(LCApi.id("null"),20,20);
        private Function<FancyWidget,Integer> color = AbstractWidget::getFGColor;
        private boolean renderLate = false;

        @Override
        protected Builder getSelf() { return this; }

        public Builder withSprite(SizedSprite sprite) {
            this.sprite = sprite;
            this.setSize(this.sprite.width(),this.sprite.height());
            return this;
        }
        public Builder withSprite(SizedSprite.Template<Void> template) { return this.withSprite(template.buildSprite(null)); }
        public <T> Builder withSprite(SizedSprite.Template<T> template,T argument) { return this.withSprite(template.buildSprite(argument)); }

        public Builder withSpriteColor(int color) { return this.withSpriteColor(w -> color); }
        public Builder withSpriteColor(Function<FancyWidget,Integer> color) { this.color = color; return this; }
        public Builder withSpriteColor(int activeColor,int disabledColor) { return this.withSpriteColor(w -> w.isActive() ? activeColor : disabledColor); }

        public Builder renderOnTop() { this.renderLate = true; return this; }

        @Override
        public SpriteButton build() { return this.renderLate ? new LateRendering(this) : new SpriteButton(this); }

    }

}
