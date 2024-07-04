package io.github.lightman314.lightmanscurrency.api.money.coins;

import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.common.impl.CoinAPIImpl;
import io.github.lightman314.lightmanscurrency.network.message.data.SPacketSyncCoinData;
import io.github.lightman314.lightmanscurrency.network.packet.CustomPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Utility class with Coin-Related data and functions.
 * Use {@link MoneyAPI} for more generic Money-Related functions that aren't coin-specific.
 */
public abstract class CoinAPI {

    public static final CoinAPI API = CoinAPIImpl.INSTANCE;
    public static final Comparator<ItemStack> COIN_SORTER = CoinAPIImpl.SORTER;

    public static final String MONEY_FILE_LOCATION = "config/lightmanscurrency/MasterCoinList.json";
    public static final String MAIN_CHAIN = "main";

    public abstract boolean NoDataAvailable();

    /**
     * Initializes the API. Should not be run by anyone else.
     */
    public abstract void Setup();

    /**
     * Reloads the <code>MasterCoinList.json</code> config file, and sends it to all connected players.
     */
    public abstract void ReloadCoinDataFromFile();

    @Nonnull
    public abstract ItemStack getEquippedWallet(@Nonnull Player player);

    @Nullable
    public abstract ChainData ChainData(@Nonnull String chain);
    @Nonnull
    public abstract List<ChainData> AllChainData();

    /**
     * Finds the {@link ChainData} that is responsible for the given coin.
     * Returns <code>null</code> of the given item is not a coin.
     */
    @Nullable
    public abstract ChainData ChainDataOfCoin(@Nonnull ItemStack coin);

    /**
     * Finds the {@link ChainData} that is responsible for the given coin.
     * Returns <code>null</code> of the given item is not a coin.
     */
    @Nullable
    public abstract ChainData ChainDataOfCoin(@Nonnull Item coin);

    /**
     * Whether the given item is a coin.
     * @param allowSideChains Whether coins from a side-chain will return a positive response or if coins from a side-chain will be ignored.
     */
    public abstract boolean IsCoin(@Nonnull ItemStack coin, boolean allowSideChains);

    /**
     * Whether the given item is a coin.
     * @param allowSideChains Whether coins from a side-chain will return a positive response or if coins from a side-chain will be ignored.
     */
    public abstract boolean IsCoin(@Nonnull Item coin, boolean allowSideChains);

    /**
     * Exchanges all coins in the container to the largest value coin possible
     */
    public abstract void CoinExchangeAllUp(@Nonnull Container container);

    /**
     * Exchanges as many of the small coin that it can for its next largest coin
     */
    public abstract void CoinExchangeUp(@Nonnull Container container, @Nonnull Item smallCoin);

    /**
     * Exchanges the coins in the container into the smallest value possible that will fit in the containers space.
     */
    public abstract void CoinExchangeAllDown(@Nonnull Container container);

    /**
     * Exchanges as many of the large coin into as many of the next smaller coin as it can
     */
    public abstract void CoinExchangeDown(@Nonnull Container container, @Nonnull Item largeCoin);

    public abstract void SortCoinsByValue(@Nonnull Container container);

    @Nonnull
    public abstract CustomPacketPayload getSyncPacket();

    /**
     * Sends a network packet to the given target to sync their coin data.<br>
     * Typically, shouldn't need to be called manually as this is done automatically when a player joins the server
     * or the data is reloaded via command.<br>
     * If the target is null, the packet will be sent to all players
     */
    public abstract void SyncCoinDataWith(@Nullable Player player);

    /**
     * Handles the coin data sync packet from the logical client.
     */
    public abstract void HandleSyncPacket(@Nonnull SPacketSyncCoinData packet);

}
