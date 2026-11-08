package io.github.lightman314.lightmanscurrency.features.trader.item.requirements;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class ItemMatchRequirement extends ItemRequirement.SingleResult {

    private final Item item;
    public ItemMatchRequirement(Item item,int count) {
        super(count);
        this.item = item;
    }

    @Override
    public ItemRequirement copy() { return new ItemMatchRequirement(this.item,this.getCount()); }

    @Override
    protected ItemResource getDummyResource() { return ItemResource.of(this.item); }

    @Override
    public boolean canMerge(ItemRequirement other) { return other instanceof ItemMatchRequirement imr && imr.item == this.item; }

    @Override
    public boolean test(ItemResource resource) { return resource.is(this.item); }

}
