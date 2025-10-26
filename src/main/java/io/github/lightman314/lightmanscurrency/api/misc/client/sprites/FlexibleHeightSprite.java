package io.github.lightman314.lightmanscurrency.api.misc.client.sprites;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface FlexibleHeightSprite {

    default void render(EasyGuiGraphics gui, ScreenPosition position, int height) { this.render(gui,position.x,position.y,height); }
    void render(EasyGuiGraphics gui, int x, int y, int height);

    int getWidth();

    default boolean canFitInSize(int height) { return true; }

    default FixedSizeSprite ofSize(int height) { return new FixedSprite(this,height); }

    class FixedSprite implements FixedSizeSprite
    {

        private final FlexibleHeightSprite sprite;
        private final int height;
        private FixedSprite(FlexibleHeightSprite sprite, int height) { this.sprite = sprite; this.height = height; }

        @Override
        public int getWidth() { return this.sprite.getWidth(); }
        @Override
        public int getHeight() { return this.height; }
        @Override
        public void render(EasyGuiGraphics gui, int x, int y) {
            if(this.sprite.canFitInSize(this.height))
                this.sprite.render(gui,x,y,this.height);
        }
        
    }


}
