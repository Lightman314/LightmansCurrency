package io.github.lightman314.lightmanscurrency.api.misc.icons.types;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public class MultiIcon extends IconData
{

    public static final IconType<MultiIcon> TYPE = new Type();

    private static final MapCodec<MultiIcon> MAP_CODEC = IconData.CODEC.listOf().fieldOf("children").xmap(MultiIcon::new,i -> i.icons);
    private static final StreamCodec<RegistryFriendlyByteBuf,MultiIcon> STREAM_CODEC = IconData.STREAM_CODEC.apply(ByteBufCodecs.list()).map(MultiIcon::new,i -> i.icons);

    public final List<IconData> icons;
    private MultiIcon(List<IconData> icons) { this.icons = icons; }

    public static IconData ofMultiple(IconData... icons) { return ofMultiple(ImmutableList.copyOf(icons)); }
    public static IconData ofMultiple(List<IconData> list) { return new MultiIcon(ImmutableList.copyOf(list)); }

    @Override
    public IconType<?> getType() { return TYPE; }

    @SuppressWarnings("deprecation")
    private static MultiIcon loadMulti(CompoundTag tag, HolderLookup.Provider lookup) {
        List<IconData> result = new ArrayList<>();
        ListTag children = tag.getList("Children", Tag.TAG_COMPOUND);
        for(int i = 0; i < children.size(); ++i)
        {
            IconData icon = loadOldData(children.getCompound(i),lookup);
            if(icon != null)
                result.add(icon);
        }
        return new MultiIcon(result);
    }

    @SuppressWarnings("deprecation")
    private static MultiIcon parseMulti(JsonObject json, HolderLookup.Provider lookup) {
        List<IconData> result = new ArrayList<>();
        JsonArray children = GsonHelper.getAsJsonArray(json,"Children");
        for(int i = 0; i < children.size(); ++i)
            result.add(parseOld(GsonHelper.convertToJsonObject(children.get(i),"Children[" + i + "]"),lookup));
        return new MultiIcon(result);
    }

    private static class Type extends IconType<MultiIcon>
    {
        @Override
        public MultiIcon loadOld(CompoundTag tag, HolderLookup.Provider lookup) { return loadMulti(tag,lookup); }
        @Override
        public MultiIcon parseOld(JsonObject json, HolderLookup.Provider lookup) { return parseMulti(json,lookup); }
        @Override
        public MapCodec<MultiIcon> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, MultiIcon> streamCodec() { return STREAM_CODEC; }
    }


}