package io.github.lightman314.lightmanscurrency.api.notifications;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CommonData(boolean seen, int count, long timestamp) {

    public static final MapCodec<CommonData> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.BOOL.fieldOf("seen").forGetter(CommonData::seen),
            Codec.INT.fieldOf("count").forGetter(CommonData::count),
            Codec.LONG.fieldOf("timestamp").forGetter(CommonData::timestamp)
    ).apply(builder, CommonData::new));

    public static final StreamCodec<ByteBuf, CommonData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, CommonData::seen,
            ByteBufCodecs.INT, CommonData::count,
            ByteBufCodecs.VAR_LONG, CommonData::timestamp,
            CommonData::new);

}
