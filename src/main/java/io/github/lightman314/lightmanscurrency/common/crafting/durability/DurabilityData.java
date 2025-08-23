package io.github.lightman314.lightmanscurrency.common.crafting.durability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;

public class DurabilityData {

    public static final Codec<DurabilityData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.BOOL.optionalFieldOf("allowInfinite").forGetter(DurabilityData::aiOptional),
                    ExtraCodecs.intRange(0,Integer.MAX_VALUE).fieldOf("min").forGetter(d -> d.min),
                    ExtraCodecs.intRange(0,Integer.MAX_VALUE).fieldOf("max").forGetter(d -> d.max))
                    .apply(builder,DurabilityData::new));
    public static final Codec<DurabilityData> VALID_CODEC = CODEC.validate(data -> {
        if(!data.isValid())
            return DataResult.error(data::getFailMessage);
        return DataResult.success(data);
    });
    public static final StreamCodec<FriendlyByteBuf,DurabilityData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL,d -> d.allowInfinite,ByteBufCodecs.INT,d -> d.min,ByteBufCodecs.INT,d -> d.max,DurabilityData::new);

    public static DurabilityData NULL = new DurabilityData(false,0,0);

    public final boolean allowInfinite;
    private Optional<Boolean> aiOptional() {
        if(this.min == 0)
            return Optional.empty();
        return Optional.of(this.allowInfinite);
    }
    public final int min;
    public final int max;
    private DurabilityData(Optional<Boolean> allowInfinite,int min, int max) { this(allowInfinite.orElse(false),min,max); }
    public DurabilityData(boolean allowInfinite,int min, int max) { this.allowInfinite = allowInfinite; this.min = min; this.max = max; }

    public boolean isValid() { return this.min < this.max && this.max > 0 && this.min >= 0; }

    public boolean test(int durability) { return !this.isValid() || (this.allowInfinite && durability == 0) || (durability >= this.min && durability <= this.max); }

    public Optional<DurabilityData> asOptional() {
        if(!this.isValid())
            return Optional.empty();
        return Optional.of(this);
    }

    private String getFailMessage()
    {
        StringBuilder builder = new StringBuilder();
        if(this.min < 0)
            this.addLine(builder,"min(" + this.min + ") must be greater than or equal to 0!");
        if(this.max <= this.min)
            this.addLine(builder,"max(" + this.max + ") must be greater than " + this.min);
        return builder.toString();
    }

    private void addLine(StringBuilder builder,String line)
    {
        if(!builder.isEmpty())
            builder.append('\n');
        builder.append(line);
    }

}
