package io.github.lightman314.lightmanscurrency.common.items.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.fluid.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public record FilterData(List<ResourceLocation> entries, List<ResourceLocation> tags) {

    public static final FilterData EMPTY = new FilterData(ImmutableList.of(),ImmutableList.of());

    public static final Codec<FilterData> CODEC = RecordCodecBuilder.create(builder ->
        builder.group(
                ResourceLocation.CODEC.listOf().fieldOf("entries").forGetter(FilterData::entries),
                ResourceLocation.CODEC.listOf().fieldOf("tags").forGetter(FilterData::tags))
                .apply(builder,FilterData::new));

    public static final StreamCodec<FriendlyByteBuf,FilterData> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()),FilterData::entries,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()),FilterData::tags,
            FilterData::new);

    public boolean isEmpty() { return this.entries.isEmpty() && this.tags.isEmpty(); }

    public FilterData addEntry(ResourceLocation entry)
    {
        if(this.entries.contains(entry))
            return this;
        List<ResourceLocation> newEntries = new ArrayList<>(this.entries);
        newEntries.add(entry);
        return new FilterData(ImmutableList.copyOf(newEntries),this.tags);
    }

    public FilterData removeEntry(ResourceLocation entry)
    {
        if(!this.entries.contains(entry))
            return this;
        List<ResourceLocation> newEntries = new ArrayList<>(this.entries);
        newEntries.remove(entry);
        return new FilterData(ImmutableList.copyOf(newEntries),this.tags);
    }

    public FilterData addTag(ResourceLocation entry)
    {
        if(this.tags.contains(entry))
            return this;
        List<ResourceLocation> newTags = new ArrayList<>(this.tags);
        newTags.add(entry);
        return new FilterData(this.entries,ImmutableList.copyOf(newTags));
    }

    public FilterData removeTag(ResourceLocation entry)
    {
        if(!this.tags.contains(entry))
            return this;
        List<ResourceLocation> newTags = new ArrayList<>(this.tags);
        newTags.remove(entry);
        return new FilterData(this.entries,ImmutableList.copyOf(newTags));
    }

    public Predicate<ItemStack> asItemPredicate()
    {
        return s -> {
            ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(s.getItem());
            if(this.entries.contains(itemKey))
                return true;
            return s.getTags().anyMatch(tagKey -> this.tags.contains(tagKey.location()));
        };
    }

    public Predicate<FluidStack> asFluidPredicate()
    {
        return s -> {
            ResourceLocation fluidKey = BuiltInRegistries.FLUID.getKey(s.getFluid());
            if(this.entries.contains(fluidKey))
                return true;
            return BuiltInRegistries.FLUID.createIntrusiveHolder(s.getFluid()).tags().anyMatch(tagKey -> this.tags.contains(tagKey.location()));
        };
    }

}
