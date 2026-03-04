package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeStackHandler;

public interface IUpgradeHandler {

    boolean allowUpgrade(UpgradeType upgrade);

    default void afterUpgradesChanged(UpgradeStackHandler container) {}

}
