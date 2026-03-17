package io.github.lightman314.lightmanscurrency.util;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Methods should only be used to load old data written before the change to codecs
 */
@Deprecated
public class OldDataHelper {

    public static SimpleContainer buildInventory(List<ItemStack> list)
    {
        SimpleContainer inventory = new SimpleContainer(list.size());
        for(int i = 0; i < list.size(); i++)
        {
            inventory.setItem(i, list.get(i).copy());
        }
        return inventory;
    }

    public static NonNullList<ItemStack> buildList(Container inventory)
    {
        NonNullList<ItemStack> list = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        for(int i = 0; i < inventory.getContainerSize(); i++)
        {
            list.set(i, inventory.getItem(i).copy());
        }
        return list;
    }

    public static void loadAllItems(String key, CompoundTag tag, NonNullList<ItemStack> list, HolderLookup.Provider lookup)
    {
        ListTag listTag = tag.getList(key, Tag.TAG_COMPOUND);
        DataContext<Tag> context = DataContext.createNBT(lookup);
        for(int i = 0; i < listTag.size(); i++)
        {
            CompoundTag slotCompound = listTag.getCompound(i);
            int index = slotCompound.getByte("Slot") & 255;
            if(index < list.size())
            {
                ItemStack stack = context.read(slotCompound, CodecHelper.UNLIMITED_ITEM_OPTIONAL);
                list.set(index, stack);
            }
        }
    }

    public static NonNullList<ItemStack> loadListOfSize(String key, CompoundTag tag, int size, HolderLookup.Provider lookup)
    {
        NonNullList<ItemStack> list = NonNullList.withSize(size,ItemStack.EMPTY);
        loadAllItems(key,tag,list,lookup);
        return list;
    }

    public static SimpleContainer loadContainer(String key, CompoundTag tag, int size, HolderLookup.Provider lookup)
    {
        return buildInventory(loadListOfSize(key,tag,size,lookup));
    }

    public static NonNullList<ItemStack> loadNonEmptyList(ListTag listTag,HolderLookup.Provider lookup)
    {
        NonNullList<ItemStack> list = loadList(listTag,lookup);
        list.removeIf(ItemStack::isEmpty);
        return list;
    }

    public static NonNullList<ItemStack> loadList(ListTag listTag, HolderLookup.Provider lookup)
    {
        DataContext<Tag> context = DataContext.createNBT(lookup);
        NonNullList<ItemStack> list = NonNullList.of(ItemStack.EMPTY);
        for(Tag tag : listTag)
            list.add(context.read(tag,CodecHelper.UNLIMITED_ITEM_OPTIONAL));
        return list;
    }

    public static ItemStack loadItem(CompoundTag tag,HolderLookup.Provider lookup)
    {
        return DataContext.createNBT(lookup).read(tag,CodecHelper.UNLIMITED_ITEM_OPTIONAL);
    }

    @Nullable
    public static <C> C decode(Tag data, Codec<C> codec, HolderLookup.Provider lookup) { return DataContext.createNBT(lookup).read(data,codec); }

}
