package io.github.lightman314.lightmanscurrency.api.upgrades;

import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public interface IUpgradeable
{
    default boolean allowUpgrade(@Nonnull UpgradeItem item) { return this.allowUpgrade(item.getUpgradeType()); }
    boolean allowUpgrade(@Nonnull UpgradeType type);
    @Nonnull
    Container getUpgrades();
    default boolean quickInsertUpgrade(@Nonnull ItemStack stack)
    {
        if(stack.getItem() instanceof UpgradeItem upgrade)
        {
            Container upgradeContainer = this.getUpgrades();
            if(this.allowUpgrade(upgrade) && UpgradeItem.noUniqueConflicts(upgrade,upgradeContainer))
            {
                ItemStack insertItem = stack.copyWithCount(1);
                for(int i = 0; i < upgradeContainer.getContainerSize(); ++i)
                {
                    if(upgradeContainer.getItem(i).isEmpty())
                    {
                        upgradeContainer.setItem(i,insertItem);
                        stack.shrink(1);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
