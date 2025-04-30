package io.github.lightman314.lightmanscurrency.client.model.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TextureMap {

    public static final TextureMap EMPTY = new TextureMap(new HashMap<>());

    private final Map<String,ResourceLocation> data;
    private TextureMap(Map<String,ResourceLocation> data) {
        //Don't store the particle here, as it's manually applied in the VariantBlockModel methods
        data.remove("particle");
        this.data = ImmutableMap.copyOf(data);
    }
    public static TextureMap create(Map<String,ResourceLocation> textures) { return new TextureMap(textures); }

    @Nullable
    public String getKey(TextureAtlasSprite sprite) {
        for(Map.Entry<String,ResourceLocation> e : this.data.entrySet())
        {
            if(sprite.contents().name().equals(e.getValue()))
                return e.getKey();
        }
        return null;
    }

    public TextureAtlasSprite createSprite(ResourceLocation texture) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("TextureMap[");
        this.data.forEach((key,value) -> {
            if(builder.length() > 11)
                builder.append(",");
            builder.append(key).append(':').append(value);
        });
        return builder.append("]").toString();
    }
}
