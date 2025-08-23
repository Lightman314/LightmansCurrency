package io.github.lightman314.lightmanscurrency.common.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

@Deprecated(since = "2.2.6.3")
public record TicketData(long id, int color) {

    public static final Codec<TicketData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(Codec.LONG.fieldOf("TicketID").forGetter(TicketData::id),
                    Codec.INT.fieldOf("TicketColor").forGetter(TicketData::color))
                    .apply(builder, TicketData::new));

    public static final StreamCodec<FriendlyByteBuf,TicketData> STREAM_CODEC = StreamCodec.of((b,d) -> {
        b.writeLong(d.id()); b.writeInt(d.color());
    },(b) -> new TicketData(b.readLong(),b.readInt()));

    @Override
    public int hashCode() { return Objects.hash(this.id,this.color); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TicketData other)
            return other.id == this.id && other.color == this.color;
        return false;
    }

}
