package io.github.lightman314.lightmanscurrency.common.items.data;

import com.google.common.collect.ImmutableList;
import dev.architectury.fluid.FluidStack;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record FilterData(List<ResourceLocation> entries, List<ResourceLocation> tags) {

    public static final FilterData EMPTY = new FilterData(ImmutableList.of(),ImmutableList.of());

    public boolean isEmpty() { return this.entries.isEmpty() && this.tags.isEmpty(); }

    public void write(ItemStack item)
    {
        //If empty, delete the data from the item
        if(this.isEmpty())
        {
            CompoundTag itemTag = item.getTag();
            if(itemTag != null && itemTag.contains("FilterData"))
                itemTag.remove("FilterData");
            return;
        }
        //Otherwise save the item to the tag
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for(ResourceLocation e : this.entries)
            list.add(StringTag.valueOf(e.toString()));
        if(!list.isEmpty())
            tag.put("entries",list);
        list = new ListTag();
        for(ResourceLocation t : this.tags)
            list.add(StringTag.valueOf(t.toString()));
        if(!list.isEmpty())
            tag.put("tags",list);
        CompoundTag itemTag = item.getOrCreateTag();
        itemTag.put("FilterData",tag);
    }

    public static FilterData parse(ItemStack item)
    {
        CompoundTag itemTag = item.getTag();
        if(itemTag == null || !itemTag.contains("FilterData"))
            return FilterData.EMPTY;
        CompoundTag tag = itemTag.getCompound("FilterData");
        List<ResourceLocation> entries = new ArrayList<>();
        List<ResourceLocation> tags = new ArrayList<>();
        if(tag.contains("entries"))
        {
            ListTag list = tag.getList("entries", Tag.TAG_STRING);
            for(int i = 0; i < list.size(); ++i)
                entries.add(VersionUtil.parseResource(list.getString(i)));
        }
        if(tag.contains("tags"))
        {
            ListTag list = tag.getList("tags", Tag.TAG_STRING);
            for(int i = 0; i < list.size(); ++i)
                tags.add(VersionUtil.parseResource(list.getString(i)));
        }
        return new FilterData(ImmutableList.copyOf(entries),ImmutableList.copyOf(tags));
    }

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
            ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(s.getItem());
            if(this.entries.contains(itemKey))
                return true;
            return s.getTags().anyMatch(tagKey -> this.tags.contains(tagKey.location()));
        };
    }

    public Predicate<FluidStack> asFluidPredicate()
    {
        return s -> {
            ResourceLocation fluidKey = ForgeRegistries.FLUIDS.getKey(s.getFluid());
            if(this.entries.contains(fluidKey))
                return true;
            Holder<Fluid> holder = ForgeRegistries.FLUIDS.getHolder(s.getFluid()).orElse(null);
            return holder != null && holder.tags().anyMatch(tagKey -> this.tags.contains(tagKey.location()));
        };
    }

}