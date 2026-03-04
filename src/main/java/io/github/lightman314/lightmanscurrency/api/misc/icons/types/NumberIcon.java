package io.github.lightman314.lightmanscurrency.api.misc.icons.types;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconType;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.GsonHelper;

public class NumberIcon extends IconData
{
    public static final IconType<NumberIcon> TYPE = new Type();

    private static final MapCodec<NumberIcon> MAP_CODEC = Codec.INT.fieldOf("number").xmap(NumberIcon::new,n -> n.number);
    private static final StreamCodec<ByteBuf,NumberIcon> STREAM_CODEC = ByteBufCodecs.INT.map(NumberIcon::new,n -> n.number);

    public final int number;
    private NumberIcon(int number) { this.number = number; }

    public static IconData ofNumber(int number) { return new NumberIcon(number); }

    @Override
    public IconType<?> getType() { return TYPE; }

    private static NumberIcon loadNumber(CompoundTag tag) { return new NumberIcon(tag.getInt("Number")); }

    private static NumberIcon parseNumber(JsonObject json) { return new NumberIcon(GsonHelper.getAsInt(json,"Number")); }

    private static class Type extends IconType<NumberIcon>
    {
        @Override
        public NumberIcon loadOld(CompoundTag tag, HolderLookup.Provider lookup) { return loadNumber(tag); }
        @Override
        public NumberIcon parseOld(JsonObject json, HolderLookup.Provider lookup) { return parseNumber(json); }
        @Override
        public MapCodec<NumberIcon> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, NumberIcon> streamCodec() { return STREAM_CODEC; }
    }

}
