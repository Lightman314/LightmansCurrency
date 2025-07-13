package io.github.lightman314.lightmanscurrency.common.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public record CouponData(int code, int color) {

    public static final Codec<CouponData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(Codec.INT.fieldOf("CouponCode").forGetter(CouponData::code),
                    Codec.INT.fieldOf("TicketColor").forGetter(CouponData::color))
                    .apply(builder, CouponData::new));

    public static final StreamCodec<FriendlyByteBuf, CouponData> STREAM_CODEC = StreamCodec.of((b, d) -> {
        b.writeInt(d.code()); b.writeInt(d.color());
    },(b) -> new CouponData(b.readInt(),b.readInt()));

    @Override
    public int hashCode() { return Objects.hash(this.code,this.color); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CouponData(int code1, int color1))
            return code1 == this.code && color1 == this.color;
        return false;
    }

}
