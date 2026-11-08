package io.github.lightman314.lightmanscurrency.features.trader.item.requirements;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class StackMatchRequirement extends ItemRequirement.SingleResult {

    private final ItemResource resource;
    public StackMatchRequirement(ItemStack stack,int count) { this(ItemResource.of(stack),count); }
    public StackMatchRequirement(ItemResource resource,int count) {
        super(count);
        this.resource = resource;
    }

    @Override
    public ItemRequirement copy() { return new StackMatchRequirement(this.resource,this.getCount()); }

    @Override
    protected ItemResource getDummyResource() { return this.resource; }

    @Override
    public boolean canMerge(ItemRequirement other) { return other instanceof StackMatchRequirement smr && this.resource.equals(smr.resource); }
    @Override
    public boolean test(ItemResource resource) { return resource.equals(this.resource); }

}
