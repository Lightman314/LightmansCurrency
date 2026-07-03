package io.github.lightman314.lightmanscurrency.api.client.gui.sprites;

import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.Identifier;

public class WidgetContextSprite implements SizedSprite.WithContext {

    private final WidgetSprites sprites;
    private final int width;
    private final int height;
    private boolean active = true;
    private boolean hovered = false;
    public WidgetContextSprite(WidgetSprites sprites,int width,int height) {
        this.sprites = sprites;
        this.width = width;
        this.height = height;
    }

    @Override
    public Identifier sprite() { return this.sprites.get(this.active,this.hovered); }

    @Override
    public void defineContext(boolean active,boolean hovered) {
        this.active = active;
        this.hovered = hovered;
    }

    @Override
    public int width() { return this.width; }
    @Override
    public int height() { return this.height; }

    public static SizedSprite.Builder hoverToggleSprite(Identifier sprite,int width,int height) {
        return hoverToggleSprite(sprite,sprite.withSuffix("_hovered"),width,height);
    }
    public static SizedSprite.Builder hoverToggleSprite(Identifier normalSprite,Identifier hoveredSprite,int width,int height) {
        return widgetSprite(new WidgetSprites(normalSprite,normalSprite,hoveredSprite,hoveredSprite),width,height);
    }

    public static SizedSprite.Builder widgetSprite(WidgetSprites sprites, int width, int height) {
        return () -> new WidgetContextSprite(sprites,width,height);
    }

}
