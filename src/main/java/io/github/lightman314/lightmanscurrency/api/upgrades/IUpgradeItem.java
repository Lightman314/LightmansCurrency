package io.github.lightman314.lightmanscurrency.api.upgrades;

public interface IUpgradeItem
{
    UpgradeType getUpgradeType();
    void setDefaultValues(UpgradeData.Mutable data);
    default void onApplied(IUpgradeable target) { }
}
