package io.github.lightman314.lightmanscurrency.api.misc.item_handlers;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class LCItemStackHandler extends ItemStackHandler {

    public static <T extends LCItemStackHandler> Codec<T> createCodec(Function<List<ItemStack>,T> factory) { return ItemStack.OPTIONAL_CODEC.listOf().xmap(factory,s -> s.stacks); }
    public static <T extends LCItemStackHandler> StreamCodec<RegistryFriendlyByteBuf,T> createStreamCodec(Function<List<ItemStack>,T> factory) { return ItemStack.OPTIONAL_LIST_STREAM_CODEC.map(factory, s -> s.stacks); }

    public static final StreamCodec<RegistryFriendlyByteBuf,LCItemStackHandler> STREAM_CODEC = createStreamCodec(LCItemStackHandler::new);

    private final List<Runnable> listeners = new ArrayList<>();

    public LCItemStackHandler() {}
    public LCItemStackHandler(int size) { super(size); }
    public LCItemStackHandler(ItemStack stack) { this(ImmutableList.of(stack)); }
    public LCItemStackHandler(List<ItemStack> items) { super(NonNullList.copyOf(ItemHandlerUtil.copyList(items))); }

    public LCItemStackHandler withListener(Runnable listener) {
        if(!this.listeners.contains(listener))
            this.listeners.add(listener);
        return this;
    }

    public void removeListener(Runnable listener) { this.listeners.remove(listener); }

    public LCItemStackHandler copy() { return new LCItemStackHandler(this.copyStacks()); }

    public void clear() { Collections.fill(this.stacks,ItemStack.EMPTY); }

    public final List<ItemStack> getStacks() { return this.copyStacks(); }
    public final List<ItemStack> getStacksAndClear() {
        List<ItemStack> result = this.copyStacks();
        this.clear();
        return result;
    }

    protected final List<ItemStack> copyStacks() { return ItemHandlerUtil.copyList(this.stacks); }

    public boolean isEmpty() { return this.stacks.stream().allMatch(ItemStack::isEmpty); }

    @Deprecated
    public void copyFromContainer(Container container) {
        NonNullList<ItemStack> list = InventoryUtil.buildList(container);
        //Force the new list to match my current list size
        while(list.size() < this.stacks.size())
            list.add(ItemStack.EMPTY);
        while(list.size() > this.stacks.size())
        {
            ItemStack removed = list.removeLast();
            if(!removed.isEmpty())
            {
                //Try and find an empty slot
                for(int i = 0; i < list.size(); ++i)
                {
                    if(list.get(i).isEmpty())
                        list.set(i,removed);
                }
            }
        }
        this.stacks = list;
    }

    public List<ItemStack> forceResize(int size) {
        //Grow the list to size
        while(this.stacks.size() < size)
            this.stacks.add(ItemStack.EMPTY);
        List<ItemStack> overflow = new ArrayList<>();
        //Shrink the list to size (storing overflow stacks for later)
        while(this.stacks.size() > size)
        {
            ItemStack lost = this.stacks.removeLast();
            if(!lost.isEmpty())
                overflow.add(lost);
        }
        //Attempt to put the overflow items back into the empty slots
        for(int i = 0; i < overflow.size(); ++i)
        {
            ItemStack s = overflow.get(i);
            for(int x = 0; x < this.stacks.size() && !s.isEmpty(); ++x)
            {
                ItemStack s2 = this.stacks.get(x);
                if(s2.isEmpty() || ItemStack.isSameItemSameComponents(s,s2))
                {
                    if(s2.isEmpty())
                    {
                        this.stacks.set(x,s.copyAndClear());
                        overflow.remove(i);
                        i--;
                    }
                    else
                    {
                        int addAmount = Math.min(Math.min(s.getCount(),s2.getMaxStackSize() - s2.getCount()),this.getSlotLimit(x));
                        s2.grow(addAmount);
                        s.shrink(addAmount);
                        if(s.isEmpty())
                        {
                            overflow.remove(i);
                            i--;
                        }
                    }
                }
            }
        }
        return overflow;
    }

    @SuppressWarnings("deprecation")
    public final void safeLoad(CompoundTag tag, String key, DataContext<Tag> context)
    {
        if(tag.contains(key, Tag.TAG_COMPOUND))
            this.deserializeNBT(context.registryAccess(),tag.getCompound(key));
        else
        {
            Container container = InventoryUtil.loadAllItems(key,tag,this.stacks.size(),context.registryAccess());
            this.copyFromContainer(container);
        }
    }

    @Override
    protected void onContentsChanged(int slot) {
        for(Runnable l : new ArrayList<>(this.listeners))
            l.run();
    }

    @Override
    @SuppressWarnings("deprecation")
    public int hashCode() { return ItemStack.hashStackList(this.stacks); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof IItemHandler handler)
            return ItemHandlerUtil.equals(this,handler);
        return false;
    }

}
