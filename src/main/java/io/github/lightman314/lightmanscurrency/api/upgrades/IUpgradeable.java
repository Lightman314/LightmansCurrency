package io.github.lightman314.lightmanscurrency.api.upgrades;

import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public interface IUpgradeable
{
    default boolean allowUpgrade(UpgradeItem item) { return this.allowUpgrade(item.getUpgradeType()); }
    boolean allowUpgrade(UpgradeType type);

    UpgradeStackHandler getUpgrades();
    default boolean quickInsertUpgrade(ItemStack stack)
    {
        if(stack.getItem() instanceof UpgradeItem upgrade)
        {
            int startingCount = stack.getCount();
            UpgradeStackHandler upgradeContainer = this.getUpgrades();
            ItemStack result = ItemHandlerHelper.insertItem(upgradeContainer,stack,false);
            return result.getCount() < startingCount;
        }
        return false;
    }
}
