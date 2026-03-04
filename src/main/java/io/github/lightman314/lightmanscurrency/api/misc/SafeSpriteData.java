package io.github.lightman314.lightmanscurrency.api.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record SafeSpriteData(ResourceLocation texture,int u,int v,int width,int height,int textureWidth,int textureHeight) {

    public static final MapCodec<SafeSpriteData> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("texture").forGetter(SafeSpriteData::texture),
            Codec.INT.fieldOf("u").forGetter(SafeSpriteData::u),
            Codec.INT.fieldOf("v").forGetter(SafeSpriteData::v),
            Codec.INT.fieldOf("width").forGetter(SafeSpriteData::width),
            Codec.INT.fieldOf("height").forGetter(SafeSpriteData::height),
            Codec.INT.optionalFieldOf("textureWidth",256).forGetter(SafeSpriteData::textureWidth),
            Codec.INT.optionalFieldOf("textureHeight",256).forGetter(SafeSpriteData::textureHeight)
    ).apply(builder,SafeSpriteData::new));
    public static final Codec<SafeSpriteData> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<ByteBuf,SafeSpriteData> STREAM_CODEC = StreamHelper.composite(
            ResourceLocation.STREAM_CODEC,SafeSpriteData::texture,
            ByteBufCodecs.INT,SafeSpriteData::u,
            ByteBufCodecs.INT,SafeSpriteData::v,
            ByteBufCodecs.INT,SafeSpriteData::width,
            ByteBufCodecs.INT,SafeSpriteData::height,
            ByteBufCodecs.INT,SafeSpriteData::textureWidth,
            ByteBufCodecs.INT,SafeSpriteData::textureHeight,
            SafeSpriteData::new);



}
