package io.github.lightman314.lightmanscurrency.common.menus.containers;

import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeable;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class UpgradeContainer extends SimpleContainer {

    private final IUpgradeable parent;
    public UpgradeContainer(int size, IUpgradeable parent) { super(size); this.parent = parent; }

    public void copyContents(Container container)
    {
        this.clearContent();
        for(int i = 0; i < this.getContainerSize() && i < container.getContainerSize(); ++i)
            this.setItem(i,container.getItem(i).copy());
    }

    protected final boolean validUpgrade(ItemStack stack) { return stack.getItem() instanceof UpgradeItem upgrade && this.parent.allowUpgrade(upgrade) && UpgradeItem.noUniqueConflicts(upgrade, this); }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) { return this.validUpgrade(stack); }

    @Override
    public int getMaxStackSize() { return 1; }

    @Override
    public ItemStack addItem(ItemStack stack) {
        if(this.canPlaceItem(0,stack))
            return super.addItem(stack);
        return stack;
    }

    @Override
    public boolean canAddItem(ItemStack stack) { return this.validUpgrade(stack) && super.canAddItem(stack); }

    public void save(String key, CompoundTag tag) { InventoryUtil.saveAllItems(key,tag,this); }
    public void load(String key, CompoundTag tag)
    {
        if(tag.contains(key))
            this.copyContents(InventoryUtil.loadAllItems(key,tag,this.getContainerSize()));
    }

}