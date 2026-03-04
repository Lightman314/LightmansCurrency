package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest;

import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;

public abstract class TickableCoinChestUpgrade extends CoinChestUpgrade {

    public abstract void OnServerTick(CoinChestBlockEntity be, CoinChestUpgradeData data);

    public int getTickFrequency() { return 20; }

}
