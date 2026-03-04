package io.github.lightman314.lightmanscurrency.common.traders.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record CodecData(List<ItemStack> items,List<Boolean> enforceNBT,TradeDirection type,String customName1,String customName2) {

    public static final MapCodec<CodecData> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ItemStack.OPTIONAL_CODEC.listOf(4,4).fieldOf("items").forGetter(CodecData::items),
                Codec.BOOL.listOf(4,4).fieldOf("nbt_test").forGetter(CodecData::enforceNBT),
                TradeDirection.CODEC.fieldOf("direction").forGetter(CodecData::type),
                Codec.STRING.fieldOf("custom_name_1").forGetter(CodecData::customName1),
                Codec.STRING.fieldOf("custom_name_2").forGetter(CodecData::customName2)
        ).apply(builder,CodecData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,CodecData> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_LIST_STREAM_CODEC,CodecData::items,
            ByteBufCodecs.BOOL.apply(ByteBufCodecs.list()),CodecData::enforceNBT,
            TradeDirection.STREAM_CODEC,CodecData::type,
            ByteBufCodecs.STRING_UTF8,CodecData::customName1,
            ByteBufCodecs.STRING_UTF8,CodecData::customName2,
            CodecData::new);

}
