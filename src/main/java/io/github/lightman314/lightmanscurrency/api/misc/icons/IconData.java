package io.github.lightman314.lightmanscurrency.api.misc.icons;

import java.util.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;

public abstract class IconData {

    public static final IconType<IconData> NULL_TYPE = new NullIcon.Type();

    public static final Codec<IconData> CODEC = Codec.withAlternative(
            //Desired Codec
            LCRegistries.ICON_TYPE.byNameCodec().dispatch(IconData::getType,IconType::codec),
            //Fallback Codec for old data
            CodecHelper.oldValueLoader(IconData::loadOldData,"Icon Data"));

    public static final StreamCodec<RegistryFriendlyByteBuf,IconData> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.ICON_TYPE_KEY).dispatch(IconData::getType,IconType::streamCodec);
    public static final StreamCodec<RegistryFriendlyByteBuf,NonNullList<IconData>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity));

	private static IconData NULL = null;
	public static IconData Null() {
		if(NULL == null)
			NULL = new NullIcon();
		return NULL;
	}

	@Nullable
    @Deprecated
	public static IconData loadOldData(CompoundTag tag, HolderLookup.Provider lookup)
	{
        if(tag.contains("type"))
            return CODEC.decode(RegistryOps.create(NbtOps.INSTANCE,lookup),tag).getOrThrow().getFirst();
		if(tag.contains("Type"))
		{
			ResourceLocation type = VersionUtil.parseResource(tag.getString("Type"));
            IconType<?> t = LCRegistries.ICON_TYPE.get(type);
			if(t != null)
				return t.loadOld(tag,lookup);
		}
		return null;
	}
    public static IconData safeLoad(CompoundTag tag, HolderLookup.Provider lookup, IconData defaultIcon) { return Objects.requireNonNullElse(loadOldData(tag,lookup),defaultIcon); }

    @Deprecated
    protected static IconData parseOld(JsonObject json, HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException
    {
        if(json.has("type"))
            return CODEC.decode(RegistryOps.create(JsonOps.INSTANCE,lookup),json).getOrThrow().getFirst();
        ResourceLocation type = VersionUtil.parseResource(GsonHelper.getAsString(json,"Type"));
        IconType<?> t = LCRegistries.ICON_TYPE.get(type);
        if(t != null)
            return t.parseOld(json,lookup);
        throw new JsonSyntaxException("Unknown icon type " + type);
    }

	public final boolean isNull() { return this instanceof NullIcon; }
	protected IconData() {}

    public abstract IconType<?> getType();

	public final CompoundTag save(HolderLookup.Provider lookup)  { return (CompoundTag)CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE,lookup),this).getOrThrow(); }

    public final JsonObject write(HolderLookup.Provider lookup)  { return (JsonObject) CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE,lookup),this).getOrThrow(); }

	private static class NullIcon extends IconData {

        private NullIcon() { super(); }
        @Override
        public IconType<?> getType() { return NULL_TYPE; }

        private static class Type extends IconType<IconData>
        {
            private static final MapCodec<IconData> CODEC = MapCodec.unit(IconData::Null);
            private static final StreamCodec<ByteBuf,IconData> STREAM_CODEC = StreamCodec.of((b,v) -> {},b -> Null());

            @Override
            public IconData loadOld(CompoundTag tag, HolderLookup.Provider lookup) { return Null(); }
            @Override
            public IconData parseOld(JsonObject tag, HolderLookup.Provider lookup) { return Null(); }
            @Override
            public MapCodec<IconData> codec() { return CODEC; }
            @Override
            public StreamCodec<ByteBuf,IconData> streamCodec() { return STREAM_CODEC; }
        }

	}

}