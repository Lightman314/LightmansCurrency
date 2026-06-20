package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;

public interface IUpgradeUser {

    boolean allowUpgrade(UpgradeType type);

}