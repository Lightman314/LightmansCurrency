package io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FlexibleSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class NineSliceSprite implements FlexibleSizeSprite {

    public final SpriteSource image;
    private final int edge;
    public NineSliceSprite(SpriteSource image, int edge) { this.image = image; this.edge = edge; }

    @Override
    public boolean canFitInSize(int width, int height) {
        return width >= this.edge * 2 && height >= this.edge * 2;
    }

    @Override
    public void render(EasyGuiGraphics gui, int x, int y, int width, int height)
    {
        int uCenter = this.image.width() - this.edge - this.edge;
        if(uCenter < 1)
            return;
        int vCenter = this.image.height() - this.edge - this.edge;
        if(vCenter < 1)
            return;

        //Top-left corner
        gui.blit(this.image.texture(), x, y, this.image.u(), this.image.v() , this.edge, this.edge,this.image.textureWidth(),this.image.textureHeight());
        //Top this.edge
        int tempX = this.edge;
        while(tempX < width - this.edge)
        {
            int widthToDraw = Math.min(uCenter, width - this.edge - tempX);
            gui.blit(this.image.texture(), x + tempX, y, this.image.u() + this.edge, this.image.v() , widthToDraw, this.edge,this.image.textureWidth(),this.image.textureHeight());
            tempX += widthToDraw;
        }
        //Top-right corner
        gui.blit(this.image.texture(), x + width - this.edge, y, this.image.u() + this.image.width() - this.edge, this.image.v() , this.edge, this.edge,this.image.textureWidth(),this.image.textureHeight());

        //Draw center
        int tempY = this.edge;
        while(tempY < height - this.edge)
        {
            int heightToDraw = Math.min(vCenter, height - this.edge - tempY);
            //Left center
            gui.blit(this.image.texture(), x, y + tempY, this.image.u(), this.image.v()  + this.edge, this.edge, heightToDraw,this.image.textureWidth(),this.image.textureHeight());
            tempX = this.edge;
            //Center
            while(tempX < width - this.edge)
            {
                int widthToDraw = Math.min(uCenter, width - this.edge - tempX);
                gui.blit(this.image.texture(), x + tempX, y + tempY, this.image.u() + this.edge, this.image.v()  + this.edge, widthToDraw, heightToDraw,this.image.textureWidth(),this.image.textureHeight());
                tempX += widthToDraw;
            }
            //Right center
            gui.blit(this.image.texture(), x + width - this.edge, y + tempY, this.image.u() + this.image.width() - this.edge, this.image.v()  + this.edge, this.edge, heightToDraw,this.image.textureWidth(),this.image.textureHeight());
            tempY += heightToDraw;
        }

        //Bottom-left corner
        gui.blit(this.image.texture(), x, y + height - this.edge, this.image.u(), this.image.v()  + this.image.height() - this.edge, this.edge, this.edge,this.image.textureWidth(),this.image.textureHeight());
        //Bottom this.edge
        tempX = this.edge;
        while(tempX < width - this.edge)
        {
            int widthToDraw = Math.min(uCenter, width - this.edge - tempX);
            gui.blit(this.image.texture(), x + tempX, y + height - this.edge, this.image.u() + this.edge, this.image.v()  + this.image.height() - this.edge, widthToDraw, this.edge,this.image.textureWidth(),this.image.textureHeight());
            tempX += widthToDraw;
        }
        //Bottom-right corner
        gui.blit(this.image.texture(), x + width - this.edge, y + height - this.edge, this.image.u() + this.image.width() - this.edge, this.image.v()  + this.image.height() - this.edge, this.edge, this.edge,this.image.textureWidth(),this.image.textureHeight());
    }

}