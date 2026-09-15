package io.github.lightman314.lightmanscurrency.api.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import net.minecraft.IdentifierException;
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
                                    ExtraCodecs.intRange(1,Integer.MAX_VALUE).fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
                                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
                            )
                            .apply(i, ItemStack::new)
            )
    );
    public static final Codec<ItemStack> UNLIMITED_ITEM = Codec.lazyInitialized(UNLIMITED_ITEM_MAP_CODEC::codec);
    public static final Codec<List<ItemStack>> UNLIMITED_ITEM_LIST = UNLIMITED_ITEM.listOf();
    public static final Codec<ItemStack> UNLIMITED_ITEM_OPTIONAL = ExtraCodecs.optionalEmptyMap(UNLIMITED_ITEM)
            .xmap(p_330099_ -> p_330099_.orElse(ItemStack.EMPTY), p_330101_ -> p_330101_.isEmpty() ? Optional.empty() : Optional.of(p_330101_));


    public static final Codec<Identifier> IDENTIFIER_LC_DEFAULT = identifierWithDefaultNamespace(LCApi.MODID);

    public static final Codec<BigDecimal> BIG_DECIMAL = Codec.STRING.xmap(BigDecimal::new,BigDecimal::toString);

    public static final Codec<Long> LONG_KEY = Codec.STRING.comapFlatMap(string -> {
        try {
            long result = Long.parseLong(string);
            return DataResult.success(result);
        } catch (NumberFormatException e) { return DataResult.error(e::getMessage); }
    },l -> Long.toString(l));

    public static Codec<Identifier> identifierWithDefaultNamespace(String defaultNamespace) {
        return Codec.STRING.comapFlatMap(string -> {
            if(string.contains(":"))
                return Identifier.read(string);
            try {
                return DataResult.success(Identifier.fromNamespaceAndPath(defaultNamespace,string));
            } catch (IdentifierException e) {
                return DataResult.error(() -> "Not a valid resource location: " + defaultNamespace + ":" + string + " " + e.getMessage());
            }
        },id -> {
            //Don't include the default namespace when encoding either
            if(id.getNamespace().equals(defaultNamespace))
                return id.getPath();
            return id.toString();
        });
    }

    public static <T> Codec<T> byNameCodec(Function<Identifier,T> parser,Function<T,Identifier> idGetter,String name) { return byNameCodec(Identifier.CODEC,parser,idGetter,name); }
    public static <T> Codec<T> byNameCodec(Codec<Identifier> idCodec,Function<Identifier,T> parser,Function<T,Identifier> idGetter,String name)
    {
        return idCodec.flatXmap(id -> {
            T result = parser.apply(id);
            if(result != null)
                return DataResult.success(result);
            return DataResult.error(() -> "Unknown " + name + ": " + id);
        },val -> {
            Identifier id = idGetter.apply(val);
            if(id != null)
                return DataResult.success(id);
            return DataResult.error(() -> "Cannot encode an unregistered " + name + ": " + val);
        });
    }

    public static <T> Codec<T> byNameCodec(Map<Identifier,T> map,String name) { return byNameCodec(Identifier.CODEC,map,name); }
    public static <T> Codec<T> byNameCodec(Codec<Identifier> idCodec,Map<Identifier,T> map,String name) {
        return byNameCodec(map::get,value -> {
            for(var entry : map.entrySet()) {
                if(entry.getValue() == value)
                    return entry.getKey();
            }
            return null;
        },name);
    }

    public static Codec<Long> longRange(final long minInclusive,final long maxInclusive) {
        final Function<Long,DataResult<Long>> checker = checkRange(minInclusive, maxInclusive);
        return Codec.LONG.flatXmap(checker,checker);
    }

    //Copy of Codec#checkRange
    public static <N extends Number & Comparable<N>> Function<N,DataResult<N>> checkRange(final N minInclusive,final N maxInclusive) {
        return value -> {
            if (value.compareTo(minInclusive) >= 0 && value.compareTo(maxInclusive) <= 0) {
                return DataResult.success(value);
            }
            return DataResult.error(() -> "Value " + value + " outside of range [" + minInclusive + ":" + maxInclusive + "]");
        };
    }

}