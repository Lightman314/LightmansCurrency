package io.github.lightman314.lightmanscurrency;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class CustomCreativeTab extends CreativeModeTab {

    private final ItemSorter itemSorter;

    Supplier<ItemLike> iconItem;

    public CustomCreativeTab(String label, Supplier<ItemLike> iconItem)
    {
        super(label);
        this.iconItem = iconItem;
        this.itemSorter = new ItemSorter();
    }

    @Override
    public @NotNull ItemStack makeIcon()
    {
        if(this.iconItem != null)
            return new ItemStack(this.iconItem.get());
        return ItemStack.EMPTY;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillItemList(@NotNull NonNullList<ItemStack> items) {

        super.fillItemList(items);

        // Sort the item list using the ItemSorter instance
        items.sort(itemSorter);

    }

    public final ItemSorterBuilder startInit() { return new ItemSorterBuilder(this.itemSorter); }


    public void addToSortingList(List<ItemLike> extras)
    {
        this.itemSorter.addToSortingList(extras);
    }

    /**
     * Initializes the sorting list of the item group. Should be called in the FMLCommonSetupEvent.
     */
    @Deprecated
    public void initSortingList(List<ItemLike> defaultList)
    {
        this.itemSorter.initSortingList(defaultList);
    }


    /**
     * Initializes the sorting list of the item group. Should be called in the FMLCommonSetupEvent.
     */
    @Deprecated
    public void initSortingList2(List<RegistryObject<? extends ItemLike>> defaultList)
    {
        List<ItemLike> list = new ArrayList<>();
        for(RegistryObject<? extends ItemLike> item : defaultList)
        {
            ItemLike i = item.get();
            if(i != null)
                list.add(i);
        }
        this.itemSorter.initSortingList(list);
    }

    public static final class ItemSorterBuilder {

        private final ItemSorter sorter;
        private final ArrayList<Item> sortList = new ArrayList<>();
        private ItemSorterBuilder(ItemSorter sorter) { this.sorter = sorter; }

        public ItemSorterBuilder add(Object... items) {
            for(Object object : items)
                this.addItem(object);
            return this;
        }

        private void addItem(Object object) {
            if(object instanceof ItemLike item)
                this.addItem(item.asItem());
            else if(object instanceof RegistryObject<?> registryObject)
                this.addItem(registryObject.get());
            else if(object instanceof RegistryObjectBundle<?,?> bundle)
            {
                for(Object obj : bundle.getAllSorted())
                    this.addItem(obj);
            }
            else if(object instanceof RegistryObjectBiBundle<?,?,?> bundle)
            {
                for(Object obj : bundle.getAllSorted())
                    this.addItem(obj);
            }
            else if(object instanceof List<?> list)
            {
                for(Object obj : list)
                    this.addItem(obj);
            }
            else
                LightmansCurrency.LogWarning("Could not parse '" + object.getClass().getName() + "' as an item for the sorting list.");
        }

        private void addItem(Item item) {
            if(!this.sortList.contains(item))
                this.sortList.add(item);
        }

        public void build() { this.sorter.initSortingList(this); }

    }



    private static class ItemSorter implements Comparator<ItemStack>
    {

        public ItemSorter() {}

        private ArrayList<Item> sortList = null;

        private ArrayList<Item> convertList(List<ItemLike> sourceList)
        {
            ArrayList<Item> list = Lists.newArrayList();
            for (ItemLike itemLike : sourceList) {
                list.add(itemLike.asItem());
            }
            return list;
        }

        @Deprecated
        public void initSortingList(List<ItemLike> sortList)
        {
            if(this.sortList == null)
                this.sortList = this.convertList(sortList);
            else
            {
                List<Item> copyList = this.sortList;
                this.sortList = this.convertList(sortList);
                this.sortList.addAll(copyList);
            }
        }

        public void initSortingList(ItemSorterBuilder builder) {
            if(this.sortList == null)
                this.sortList = new ArrayList<>(builder.sortList);
            else
            {
                List<Item> copyList = this.sortList;
                this.sortList = new ArrayList<>(builder.sortList);
                this.sortList.addAll(copyList);
            }
        }

        public void addToSortingList(List<ItemLike> extras)
        {
            if(this.sortList == null)
            {
                //LightmansCurrency.LogWarning("Sorting list has not been initialized. Adding temporarily, until the official init arrives.");
                this.sortList = this.convertList(extras);
                return;
            }
            for (ItemLike extra : extras) {
                this.sortList.add(extra.asItem());
            }
            LightmansCurrency.LogInfo("Added " + extras.size() + " items to the creative tab sorting list.");
        }

        @Override
        public int compare(ItemStack o1, ItemStack o2) {

            Item item1 = o1.getItem();
            Item item2 = o2.getItem();

            if(this.sortList == null)
            {
                LightmansCurrency.LogWarning("No sortlist defined for this CurrencyGroup.");
                return 0;
            }

            //If item1 is on the sort list and item2 isn't, sort item1 before item2
            if(sortList.contains(item1) && !sortList.contains(item2))
            {
                return -1;
            }

            //If item2 is on the sort list and item1 isn't, sort item1 before item2
            if(!sortList.contains(item1) && sortList.contains(item2))
            {
                return 1;
            }

            //If both items are on the sort list, sort by index
            if(sortListContains(item1) && sortListContains(item2))
            {
                return Integer.compare(this.indexOf(item1), this.indexOf(item2));
            }

            //No other sort method found, do nothing.
            return 0;

        }

        private boolean sortListContains(Item item)
        {
            return indexOf(item) >= 0;
        }

        private int indexOf(Item item) { return this.sortList.indexOf(item); }

    }

}