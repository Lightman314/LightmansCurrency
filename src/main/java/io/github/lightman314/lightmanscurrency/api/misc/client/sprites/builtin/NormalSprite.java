package io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NormalSprite implements FixedSizeSprite {

    public final SpriteSource image;
    public NormalSprite(SpriteSource image) { this.image = image; }


    @Override
    public int getWidth() { return this.image.width(); }
    @Override
    public int getHeight() { return this.image.height(); }

    @Override
    public void render(EasyGuiGraphics gui, int x, int y) {
        gui.blit(this.image.texture(),x,y,this.image.u(),this.image.v(),this.image.width(),this.image.height(),this.image.textureWidth(),this.image.textureHeight());
    }

}
