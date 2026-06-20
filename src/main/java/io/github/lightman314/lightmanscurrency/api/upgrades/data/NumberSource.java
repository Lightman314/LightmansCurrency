package io.github.lightman314.lightmanscurrency.api.upgrades.data;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public abstract class NumberSource {

    public static final Codec<NumberSource> CODEC = Codec.withAlternative(
            LCRegistries.Upgrades.NUMBER_SOURCE.byNameCodec()
                .dispatch(NumberSource::getType,NumberSourceType::codec),
            Codec.DOUBLE.xmap(NumberSource::parseSimple,NumberSource::writeSimple));
    public static final StreamCodec<RegistryFriendlyByteBuf,NumberSource> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.Upgrades.NUMBER_SOURCE_KEY).dispatch(NumberSource::getType,NumberSourceType::streamCodec);

    public abstract NumberSourceType<?> getType();

    public abstract double get();
    public float getFloat() {
        double value = this.get();
        if(value > Float.MAX_VALUE)
            return Float.MAX_VALUE;
        if(value < Float.MAX_VALUE * -1)
            return Float.MAX_VALUE * -1;
        return (float)value;
    }
    public int getInt() { return Ints.checkedCast(this.getLong()); }
    public long getLong() { return Math.round(this.get()); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof NumberSource s)
            return this.equals(s);
        return false;
    }
    protected abstract boolean equals(NumberSource source);

    @Override
    public abstract int hashCode();

    private static NumberSource parseSimple(double value) { return new DirectNumberSource(value); }
    private static double writeSimple(NumberSource source) { return source.get(); }

}