package io.github.lightman314.lightmanscurrency.common.enchantments.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class ItemOverride extends ValueInput {

    public static final Codec<ItemOverride> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(Codec.STRING.fieldOf("baseCost").forGetter(v -> v.costInput),
                    Codec.STRING.listOf().fieldOf("items").forGetter(ItemOverride::writeList))
                    .apply(builder,ItemOverride::new));

    private final List<ResourceLocation> items;
    private final List<TagKey<Item>> tags;

    private ItemOverride(String costInput, List<String> inputs) {
        super(costInput);
        List<ResourceLocation> itemTemp = new ArrayList<>();
        List<TagKey<Item>> tagTemp = new ArrayList<>();
        for(String entry : inputs)
        {
            if(entry.startsWith("#"))
                tagTemp.add(TagKey.create(Registries.ITEM, VersionUtil.parseResource(entry.substring(1))));
            else
                itemTemp.add(VersionUtil.parseResource(entry));
        }
        this.items = ImmutableList.copyOf(itemTemp);
        this.tags = ImmutableList.copyOf(tagTemp);
    }

    private List<String> writeList()
    {
        List<String> list = new ArrayList<>();
        for(TagKey<Item> tag : this.tags)
            list.add("#" + tag.location());
        for(ResourceLocation item : this.items)
            list.add(item.toString());
        return list;
    }

    public boolean matches(@Nonnull ItemStack item) {
        return this.items.contains(BuiltInRegistries.ITEM.getKey(item.getItem())) || this.tags.stream().anyMatch(t -> InventoryUtil.ItemHasTag(item,t));
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ItemOverride other)
            return other.costInput.equals(this.costInput) && this.items.equals(other.items);
        return super.equals(obj);
    }

    @Override
    public int hashCode() { return Objects.hash(this.costInput,this.items); }


    public static Builder builder(String baseCost, RepairWithMoneyData.Builder parentBuilder) { return new Builder(baseCost,parentBuilder); }

    public static class Builder
    {
        private final RepairWithMoneyData.Builder parent;
        private final String baseCost;
        private final List<String> inputs = new ArrayList<>();
        private Builder(String baseCost, RepairWithMoneyData.Builder parent) { this.baseCost = baseCost; this.parent = parent; }

        public Builder withItem(Supplier<? extends ItemLike> item) { return this.withItem(item.get()); }
        public Builder withItem(ItemLike item) { return this.withItem(BuiltInRegistries.ITEM.getKey(item.asItem())); }
        public Builder withItem(ResourceLocation item) { this.inputs.add(item.toString()); return this; }

        public Builder withTag(TagKey<Item> tag) { return this.withTag(tag.location()); }
        public Builder withTag(ResourceLocation tag) { this.inputs.add("#" + tag); return this; }

        public RepairWithMoneyData.Builder build() { return this.parent.itemOverride(new ItemOverride(this.baseCost,this.inputs)); }

    }

}
