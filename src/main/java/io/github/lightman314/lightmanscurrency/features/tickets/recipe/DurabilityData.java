package io.github.lightman314.lightmanscurrency.features.tickets.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.features.tickets.data.TicketUses;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public class DurabilityData {

    private static final Codec<DurabilityData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    Codec.BOOL.optionalFieldOf("allowInfinite",false).forGetter(d -> d.allowInfinite),
                    Codec.intRange(0,Integer.MAX_VALUE).fieldOf("min").forGetter(d -> d.min),
                    Codec.intRange(0,Integer.MAX_VALUE).fieldOf("max").forGetter(d -> d.max))
            .apply(builder,DurabilityData::new));
    public static final Codec<DurabilityData> VALID_CODEC = CODEC.validate(DurabilityData::validate);
    public static final StreamCodec<FriendlyByteBuf,DurabilityData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL,d -> d.allowInfinite,ByteBufCodecs.INT,d -> d.min,ByteBufCodecs.INT,d -> d.max,DurabilityData::new);

    public static DurabilityData NULL = new DurabilityData(false,0,0);
    public static DurabilityData DEFAULT = new DurabilityData(true,1,99);

    public final boolean allowInfinite;
    public final int min;
    public final int max;
    public DurabilityData(boolean allowInfinite,int min, int max) { this.allowInfinite = allowInfinite; this.min = min; this.max = max; }

    public boolean isValid() { return this.min <= this.max && this.max >= 0 && this.min >= 0 && (this.min > 0 || this.allowInfinite); }

    public boolean requiresInput() { return this.isValid() && ((this.allowInfinite && this.min > 0) || (this.max > this.min)); }

    public DataResult<DurabilityData> validate() {
        return this.isValid() ? DataResult.success(this) : DataResult.error(this::getFailMessage);
    }

    public DurabilityData assertValid() throws IllegalStateException {
        if(!this.isValid())
            throw new IllegalStateException("Durability Data is not valid: " + this.getFailMessage());
        return this;
    }

    public int forceWithinRange(int durability) {
        if(durability <= 0 && this.allowInfinite)
            return 0;
        return Math.clamp(durability,this.min,this.max);
    }

    public TicketUses assemble(int durability) {
        int result = this.forceWithinRange(durability);
        if(result <= 0)
            return TicketUses.UNLIMITED;
        return new TicketUses(result);
    }

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
        if(this.max < this.min)
            this.addLine(builder,"maxUses(" + this.max + ") must be greater than or equal to min(" + this.min + ")");
        if(this.min == 0 && !this.allowInfinite)
            this.addLine(builder,"min can only be 0 if infinite durability is allowed!");
        return builder.toString();
    }

    private void addLine(StringBuilder builder,String line)
    {
        if(!builder.isEmpty())
            builder.append('\n');
        builder.append(line);
    }

}