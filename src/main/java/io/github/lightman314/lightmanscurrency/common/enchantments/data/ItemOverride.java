package io.github.lightman314.lightmanscurrency.common.enchantments.data;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ItemOverride {

    public final MoneyValue baseCost;
    private final List<ResourceLocation> items;
    private final List<TagKey<Item>> tags;
    public ItemOverride(MoneyValue baseCost, List<String> inputs)
    {
        this.baseCost = baseCost;
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

    public List<String> writeList()
    {
        List<String> list = new ArrayList<>();
        for(TagKey<Item> tag : this.tags)
            list.add("#" + tag.location());
        for(ResourceLocation item : this.items)
            list.add(item.toString());
        return list;
    }

    public boolean matches(@Nonnull ItemStack item) {
        return this.items.contains(ForgeRegistries.ITEMS.getKey(item.getItem())) || this.tags.stream().anyMatch(t -> InventoryUtil.ItemHasTag(item,t));
    }

}
