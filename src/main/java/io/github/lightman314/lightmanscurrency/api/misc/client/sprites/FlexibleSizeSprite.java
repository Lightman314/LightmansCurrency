package io.github.lightman314.lightmanscurrency.api.misc.client.sprites;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface FlexibleSizeSprite {

    default void render(EasyGuiGraphics gui, ScreenArea area) { this.render(gui,area.pos,area.width,area.height); }
    default void render(EasyGuiGraphics gui, ScreenPosition pos, int width, int height) { this.render(gui,pos.x,pos.y,width,height); }
    void render(EasyGuiGraphics gui, int x, int y, int width, int height);

    default boolean canFitInSize(int width,int height) { return true; }

    default FixedSizeSprite ofSize(int width,int height) { return new FixedSprite(this,width,height); }

    class FixedSprite implements FixedSizeSprite
    {

        private final FlexibleSizeSprite sprite;
        private final int width;
        private final int height;
        private FixedSprite(FlexibleSizeSprite sprite,int width,int height) { this.sprite = sprite; this.width = width; this.height = height; }

        @Override
        public int getWidth() { return this.width; }
        @Override
        public int getHeight() { return this.height; }
        @Override
        public void render(EasyGuiGraphics gui, int x, int y) {
            if(this.sprite.canFitInSize(this.width,this.height))
                this.sprite.render(gui,x,y,this.width,this.height);
        }
    }

}