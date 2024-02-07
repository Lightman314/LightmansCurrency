package io.github.lightman314.lightmanscurrency.api.upgrades;

import javax.annotation.Nonnull;

public interface IUpgradeItem
{
    @Nonnull
    UpgradeType getUpgradeType();
    @Nonnull
    UpgradeData getDefaultUpgradeData();
    default void onApplied(@Nonnull IUpgradeable target) { }
}
