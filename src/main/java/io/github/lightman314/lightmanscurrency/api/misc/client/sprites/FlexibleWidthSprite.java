package io.github.lightman314.lightmanscurrency.api.misc.client.sprites;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface FlexibleWidthSprite {

    default void render(EasyGuiGraphics gui, ScreenPosition position, int width) { this.render(gui,position.x,position.y,width); }
    void render(EasyGuiGraphics gui, int x, int y, int width);

    int getHeight();

    default boolean canFitInSize(int width) { return true; }

    default FixedSizeSprite ofSize(int width) { return new FixedSprite(this,width); }

    class FixedSprite implements FixedSizeSprite
    {

        private final FlexibleWidthSprite sprite;
        private final int width;
        private FixedSprite(FlexibleWidthSprite sprite,int width) { this.sprite = sprite; this.width = width; }

        @Override
        public int getWidth() { return this.width; }
        @Override
        public int getHeight() { return this.sprite.getHeight(); }
        @Override
        public void render(EasyGuiGraphics gui, int x, int y) {
            if(this.sprite.canFitInSize(this.width))
                this.sprite.render(gui,x,y,this.width);
        }

    }


}
