package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.upgrades.world.UpgradeStorage;

public interface IUpgradeListener {

    void afterUpgradesChanged(UpgradeStorage upgrades);

}
