package io.github.lightman314.lightmanscurrency.features.enchantments.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.source.MoneyValueSource;
import io.github.lightman314.lightmanscurrency.api.money.values.source.builtin.DirectSource;
import net.minecraft.IdentifierException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.*;
import java.util.function.Consumer;

public final class ItemOverride {

    public static final Codec<ItemOverride> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            MoneyValueSource.CODEC.fieldOf("baseCost").forGetter(o -> o.baseCost),
            Codec.STRING.listOf(1,Integer.MAX_VALUE).fieldOf("items").forGetter(ItemOverride::writeList)
    ).apply(builder,ItemOverride::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,ItemOverride> STREAM_CODEC = StreamCodec.composite(
            MoneyValueSource.STREAM_CODEC,o -> o.baseCost,
            StreamHelper.setCodec(Identifier.STREAM_CODEC),o -> o.items,
            StreamHelper.setCodec(TagKey.streamCodec(Registries.ITEM)),o -> o.tags,
            ItemOverride::new);

    private final MoneyValueSource baseCost;
    public MoneyValue getBaseCost() { return this.baseCost.getMoneyValue(); }
    private final Set<Identifier> items;
    private final Set<TagKey<Item>> tags;
    public ItemOverride(MoneyValueSource baseCost,Collection<Identifier> items,Collection<TagKey<Item>> tags) {
        this.baseCost = baseCost;
        this.items = Set.copyOf(items);
        this.tags = Set.copyOf(tags);
    }
    private ItemOverride(MoneyValueSource baseCost,Collection<String> inputs) {
        this.baseCost = baseCost;
        List<Identifier> itemTemp = new ArrayList<>();
        List<TagKey<Item>> tagTemp = new ArrayList<>();
        for(String entry : inputs) {
            try {
                if(entry.startsWith("#"))
                    tagTemp.add(TagKey.create(Registries.ITEM,Identifier.parse(entry.substring(1))));
                else
                    itemTemp.add(Identifier.parse(entry));
            } catch (IdentifierException ignored) {}
        }
        this.items = Set.copyOf(itemTemp);
        this.tags =  Set.copyOf(tagTemp);
    }
    private List<String> writeList() {
        List<String> list = new ArrayList<>();
        for(TagKey<Item> tag : this.tags)
            list.add("#" + tag.location());
        for(Identifier item : this.items)
            list.add(item.toString());
        return list;
    }

    public boolean matches(ItemStack item) {
        return this.items.contains(BuiltInRegistries.ITEM.getKey(item.getItem())) || this.tags.stream().anyMatch(item::is);
    }

    @Override
    public int hashCode() { return Objects.hash(this.baseCost,this.items,this.tags.stream().map(TagKey::location).toList()); }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(obj instanceof ItemOverride override)
            return this.baseCost.equals(override.baseCost) && this.items.equals(override.items) && this.tags.equals(override.tags);
        return false;
    }

    public static Builder<Void> builder() { return builder(null,i -> {}); }

    public static <T> Builder<T> builder(T original, Consumer<ItemOverride> consumer) { return new Builder<>(original,consumer); }

    public static class Builder<T> {

        private final T parent;
        private final Consumer<ItemOverride> consumer;
        private MoneyValueSource baseCost = new DirectSource(MoneyValue.empty());
        private final Set<String> inputs = new HashSet<>();

        private Builder(T parent,Consumer<ItemOverride> consumer) {
            this.parent = parent;
            this.consumer = consumer;
        }

        public Builder<T> baseCost(MoneyValue value) { return this.baseCost(new DirectSource(value)); }
        public Builder<T> baseCost(MoneyValueSource value) { this.baseCost = value; return this; }

        public Builder<T> withItem(ItemLike item) { return this.withItem(BuiltInRegistries.ITEM.getKey(item.asItem())); }
        public Builder<T> withItem(Identifier item) { this.inputs.add(item.toString()); return this; }

        public Builder<T> withTag(TagKey<Item> tag) { return this.withTag(tag.location()); }
        public Builder<T> withTag(Identifier tag) { this.inputs.add("#" + tag); return this; }

        public ItemOverride build() { return new ItemOverride(this.baseCost,this.inputs); }
        public T buildAndReturn() { this.consumer.accept(this.build()); return this.parent; }

    }

}
