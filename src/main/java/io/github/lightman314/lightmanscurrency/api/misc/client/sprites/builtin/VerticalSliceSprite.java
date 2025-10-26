package io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FlexibleHeightSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VerticalSliceSprite implements FlexibleHeightSprite {

    public final SpriteSource image;
    private final int edge;
    public VerticalSliceSprite(SpriteSource image, int edge) { this.image = image; this.edge = edge; }

    @Override
    public void render(EasyGuiGraphics gui, int x, int y, int height) {
        int vCenter = this.image.height() - edge - edge;
        if(vCenter < 0)
            return;
        //Left edge
        gui.blit(this.image.texture(),x,y,this.image.u(),this.image.v(),this.image.width(),this.edge,this.image.textureWidth(),this.image.textureHeight());
        //Center
        int tempY = edge;
        while(tempY < height - edge)
        {
            int heightToDraw = Math.min(vCenter,height - edge - tempY);
            gui.blit(this.image.texture(),x, y + tempY, this.image.u(),this.image.v() + this.edge,this.image.width(),heightToDraw,this.image.textureWidth(),this.image.textureHeight());
            tempY += heightToDraw;
        }
        //Right edge
        gui.blit(this.image.texture(),x, y + height - edge, this.image.u(), this.image.v() + this.image.height() - this.edge, this.image.width(),this.edge,this.image.textureWidth(),this.image.textureHeight());
    }

    @Override
    public int getWidth() { return this.image.width(); }

}
