package io.github.lightman314.lightmanscurrency.common.traders.gacha;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.common.items.GachaBallItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class GachaStorage {

    private Runnable listener = () -> {};
    private final Supplier<Integer> maxStorage;
    public GachaStorage(Supplier<Integer> maxStorage) { this.maxStorage = maxStorage; }

    public GachaStorage withListener(Runnable listener) { this.listener = listener; return this; }

    private List<ItemStack> randomizedContents = null;
    private void clearRandomizedContents() { this.randomizedContents = null; }

    private final List<ItemStack> contents = new ArrayList<>();
    public List<ItemStack> getContents() { return this.contents; }
    public List<ItemStack> getSplitContents()
    {
        List<ItemStack> result = new ArrayList<>();
        for(ItemStack stack : ItemHandlerUtil.copyList(this.contents))
        {
            while(stack.getCount() > stack.getMaxStackSize())
                result.add(stack.split(stack.getMaxStackSize()));
            if(!stack.isEmpty())
                result.add(stack);
        }
        return result;
    }

    public ItemStack getStackInSlot(int slot) {
        if(slot < 0 || slot >= this.contents.size())
            return ItemStack.EMPTY;
        return this.contents.get(slot);
    }

    public void load(List<ItemStack> contents)
    {
        this.contents.clear();
        this.contents.addAll(contents);
        this.clearRandomizedContents();
    }

    @Deprecated
    public void loadOldData(ListTag list, HolderLookup.Provider lookup) {
        this.contents.clear();
        DataContext<Tag> context = DataContext.createNBT(lookup);
        this.contents.addAll(context.read(list,CodecHelper.UNLIMITED_ITEM_LIST));
        this.clearRandomizedContents();
    }

    public boolean isEmpty() { return this.contents.isEmpty() || this.getItemCount() <= 0; }

    public int getItemCount() {
        int count = 0;
        for(ItemStack item : this.contents)
            count += item.getCount();
        return count;
    }

    public int getSpace() { return this.maxStorage.get() - this.getItemCount(); }

    public boolean insertItem(ItemStack item)
    {
        int space = this.getSpace();
        if(space <= 0)
            return false;
        for(ItemStack entry : this.contents)
        {
            if(ItemStack.isSameItemSameComponents(entry,item))
            {
                int fittableAmount = Math.min(space,item.getCount());
                entry.grow(fittableAmount);
                item.shrink(fittableAmount);
                this.setChanged();
                return true;
            }
        }
        //Not found in existing stack, so we'll add a new entry to the list
        this.contents.add(item.split(space));
        this.setChanged();
        return true;
    }

    private void setChanged()
    {
        this.clearRandomizedContents();
        this.listener.run();
    }

    public void forceInsertItem(ItemStack item)
    {
        for(ItemStack entry : this.contents)
        {
            if(ItemStack.isSameItemSameComponents(entry,item))
            {
                entry.grow(item.getCount());
                this.setChanged();
                return;
            }
        }
        //Not found in existing stack, so we'll add a new entry to the list
        this.contents.add(item.copy());
        this.setChanged();
    }

    public ItemStack removeItem(int slot, int count) {
        if(slot < 0 || slot >= this.contents.size())
            return ItemStack.EMPTY;
        ItemStack item = this.contents.get(slot);
        ItemStack result = item.split(count);
        if(item.isEmpty())
            this.contents.remove(slot);
        this.setChanged();
        return result;
    }

    public ItemStack findRandomItem(boolean remove)
    {
        RandomSource random = RandomSource.create();
        if(this.contents.isEmpty())
            return ItemStack.EMPTY;
        int rand = random.nextInt(this.getItemCount());
        for(int i = 0; i < this.contents.size(); ++i)
        {
            ItemStack item = this.contents.get(i);
            rand -= item.getCount();
            if(rand < 0)
            {
                ItemStack result = item.copyWithCount(1);
                if(remove)
                {
                    item.shrink(1);
                    if(item.isEmpty())
                        this.contents.remove(i);
                    this.setChanged();
                }
                return result;
            }
        }
        LightmansCurrency.LogWarning("Somehow randomly generated nothing");
        return ItemStack.EMPTY;
    }

    public List<ItemStack> getRandomizedContents()
    {
        if(this.randomizedContents == null)
            this.randomizeContents();
        return this.randomizedContents == null ? new ArrayList<>() : new ArrayList<>(this.randomizedContents);
    }

    private void randomizeContents()
    {
        List<ItemStack> results = new ArrayList<>();
        RandomSource random = RandomSource.create();
        List<ItemStack> contentCopy = ItemHandlerUtil.copyList(this.contents);
        int totalCount = this.getItemCount();
        while(!contentCopy.isEmpty() && totalCount > 0)
        {
            int rand = random.nextInt(totalCount);
            for(int x = 0; x < contentCopy.size(); ++x)
            {
                ItemStack item = contentCopy.get(x);
                rand -= item.getCount();
                if(rand < 0)
                {
                    ItemStack ball = GachaBallItem.createWithItem(item.copyWithCount(1),random);
                    results.add(ball);
                    item.shrink(1);
                    if(item.isEmpty())
                        contentCopy.remove(x);
                    break;
                }
            }
            totalCount--;
        }
        this.randomizedContents = ImmutableList.copyOf(results);
    }

}
