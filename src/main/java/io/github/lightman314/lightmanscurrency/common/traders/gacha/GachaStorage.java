package io.github.lightman314.lightmanscurrency.common.traders.gacha;

import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GachaStorage {

    private final Supplier<Integer> maxStorage;
    public GachaStorage(Supplier<Integer> maxStorage) { this.maxStorage = maxStorage; }

    private final List<ItemStack> contents = new ArrayList<>();
    public List<ItemStack> getContents() { return this.contents; }
    public List<ItemStack> getSplitContents()
    {
        List<ItemStack> result = new ArrayList<>();
        for(ItemStack stack : InventoryUtil.copyList(this.contents))
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

    public ListTag save(HolderLookup.Provider lookup) {
        ListTag list = new ListTag();
        for(ItemStack item : this.contents)
        {
            if(item.isEmpty())
                continue;
            list.add(InventoryUtil.saveItemNoLimits(item,lookup));
        }
        return list;
    }

    public void load(ListTag list, HolderLookup.Provider lookup) {
        this.contents.clear();
        for(int i = 0; i < list.size(); ++i)
            this.contents.add(InventoryUtil.loadItemNoLimits(list.getCompound(i),lookup));
    }

    public JsonArray write(HolderLookup.Provider lookup) {
        JsonArray list = new JsonArray();
        for(ItemStack item : this.contents)
            list.add(FileUtil.convertItemStack(item,lookup));
        return list;
    }

    public void read(JsonArray list, HolderLookup.Provider lookup) throws JsonSyntaxException {
        this.contents.clear();
        for(int i = 0; i < list.size(); ++i)
            this.forceInsertItem(FileUtil.parseItemStack(GsonHelper.convertToJsonObject(list.get(i),"Storage[" + i + "]"),lookup));
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
            if(InventoryUtil.ItemMatches(entry,item))
            {
                int fittableAmount = Math.min(space,item.getCount());
                entry.grow(fittableAmount);
                item.shrink(fittableAmount);
                return true;
            }
        }
        //Not found in existing stack, so we'll add a new entry to the list
        this.contents.add(item.split(space));
        return true;
    }

    public void forceInsertItem(ItemStack item)
    {
        for(ItemStack entry : this.contents)
        {
            if(InventoryUtil.ItemMatches(entry,item))
            {
                entry.grow(item.getCount());
                return;
            }
        }
        //Not found in existing stack, so we'll add a new entry to the list
        this.contents.add(item.copy());
    }

    public ItemStack removeItem(int slot, int count) {
        if(slot < 0 || slot >= this.contents.size())
            return ItemStack.EMPTY;
        ItemStack item = this.contents.get(slot);
        ItemStack result = item.split(count);
        if(item.isEmpty())
            this.contents.remove(slot);
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
                }
                return result;
            }
        }
        LightmansCurrency.LogWarning("Somehow randomly generated nothing");
        return ItemStack.EMPTY;
    }

    public List<ItemStack> peekRandomItems(RandomSource random, int count)
    {
        if(this.contents.isEmpty())
            return new ArrayList<>();
        List<ItemStack> result = new ArrayList<>();
        //Copy local inventory to ensure that we don't display duplicate items
        List<ItemStack> contentCopy = InventoryUtil.copyList(this.contents);
        int totalCount = this.getItemCount();
        for(int i = 0; i < count; ++i)
        {
            int rand = random.nextInt(totalCount);
            for(int x = 0; x < contentCopy.size(); ++x)
            {
                ItemStack item = contentCopy.get(x);
                rand -= item.getCount();
                if(rand < 0)
                {
                    result.add(item.copyWithCount(1));
                    item.shrink(1);
                    if(item.isEmpty())
                    {
                        contentCopy.remove(x);
                        if(contentCopy.isEmpty())
                            return result;
                    }
                    totalCount--;
                    break;
                }
            }
        }
        return result;
    }

}
