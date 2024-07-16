package io.github.lightman314.lightmanscurrency.client.gui.easy.rendering;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class Sprite {

    public final ResourceLocation image;
    public final int u;
    public final int v;
    public final int width;
    public final int height;
    public final int hoverOffsetU;
    public final int hoverOffsetV;
    public int getU(boolean hovered) { return hovered ? this.u + this.hoverOffsetU : this.u; }
    public int getV(boolean hovered) { return hovered ? this.v + this.hoverOffsetV : this.v; }

    public Sprite(@Nonnull ResourceLocation image, int u, int v, int width, int height, int hoverOffsetU, int hoverOffsetV) {
        this.image = image;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
        this.hoverOffsetU = hoverOffsetU;
        this.hoverOffsetV = hoverOffsetV;
    }

    public static Sprite LockedSprite(@Nonnull ResourceLocation image, int u, int v, int width, int height) { return new Sprite(image, u, v, width, height, 0, 0); }
    public static Sprite SimpleSprite(@Nonnull ResourceLocation image, int u, int v, int width, int height) { return new Sprite(image, u, v, width, height, 0, height); }
    public static Sprite SimpleSpriteH(@Nonnull ResourceLocation image, int u, int v, int width, int height) { return new Sprite(image, u, v, width, height, width, 0); }


}
