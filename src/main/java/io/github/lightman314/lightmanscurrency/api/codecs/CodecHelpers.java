package io.github.lightman314.lightmanscurrency.api.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CodecHelpers {

    private CodecHelpers() {}

    public static final Codec<ListTag> LIST_TAG_CODEC = Codec.PASSTHROUGH
            .comapFlatMap(
                    p_311527_ -> {
                        Tag tag = p_311527_.convert(NbtOps.INSTANCE).getValue();
                        return tag instanceof ListTag listTag
                                ? DataResult.success(listTag == p_311527_.getValue() ? listTag.copy() : listTag)
                                : DataResult.error(() -> "Not a list tag: " + tag);
                    },
                    p_311526_ -> new Dynamic<>(NbtOps.INSTANCE, p_311526_.copy())
            );

    public static final Codec<ItemStack> UNLIMITED_ITEM = Codec.lazyInitialized(
            () -> RecordCodecBuilder.create(
                    builder -> builder.group(
                                    ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
                                    ExtraCodecs.intRange(1, Integer.MAX_VALUE).fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
                                    DataComponentPatch.CODEC
                                            .optionalFieldOf("components", DataComponentPatch.EMPTY)
                                            .forGetter(ItemStack::getComponentsPatch)
                            )
                            .apply(builder, ItemStack::new)
            )
    );
    public static final Codec<ItemStack> UNLIMITED_ITEM_OPTIONAL = ExtraCodecs.optionalEmptyMap(UNLIMITED_ITEM)
            .xmap(p_330099_ -> p_330099_.orElse(ItemStack.EMPTY), p_330101_ -> p_330101_.isEmpty() ? Optional.empty() : Optional.of(p_330101_));

    public static final StreamCodec<RegistryFriendlyByteBuf,Optional<Item>> OPTIONAL_ITEM_STREAM = ByteBufCodecs.optional(ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()));

    public static <T> Codec<T> byNameCodec(Function<ResourceLocation,T> parser,Function<T,ResourceLocation> idGetter,String name)
    {
        return ResourceLocation.CODEC.comapFlatMap(id -> {
            T result = parser.apply(id);
            if(result != null)
                return DataResult.success(result);
            return DataResult.error(() -> "Unknown " + name + ": " + id);
        },idGetter);
    }

    public static <T> Codec<T> oldValueLoader(final Function<CompoundTag,T> loader,String object)
    {
        return CompoundTag.CODEC.flatXmap(tag -> {
            T result = loader.apply(tag);
            if(result != null)
                return DataResult.success(result);
            return DataResult.error(() -> object + " could not be decoded!");
        },(entry -> DataResult.error(() -> "Cannot encode " + object + " using deprecated methods!")));
    }

    public static <T> Codec<T> oldValueLoader(final BiFunction<CompoundTag,HolderLookup.Provider,T> loader,String object)
    {
        return CompoundTag.CODEC.flatXmap(tag -> {
            T result = loader.apply(tag,LookupHelper.getRegistryAccess());
            if(result != null)
                return DataResult.success(result);
            return DataResult.error(() -> object + " could not be decoded!");
        },(entry -> DataResult.error(() -> "Cannot encode " + object + " using deprecated methods!")));
    }

    public static <T> Codec<T> oldValueListLoader(final Function<ListTag,T> loader,String object)
    {
        return LIST_TAG_CODEC.flatXmap(tag -> {
            T result = loader.apply(tag);
            if(result == null)
                return DataResult.error(() -> object + " could not be decoded!");
            return DataResult.success(result);
        },entry -> DataResult.error(() -> "Cannot encode " + object + " using deprecated methods!"));
    }

}
