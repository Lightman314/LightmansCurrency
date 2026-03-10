package io.github.lightman314.lightmanscurrency.api.misc.item_handlers;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FlexibleSlotItemHandler implements IItemHandler {

    public static final Codec<FlexibleSlotItemHandler> CODEC = createCodec(FlexibleSlotItemHandler::new);

    public static <T extends FlexibleSlotItemHandler> Codec<T> createCodec(Function<List<ItemStack>,T> factory) { return CodecHelper.UNLIMITED_ITEM_LIST.xmap(factory, s -> s.stacks); }
    public static <T extends FlexibleSlotItemHandler> StreamCodec<RegistryFriendlyByteBuf,T> createStreamCodec(Function<List<ItemStack>,T> factory) { return ItemStack.LIST_STREAM_CODEC.map(factory, s -> s.stacks); }

    protected final NonNullList<ItemStack> stacks = NonNullList.of(ItemStack.EMPTY);

    private final List<Runnable> listeners = new ArrayList<>();

    protected FlexibleSlotItemHandler() {}
    protected FlexibleSlotItemHandler(List<ItemStack> stacks) {
        this.stacks.addAll(ItemHandlerUtil.combineStacks(stacks));
        this.stacks.removeIf(ItemStack::isEmpty);
    }

    public FlexibleSlotItemHandler copy() { return new FlexibleSlotItemHandler(this.stacks); }

    public final List<ItemStack> getStacks() { return this.copyStacks(); }
    public final List<ItemStack> getStacksAndClear() {
        List<ItemStack> result = this.copyStacks();
        this.clear();
        return result;
    }

    protected final List<ItemStack> copyStacks() { return ItemHandlerUtil.copyList(this.stacks); }

    public boolean isEmpty() { return this.stacks.isEmpty(); }

    public void clear() { this.stacks.clear(); }

    public final void load(List<ItemStack> items) {
        this.stacks.clear();
        this.stacks.addAll(ItemHandlerUtil.combineStacks(stacks));
        this.stacks.removeIf(ItemStack::isEmpty);
    }

    public final void load(FlexibleSlotItemHandler inventory) {
        this.stacks.clear();
        this.stacks.addAll(ItemHandlerUtil.copyList(inventory.stacks));
    }

    public FlexibleSlotItemHandler withListener(Runnable listener) {
        if(!this.listeners.contains(listener))
            this.listeners.add(listener);
        return this;
    }

    public void removeListener(Runnable listener) { this.listeners.remove(listener); }

    protected int getTotalSpace() { return 64; }

    public final int getCurrentCount() {
        int count = 0;
        for(ItemStack s : this.stacks)
            count += s.getCount();
        return count;
    }

    public final int getCurrentSpace() { return this.getTotalSpace() - this.getCurrentCount(); }

    @Override
    public int getSlots() { return this.stacks.size() + 9; }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if(slot >= 0 && slot < this.stacks.size())
            return this.stacks.get(slot).copy();
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot,ItemStack stack,boolean simulate) {
        if(this.isItemValid(stack))
        {
            stack = stack.copy();
            int space = this.getCurrentSpace();
            int insertAmount = Math.min(stack.getCount(),space);
            //Look for a slot that's already full
            for (ItemStack sis : this.stacks)
            {
                if (ItemStack.isSameItemSameComponents(stack, sis)) {
                    if (!simulate) {
                        sis.grow(insertAmount);
                        this.onContentsChanged();
                    }
                    stack.shrink(insertAmount);
                    return stack;
                }
            }
            //If no matching stacks were found, append to the end of the list
            if(!simulate)
            {
                this.stacks.add(stack.copyWithCount(insertAmount));
                this.onContentsChanged();
            }
            stack.shrink(insertAmount);
            return stack;
        }
        return stack;
    }

    @Override
    public final ItemStack extractItem(int slot,int amount,boolean simulate) {
        if(slot < 0 || slot >= this.stacks.size())
            return ItemStack.EMPTY;
        ItemStack sis = this.stacks.get(slot);
        int removeAmount = Math.min(amount,sis.getCount());
        ItemStack result = sis.copyWithCount(removeAmount);
        if(!simulate)
        {
            sis.shrink(removeAmount);
            if(sis.isEmpty())
                this.stacks.remove(slot);
            this.onContentsChanged();
        }
        return result;
    }

    @Override
    public final int getSlotLimit(int slot) {
        ItemStack sis = this.getStackInSlot(slot);
        return this.getCurrentSpace() - sis.getCount();
    }

    @Override
    public final boolean isItemValid(int slot, ItemStack stack) { return this.isItemValid(stack); }

    protected boolean isItemValid(ItemStack stack) { return true; }

    public final ItemStack removeRandomItem(RandomSource random)
    {
        int totalCount = this.getCurrentCount();
        if(totalCount <= 0)
            return ItemStack.EMPTY;
        int rand = random.nextInt(totalCount);
        for(int i = 0; i < this.stacks.size(); ++i)
        {
            ItemStack item = this.stacks.get(i);
            rand -= item.getCount();
            if(rand < 0)
            {
                ItemStack result = item.split(1);
                if(item.isEmpty())
                    this.stacks.remove(i);
                this.onContentsChanged();
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    protected void onContentsChanged() {
        for(Runnable l : new ArrayList<>(this.listeners))
            l.run();
    }

}
