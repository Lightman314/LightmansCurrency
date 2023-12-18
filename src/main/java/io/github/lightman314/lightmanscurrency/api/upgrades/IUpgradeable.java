package io.github.lightman314.lightmanscurrency.api.upgrades;

import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;

import javax.annotation.Nonnull;

public interface IUpgradeable
{
    default boolean allowUpgrade(@Nonnull UpgradeItem item) { return this.allowUpgrade(item.getUpgradeType()); }
    boolean allowUpgrade(@Nonnull UpgradeType type);
}
