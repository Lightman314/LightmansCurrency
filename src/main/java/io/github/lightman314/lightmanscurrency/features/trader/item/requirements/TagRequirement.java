package io.github.lightman314.lightmanscurrency.features.trader.item.requirements;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.List;

public class TagRequirement extends ItemRequirement.MultiResult {

    private final TagKey<Item> tag;
    public TagRequirement(TagKey<Item> tag,int count) {
        super(count);
        this.tag = tag;
    }

    @Override
    public ItemRequirement copy() { return new TagRequirement(this.tag,this.getCount()); }

    @Override
    protected List<Holder<Item>> getPossibleResults() {
        List<Holder<Item>> list = new ArrayList<>();
        for(var h : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag))
            list.add(h);
        return list;
    }

    @Override
    public boolean canMerge(ItemRequirement other) { return other instanceof TagRequirement tr && tr.tag.equals(this.tag); }

    @Override
    public boolean test(ItemResource resource) { return resource.is(this.tag); }

}