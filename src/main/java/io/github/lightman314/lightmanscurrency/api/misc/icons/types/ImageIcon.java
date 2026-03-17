package io.github.lightman314.lightmanscurrency.api.misc.icons.types;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.misc.SafeSpriteData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconType;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class ImageIcon extends IconData
{
    public static final IconType<ImageIcon> TYPE = new Type();

    private static final MapCodec<ImageIcon> MAP_CODEC = SafeSpriteData.MAP_CODEC.xmap(ImageIcon::new,i -> i.sprite);
    private static final StreamCodec<ByteBuf,ImageIcon> STREAM_CODEC = SafeSpriteData.STREAM_CODEC.map(ImageIcon::new,i -> i.sprite);

    public final SafeSpriteData sprite;
    private ImageIcon(SafeSpriteData sprite) { this.sprite = sprite; }

    public static IconData ofImage(ResourceLocation image, int u, int v) { return ofImage(new SafeSpriteData(image,u,v,16,16,256,256)); }
    public static IconData ofImage(SafeSpriteData sprite) { return new ImageIcon(sprite); }

    @Override
    public IconType<?> getType() { return TYPE; }

    private static class Type extends IconType<ImageIcon>
    {
        @Override
        public ImageIcon loadOld(CompoundTag tag, HolderLookup.Provider lookup) {
            ResourceLocation image = ResourceLocation.parse(tag.getString("Image"));
            int u = tag.getInt("u");
            int v = tag.getInt("v");
            int w = tag.getInt("w");
            int h = tag.getInt("h");
            int tw = tag.getInt("tw");
            int th = tag.getInt("th");
            return new ImageIcon(new SafeSpriteData(image,u,v,w,h,tw,th));
        }
        @Override
        public ImageIcon parseOld(JsonObject json, HolderLookup.Provider lookup) {
            ResourceLocation image = ResourceLocation.parse(GsonHelper.getAsString(json,"Image"));
            int u = GsonHelper.getAsInt(json,"u");
            int v = GsonHelper.getAsInt(json,"v");
            int w = GsonHelper.getAsInt(json,"w");
            int h = GsonHelper.getAsInt(json,"h");
            int tw = GsonHelper.getAsInt(json,"tw");
            int th = GsonHelper.getAsInt(json,"th");
            return new ImageIcon(new SafeSpriteData(image,u,v,w,h,tw,th));
        }
        @Override
        public MapCodec<ImageIcon> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf,ImageIcon> streamCodec() { return STREAM_CODEC; }

    }

}
