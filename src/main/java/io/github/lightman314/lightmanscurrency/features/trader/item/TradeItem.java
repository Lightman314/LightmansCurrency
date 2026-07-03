package io.github.lightman314.lightmanscurrency.features.trader.item;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class TradeItem implements Predicate<ItemResource> {

    public static final Codec<TradeItem> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ItemResource.OPTIONAL_CODEC.fieldOf("item").forGetter(r -> r.item),
            Codec.intRange(0,Integer.MAX_VALUE).fieldOf("count").forGetter(r -> r.count),
            Codec.STRING.optionalFieldOf("name").forGetter(r -> r.nameChange),
            Codec.BOOL.fieldOf("strict").forGetter(r -> r.strict)
    ).apply(builder, TradeItem::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, TradeItem> STREAM_CODEC = StreamCodec.composite(
            ItemResource.STREAM_CODEC,r -> r.item,
            ByteBufCodecs.INT,r -> r.count,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),r -> r.nameChange,
            ByteBufCodecs.BOOL,r -> r.strict,
            TradeItem::new);

    private ItemResource item;

    public ItemStack getStack() { return this.item.toStack(this.count); }
    public ItemStack getDisplayStack(TradeContext context) {
        //TODO
        return this.getStack();
    }
    public void setResource(ItemResource resource) { this.item = resource; }
    public void setStack(ItemStack stack) { this.item = ItemResource.of(stack); this.count = stack.getCount(); }

    private int count;
    public int getCount() { return this.count; }
    public void setCount(int count) { this.count = Math.max(0,count); }
    public boolean grow(int amount) {
        ItemStack stack = this.item.toStack();
        if(this.count < stack.getMaxStackSize())
        {
            this.count = Math.min(stack.getMaxStackSize(),this.count + amount);
            return true;
        }
        return false;
    }
    public boolean shrink(int amount) {
        ItemStack stack = this.item.toStack();
        if(this.count > 0)
        {
            this.count = Math.max(0,this.count - amount);
            if(this.count == 0)
                this.item = ItemResource.EMPTY;
            return true;
        }
        return false;
    }

    public boolean isEmpty() { return this.item.isEmpty() || this.count <= 0; }

    private Optional<String> nameChange;
    public Optional<String> getNameChange() { return this.nameChange; }
    public void setNameChange(String name) {
        if(name.isBlank())
            this.nameChange = Optional.empty();
        else
            this.nameChange = Optional.of(name);
    }

    private boolean strict;
    public boolean isStrict() { return this.strict && !this.isFilter(); }
    public void setStrict(boolean strict) { this.strict = strict; }

    public boolean isFilter() { return false; }

    public static TradeItem create() { return new TradeItem(ItemResource.EMPTY,0,Optional.empty(),true); }
    public static ImmutableList<TradeItem> createList(int size) {
        ImmutableList.Builder<TradeItem> builder = ImmutableList.builderWithExpectedSize(size);
        for(int i = 0; i < size; ++i)
            builder.add(create());
        return builder.build();
    }
    public static void loadList(List<TradeItem> list,List<TradeItem> data) {
        for(int i = 0; i < list.size() && i < data.size(); ++i)
            list.get(i).copyFrom(data.get(i));
    }

    private TradeItem(ItemResource item,int count,Optional<String> nameChange,boolean strict) {
        this.item = item;
        this.count = count;
        this.nameChange = nameChange;
        this.strict = strict;
    }

    private void copyFrom(TradeItem other) {
        this.item = ItemResource.of(other.getStack());
        this.count = other.count;
        this.nameChange = other.nameChange;
        this.strict = other.strict;
    }

    private TradeItem copy() {
        TradeItem copy = create();
        copy.copyFrom(this);
        return copy;
    }

    @Override
    public boolean test(ItemResource resource) {
        if(this.isFilter())
        {
            //Get Filter
            return false;
        }
        if(this.isStrict())
            return this.item.equals(resource);
        return this.item.is(resource.getItem());
    }

    @Override
    public int hashCode() { return Objects.hash(this.item,this.count,this.nameChange,this.strict); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TradeItem other)
            return this.item.equals(other.item) && this.count == other.count && this.nameChange.equals(other.nameChange) && this.strict == other.strict;
        return false;
    }

    public static List<TradeItem> combineMatching(List<TradeItem> items) {
        List<TradeItem> list = new ArrayList<>();
        for(TradeItem item : items)
        {
            //Ignore empty requirements
            if(item.isEmpty())
                continue;
            boolean add = true;
            for(TradeItem r : list)
            {
                if(r.equals(item))
                {
                    r.count += item.count;
                    add = false;
                    break;
                }
            }
            if(add)
                list.add(item.copy());
        }
        return list;
    }

}