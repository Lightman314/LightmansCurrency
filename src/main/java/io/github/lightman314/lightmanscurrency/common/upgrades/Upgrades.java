package io.github.lightman314.lightmanscurrency.common.upgrades;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.SpeedUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.ItemCapacityUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.TradeOfferUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestExchangeUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestMagnetUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestSecurityUpgrade;

public class Upgrades {

    public static final TradeOfferUpgrade TRADE_OFFERS = new TradeOfferUpgrade();

    public static final ItemCapacityUpgrade ITEM_CAPACITY = new ItemCapacityUpgrade();

    public static final SpeedUpgrade SPEED = new SpeedUpgrade();

    public static final UpgradeType.Simple NETWORK = new UpgradeType.Simple(true, LCText.TOOLTIP_UPGRADE_NETWORK.get()).withTarget(LCText.TOOLTIP_UPGRADE_TARGET_TRADER_NOT_NETWORK.get());

    public static final UpgradeType.Simple HOPPER = new UpgradeType.Simple(LCText.TOOLTIP_UPGRADE_HOPPER.get()).withTarget(LCText.TOOLTIP_UPGRADE_TARGET_TRADER_INTERFACE.get());

    //Coin Chest Upgrades
    public static final CoinChestExchangeUpgrade COIN_CHEST_EXCHANGE = new CoinChestExchangeUpgrade();
    public static final CoinChestMagnetUpgrade COIN_CHEST_MAGNET = new CoinChestMagnetUpgrade();
    public static final CoinChestSecurityUpgrade COIN_CHEST_SECURITY = new CoinChestSecurityUpgrade();

}
