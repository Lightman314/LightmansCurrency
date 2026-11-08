package io.github.lightman314.lightmanscurrency.integration.jei;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SizedSprite;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public record SizedSpriteDrawable(SizedSprite sprite) implements IDrawable {

    @Override
    public int getWidth() { return this.sprite.width(); }
    @Override
    public int getHeight() { return this.sprite.height(); }
    @Override
    public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
        FancyGuiExtractor gui = new FancyGuiExtractor(guiGraphics,0f);
        gui.blitSprite(this.sprite,xOffset,yOffset);
    }

}
