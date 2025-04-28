package io.github.lightman314.lightmanscurrency.client.model.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TextureMap {

    public static final TextureMap EMPTY = new TextureMap(ImmutableMap.of());

    private final List<Entry> textures;
    private TextureMap(Map<String,ResourceLocation> data) {
        ImmutableList.Builder<Entry> builder = ImmutableList.builderWithExpectedSize(data.size());
        Function<ResourceLocation,TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        data.forEach((key,texture) -> builder.add(new Entry(key,texture,atlas.apply(texture))));
        this.textures = builder.build();
    }
    public static TextureMap create(Map<String,ResourceLocation> textures) { return new TextureMap(textures); }

    @Nullable
    public String getKey(TextureAtlasSprite sprite) {
        for(Entry e : this.textures)
        {
            if(e.sprite == sprite)
                return e.key;
        }
        return null;
    }

    private record Entry(String key, ResourceLocation textureID, TextureAtlasSprite sprite) { }

}
