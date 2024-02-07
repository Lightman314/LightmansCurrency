package io.github.lightman314.lightmanscurrency.api.traders.menu;

/**
 * Interface used by the {@link io.github.lightman314.lightmanscurrency.network.message.trader.CPacketCollectCoins} packet to trigger money collection via button press.
 */
public interface IMoneyCollectionMenu {
    void CollectStoredMoney();
}
