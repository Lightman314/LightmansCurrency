package io.github.lightman314.lightmanscurrency.api.upgrades;

import javax.annotation.Nonnull;

public interface IUpgradeItem
{
    @Nonnull
    UpgradeType getUpgradeType();
    void setDefaultValues(@Nonnull UpgradeData.Mutable data);
    default void onApplied(@Nonnull IUpgradeable target) { }
}
