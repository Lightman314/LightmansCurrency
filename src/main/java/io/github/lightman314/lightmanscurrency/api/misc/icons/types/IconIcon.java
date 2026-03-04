package io.github.lightman314.lightmanscurrency.api.misc.icons.types;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconType;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class IconIcon extends IconData {

    public static final IconType<IconIcon> TYPE = new Type();

    private static final MapCodec<IconIcon> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("location").forGetter(i -> i.location)
    ).apply(builder,IconIcon::new));

    private static final StreamCodec<ByteBuf,IconIcon> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(IconIcon::new,i -> i.location);

    public final ResourceLocation location;
    private IconIcon(ResourceLocation location) { this.location = location; }

    public static IconData ofIcon(ResourceLocation location) { return new IconIcon(location.withPrefix("textures/gui/icons/").withSuffix(".png")); }

    @Override
    public IconType<?> getType() { return TYPE; }

    private static class Type extends IconType<IconIcon>
    {
        @Override
        public IconIcon loadOld(CompoundTag tag, HolderLookup.Provider lookup) { return new IconIcon(VersionUtil.parseResource(tag.getString("location"))); }
        @Override
        public IconIcon parseOld(JsonObject json, HolderLookup.Provider lookup) { return new IconIcon(VersionUtil.parseResource(GsonHelper.getAsString(json,"location"))); }
        @Override
        public MapCodec<IconIcon> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<ByteBuf,IconIcon> streamCodec() { return STREAM_CODEC; }
    }

}
