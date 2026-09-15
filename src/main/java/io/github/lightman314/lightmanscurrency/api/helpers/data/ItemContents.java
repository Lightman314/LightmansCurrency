package io.github.lightman314.lightmanscurrency.api.helpers.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.*;

/**
 * Replication of {@link net.minecraft.world.item.component.ItemContainerContents ItemContainerContents} but without the tooltip provider and more easily translatable into a list of items
 */
public class ItemContents {

    public static final ItemContents EMPTY = new ItemContents(List.of());

    public static final Codec<ItemContents> CODEC = Slot.CODEC
            .sizeLimitedListOf(256)
            .xmap(ItemContents::fromSlots,ItemContents::asSlots);
    public static final StreamCodec<RegistryFriendlyByteBuf,ItemContents> STREAM_CODEC = ItemStackTemplate.STREAM_CODEC
            .apply(ByteBufCodecs::optional)
            .apply(ByteBufCodecs.list(256))
            .map(ItemContents::new,c -> c.items);

    private final List<Optional<ItemStackTemplate>> items;
    private final int hashCode;
    private ItemContents(List<Optional<ItemStackTemplate>> items) {
        if(items.size() > 256)
            throw new IllegalStateException("Got " + items.size() + " items, but maximum is 256");
        this.items = ImmutableList.copyOf(items);
        this.hashCode = this.items.hashCode();
    }

    public boolean isEmpty() { return this.items.isEmpty() || this.items.stream().allMatch(Optional::isEmpty); }

    private static List<Optional<ItemStackTemplate>> emptyContents(int size) {
        return new ArrayList<>(Collections.nCopies(size, Optional.empty()));
    }

    private static ItemContents fromSlots(List<Slot> slots) {
        OptionalInt maxSlotIndex = slots.stream().mapToInt(Slot::index).max();
        if(maxSlotIndex.isEmpty())
            return EMPTY;
        else
        {
            List<Optional<ItemStackTemplate>> items = emptyContents(maxSlotIndex.getAsInt() + 1);
            for(Slot slot : slots)
                items.set(slot.index,Optional.of(slot.item));
            return new ItemContents(items);
        }
    }

    private List<Slot> asSlots() {
        List<Slot> result = new ArrayList<>();
        for(int i = 0; i < this.items.size(); ++i)
        {
            Optional<ItemStackTemplate> item = this.items.get(i);
            if(item.isPresent())
                result.add(new Slot(i,item.get()));
        }
        return result;
    }

    public final int size() { return this.items.size(); }

    public List<ItemStack> asItems() { return asItems(this.size(),new ArrayList<>()); }
    public List<ItemStack> asItems(int expectedSize,List<ItemStack> overflow) {
        List<ItemStack> result = NonNullList.withSize(expectedSize,ItemStack.EMPTY);
        for(int i = 0; i < this.items.size(); ++i) {
            Optional<ItemStackTemplate> item = this.items.get(i);
            if(item.isPresent())
            {
                ItemStack stack = item.get().create();
                if(i >= expectedSize)
                    overflow.add(stack);
                else
                    result.set(i,stack);
            }
        }
        return result;
    }

    public static ItemContents fromItems(List<ItemStack> items) {
        int lastNonEmptySlot = findLastNonEmptySlot(items);
        if(lastNonEmptySlot < 0)
            return EMPTY;
        List<Optional<ItemStackTemplate>> result = emptyContents(lastNonEmptySlot + 1);
        for(int i = 0; i <= lastNonEmptySlot; ++i)
        {
            ItemStack stack = items.get(i);
            if(!stack.isEmpty())
                result.set(i,Optional.of(ItemStackTemplate.fromNonEmptyStack(stack)));
        }
        return new ItemContents(result);
    }

    private static int findLastNonEmptySlot(List<ItemStack> items) {
        for(int i = items.size() - 1; i >= 0; --i){
            if(!items.get(i).isEmpty())
                return i;
        }
        return -1;
    }

    @Override
    public boolean equals(Object obj) { return this == obj || obj instanceof ItemContents other && this.items.equals(other.items); }

    @Override
    public int hashCode() { return this.hashCode; }

    private record Slot(int index, ItemStackTemplate item) {
        public static final Codec<Slot> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.intRange(0,255).fieldOf("slot").forGetter(Slot::index),
                ItemStackTemplate.CODEC.fieldOf("item").forGetter(Slot::item))
                .apply(builder,Slot::new));
    }

}