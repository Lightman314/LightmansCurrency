package io.github.lightman314.lightmanscurrency.common.upgrades;

import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.SpeedUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.ItemCapacityUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestExchangeUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestMagnetUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestSecurityUpgrade;

public class Upgrades {

    public static final ItemCapacityUpgrade ITEM_CAPACITY = new ItemCapacityUpgrade();

    public static final SpeedUpgrade SPEED = new SpeedUpgrade();

    public static final UpgradeType.Simple NETWORK = new UpgradeType.Simple(EasyText.translatable("tooltip.lightmanscurrency.upgrade.network"));

    public static final UpgradeType.Simple HOPPER = new UpgradeType.Simple(EasyText.translatable("tooltip.lightmanscurrency.upgrade.hopper"));

    //Coin Chest Upgrades
    public static final CoinChestExchangeUpgrade COIN_CHEST_EXCHANGE = new CoinChestExchangeUpgrade();
    public static final CoinChestMagnetUpgrade COIN_CHEST_MAGNET = new CoinChestMagnetUpgrade();
    public static final CoinChestSecurityUpgrade COIN_CHEST_SECURITY = new CoinChestSecurityUpgrade();


}
