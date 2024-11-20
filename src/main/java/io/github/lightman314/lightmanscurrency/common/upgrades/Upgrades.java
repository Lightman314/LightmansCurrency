package io.github.lightman314.lightmanscurrency.common.upgrades;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.SpeedUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.ItemCapacityUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.TradeOfferUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestBankUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestExchangeUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestMagnetUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestSecurityUpgrade;
import net.minecraft.ChatFormatting;

public class Upgrades {

    public static final TradeOfferUpgrade TRADE_OFFERS = new TradeOfferUpgrade();

    public static final ItemCapacityUpgrade ITEM_CAPACITY = new ItemCapacityUpgrade();

    public static final SpeedUpgrade SPEED = new SpeedUpgrade();

    public static final UpgradeType.Simple NETWORK = UpgradeType.Simple.builder()
            .unique()
            .tooltip(LCText.TOOLTIP_UPGRADE_NETWORK)
            .target(LCText.TOOLTIP_UPGRADE_TARGET_TRADER_NOT_NETWORK)
            .optionalTooltip(QuarantineAPI::IsDimensionQuarantined,LCText.TOOLTIP_DIMENSION_QUARANTINED_NETWORK_TRADER.getWithStyle(ChatFormatting.GOLD))
            .build();

    public static final UpgradeType.Simple VOID = UpgradeType.Simple.builder()
            .unique()
            .tooltip(LCText.TOOLTIP_UPGRADE_VOID)
            .target(LCText.TOOLTIP_UPGRADE_TARGET_TRADER_ITEM)
            .build();

    public static final UpgradeType.Simple HOPPER = UpgradeType.Simple.builder()
            .unique()
            .tooltip(LCText.TOOLTIP_UPGRADE_HOPPER)
            .target(LCText.TOOLTIP_UPGRADE_TARGET_TRADER_INTERFACE)
            .build();

    //Coin Chest Upgrades
    public static final CoinChestExchangeUpgrade COIN_CHEST_EXCHANGE = new CoinChestExchangeUpgrade();
    public static final CoinChestMagnetUpgrade COIN_CHEST_MAGNET = new CoinChestMagnetUpgrade();
    public static final CoinChestBankUpgrade COIN_CHEST_BANK = new CoinChestBankUpgrade();
    public static final CoinChestSecurityUpgrade COIN_CHEST_SECURITY = new CoinChestSecurityUpgrade();

}