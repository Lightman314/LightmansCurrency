package io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FlexibleWidthSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HorizontalSliceSprite implements FlexibleWidthSprite {

    public final SpriteSource image;
    private final int edge;
    public HorizontalSliceSprite(SpriteSource image, int edge) { this.image = image; this.edge = edge; }

    @Override
    public void render(EasyGuiGraphics gui, int x, int y, int width) {
        int uCenter = this.image.width() - edge - edge;
        if(uCenter < 0)
            return;
        //Left edge
        gui.blit(this.image.texture(),x,y,this.image.u(),this.image.v(),this.edge,this.image.height(),this.image.textureWidth(),this.image.textureHeight());
        //Center
        int tempX = edge;
        while(tempX < width - edge)
        {
            int widthToDraw = Math.min(uCenter,width - edge - tempX);
            gui.blit(this.image.texture(),x + tempX, y, this.image.u() + this.edge,this.image.v(),widthToDraw,this.image.height(),this.image.textureWidth(),this.image.textureHeight());
            tempX += widthToDraw;
        }
        //Right edge
        gui.blit(this.image.texture(),x + width - edge, y, this.image.u() + this.image.width() - this.edge, this.image.v(), this.edge,this.image.height(),this.image.textureWidth(),this.image.textureHeight());
    }

    @Override
    public int getHeight() { return this.image.height(); }

}