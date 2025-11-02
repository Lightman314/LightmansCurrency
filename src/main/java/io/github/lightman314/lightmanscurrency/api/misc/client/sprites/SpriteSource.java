package io.github.lightman314.lightmanscurrency.api.misc.client.sprites;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record SpriteSource(ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight) {

    public SpriteSource(ResourceLocation sprite, int u, int v, int width, int height) { this(sprite,u,v,width,height,256,256); }

    public static ResourceLocation modifyTexture(ResourceLocation texture) { return texture.withPrefix("textures/gui/").withSuffix(".png"); }

    public static SpriteSource create(ResourceLocation texture,int width,int height) { return new SpriteSource(modifyTexture(texture),0,0,width,height,width,height); }
    public static SpriteSource createTop(ResourceLocation texture, int width,int height) { return new SpriteSource(modifyTexture(texture),0,0,width,height,width,height * 2); }
    public static SpriteSource createBottom(ResourceLocation texture, int width,int height) { return new SpriteSource(modifyTexture(texture),0,height,width,height,width,height * 2); }

}