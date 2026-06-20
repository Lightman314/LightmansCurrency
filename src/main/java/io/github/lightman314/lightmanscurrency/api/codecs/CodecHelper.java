package io.github.lightman314.lightmanscurrency.api.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

public class CodecHelper {

    private CodecHelper() {}

    public static final MapCodec<ItemStack> UNLIMITED_ITEM_MAP_CODEC = MapCodec.recursive(
            "ItemStack",
            subCodec -> RecordCodecBuilder.mapCodec(
                    i -> i.group(
                                    Item.CODEC_WITH_BOUND_COMPONENTS.fieldOf("id").forGetter(ItemStack::typeHolder),
                                    ExtraCodecs.intRange(1, 99).fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
                                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
                            )
                            .apply(i, ItemStack::new)
            )
    );
    public static final Codec<ItemStack> UNLIMITED_ITEM = Codec.lazyInitialized(UNLIMITED_ITEM_MAP_CODEC::codec);
    public static final Codec<List<ItemStack>> UNLIMITED_ITEM_LIST = UNLIMITED_ITEM.listOf();
    public static final Codec<ItemStack> UNLIMITED_ITEM_OPTIONAL = ExtraCodecs.optionalEmptyMap(UNLIMITED_ITEM)
            .xmap(p_330099_ -> p_330099_.orElse(ItemStack.EMPTY), p_330101_ -> p_330101_.isEmpty() ? Optional.empty() : Optional.of(p_330101_));


    public static final Codec<BigDecimal> BIG_DECIMAL = Codec.STRING.xmap(BigDecimal::new,BigDecimal::toString);

    public static final Codec<Long> LONG_KEY = Codec.STRING.comapFlatMap(string -> {
        try {
            long result = Long.parseLong(string);
            return DataResult.success(result);
        } catch (NumberFormatException e) { return DataResult.error(e::getMessage); }
    },l -> Long.toString(l));

    public static <T> Codec<Set<T>> setCodec(Codec<T> codec) { return codec.listOf().xmap(HashSet::new,ArrayList::new); }

    public static <T> Codec<T> byNameCodec(Function<Identifier,T> parser,Function<T,Identifier> idGetter,String name)
    {
        return Identifier.CODEC.comapFlatMap(id -> {
            T result = parser.apply(id);
            if(result != null)
                return DataResult.success(result);
            return DataResult.error(() -> "Unknown " + name + ": " + id);
        },idGetter);
    }

}