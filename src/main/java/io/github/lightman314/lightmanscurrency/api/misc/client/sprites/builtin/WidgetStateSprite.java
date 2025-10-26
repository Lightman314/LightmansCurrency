package io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FlexibleSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.IWidgetContextSprite;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WidgetStateSprite implements FixedSizeSprite, IWidgetContextSprite {

    private final FixedSizeSprite activeSprite;
    private final FixedSizeSprite activeFocusedSprite;
    private final FixedSizeSprite inactiveSprite;
    private final FixedSizeSprite inactiveFocusedSprite;

    private Pair<Boolean,Boolean> lastState = null;

    private WidgetStateSprite(Builder builder)
    {
        this.activeSprite = builder.activeSprite;
        this.activeFocusedSprite = builder.activeFocusedSprite != null ? builder.activeFocusedSprite : this.activeSprite;
        this.inactiveSprite = builder.inactiveSprite;
        this.inactiveFocusedSprite = builder.inactiveFocusedSprite != null ? builder.inactiveFocusedSprite : this.inactiveSprite;
    }

    public static WidgetStateSprite lazyHoverable(FixedSizeSprite normal, FixedSizeSprite hovered) { return builder(normal).withActiveFocus(hovered).build(); }
    public static WidgetStateSprite lazyHoverable(ResourceLocation texture, int width, int height)
    {
        FixedSizeSprite normal = new NormalSprite(SpriteSource.createTop(texture,width,height));
        FixedSizeSprite hovered = new NormalSprite(SpriteSource.createBottom(texture,width,height));
        return lazyHoverable(normal,hovered);
    }

    public static WidgetStateSprite lazyHoverable(FlexibleSizeSprite normal, FlexibleSizeSprite hovered, int width, int height)
    {
        FixedSizeSprite n = normal.ofSize(width,height);
        FixedSizeSprite h = hovered.ofSize(width,height);
        return lazyActive(n,h);
    }

    public static WidgetStateSprite lazyActive(FixedSizeSprite active, FixedSizeSprite inactive) { return builder(active).withInactive(inactive).build(); }
    public static WidgetStateSprite lazyActive(ResourceLocation texture, int width, int height)
    {
        FixedSizeSprite active = new NormalSprite(SpriteSource.createTop(texture,width,height));
        FixedSizeSprite inactive = new NormalSprite(SpriteSource.createBottom(texture,width,height));
        return lazyActive(active,inactive);
    }

    @Override
    public int getWidth() { return this.activeSprite.getWidth(); }

    @Override
    public int getHeight() { return this.activeSprite.getHeight(); }

    @Override
    public void render(EasyGuiGraphics gui, int x, int y) { this.getCurrentSprite().render(gui,x,y); }

    private FixedSizeSprite getCurrentSprite()
    {
        if(this.lastState == null)
            return this.activeSprite;
        boolean active = this.lastState.getFirst();
        boolean focused = this.lastState.getSecond();
        //Clear the last state as this should be updated every frame
        this.lastState = null;
        return this.getSprite(active,focused);
    }

    @Override
    public FixedSizeSprite getSprite(boolean active,boolean focused)
    {
        //Get the relevant texture
        if(active)
            return focused ? this.activeFocusedSprite : this.activeSprite;
        else
            return focused ? this.inactiveFocusedSprite : this.inactiveSprite;
    }

    @Override
    public void updateWidgetContext(AbstractWidget widget) { this.lastState = Pair.of(widget.isActive(),widget.isHovered()); }

    public static Builder builder(FixedSizeSprite activeSprite) { return new Builder(activeSprite,activeSprite); }

    public static class Builder
    {
        private final FixedSizeSprite activeSprite;
        private FixedSizeSprite activeFocusedSprite = null;
        private FixedSizeSprite inactiveSprite;
        private FixedSizeSprite inactiveFocusedSprite = null;
        private Builder(FixedSizeSprite activeSprite,FixedSizeSprite inactiveSprite) { this.activeSprite = this.inactiveSprite = activeSprite; }

        private FixedSizeSprite checkSprite(FixedSizeSprite sprite) {
            if(sprite.getWidth() != this.activeSprite.getWidth() || sprite.getHeight() != this.activeSprite.getHeight())
                throw new IllegalArgumentException("Sprite must be the same size as the active size texture! " + debugSpriteSize(this.activeSprite,"Active Sprite") + " " + debugSpriteSize(sprite,"Builder Sprite"));
            return sprite;
        }

        private static String debugSpriteSize(FixedSizeSprite sprite,String name) { return name + ": " + sprite.getWidth() + "," + sprite.getHeight(); }

        public Builder withActiveFocus(FixedSizeSprite activeFocusedSprite) { this.activeFocusedSprite = this.checkSprite(activeFocusedSprite); return this; }
        public Builder withInactive(FixedSizeSprite inactiveSprite) { this.inactiveSprite = this.checkSprite(inactiveSprite); return this; }
        public Builder withInactiveFocus(FixedSizeSprite inactiveFocusedSprite) { this.inactiveFocusedSprite = this.checkSprite(inactiveFocusedSprite); return this; }

        public WidgetStateSprite build() { return new WidgetStateSprite(this); }

    }

}
