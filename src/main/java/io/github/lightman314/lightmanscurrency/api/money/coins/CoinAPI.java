package io.github.lightman314.lightmanscurrency.api.money.coins;

import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.common.impl.CoinAPIImpl;
import io.github.lightman314.lightmanscurrency.network.message.data.SPacketSyncCoinData;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;

/**
 * Utility class with Coin-Related data and functions.
 * Use {@link MoneyAPI} for more generic Money-Related functions that aren't coin-specific.
 */
public abstract class CoinAPI {

    public static final CoinAPI API = CoinAPIImpl.INSTANCE;
    public static final Comparator<ItemStack> COIN_SORTER = CoinAPIImpl.SORTER;

    public static final String MONEY_FILE_LOCATION = "config/lightmanscurrency/MasterCoinList.json";
    public static final String MAIN_CHAIN = "main";

    @Deprecated(since = "2.2.0.4")
    public static boolean DataNotReady() { return API.NoDataAvailable(); }

    public abstract boolean NoDataAvailable();

    /**
     * Initializes the API. Should not be run by anyone else.
     */
    public abstract void Setup();

    /**
     * @deprecated Use {@link #ReloadCoinDataFromFile()} instead
     * @see #API
     */
    @Deprecated(since = "2.2.0.4")
    public static void reloadMoneyDataFromFile() { API.ReloadCoinDataFromFile(); }

    /**
     * Reloads the <code>MasterCoinList.json</code> config file, and sends it to all connected players.
     */
    public abstract void ReloadCoinDataFromFile();
    /**
     * @deprecated Use {@link #ReloadCoinDataFromFile()} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.0.4")
    @Nonnull
    public static ItemStack getWalletStack(@Nonnull Player player) { return API.getEquippedWallet(player); }

    @Nonnull
    public abstract ItemStack getEquippedWallet(@Nonnull Player player);

    /**
     * @deprecated Use {@link #ChainData(String)} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.0.4")
    @Nullable
    public static ChainData getChainData(@Nonnull String chain) { return API.ChainData(chain); }

    @Nullable
    public abstract ChainData ChainData(@Nonnull String chain);

    /**
     * @deprecated Use {@link #AllChainData()} instead.
     * @see #API
     */
    @Nonnull
    @Deprecated(since = "2.2.0.4")
    public static List<ChainData> getAllChainData() { return API.AllChainData(); }
    @Nonnull
    public abstract List<ChainData> AllChainData();


    /**
     * @deprecated Use {@link #ChainDataOfCoin(ItemStack)} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.0.4")
    @Nullable
    public static ChainData chainForCoin(@Nonnull ItemStack coin) { return API.ChainDataOfCoin(coin); }
    /**
     * Finds the {@link ChainData} that is responsible for the given coin.
     * Returns <code>null</code> of the given item is not a coin.
     */
    @Nullable
    public abstract ChainData ChainDataOfCoin(@Nonnull ItemStack coin);
    /**
     * @deprecated Use {@link #ChainDataOfCoin(ItemStack)} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.0.4")
    @Nullable
    public static ChainData chainForCoin(@Nonnull Item coin) { return API.ChainDataOfCoin(coin); }

    /**
     * Finds the {@link ChainData} that is responsible for the given coin.
     * Returns <code>null</code> of the given item is not a coin.
     */
    @Nullable
    public abstract ChainData ChainDataOfCoin(@Nonnull Item coin);

    /**
     * @deprecated Use {@link #IsCoin(ItemStack, boolean)} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.0.4")
    public static boolean isCoin(@Nonnull ItemStack coin, boolean allowSideChains) { return API.IsCoin(coin, allowSideChains); }

    /**
     * Whether the given item is a coin.
     * @param allowSideChains Whether coins from a side-chain will return a positive response or if coins from a side-chain will be ignored.
     */
    public abstract boolean IsCoin(@Nonnull ItemStack coin, boolean allowSideChains);

    /**
     * @deprecated Use {@link #IsCoin(Item, boolean)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.0.4")
    public static boolean isCoin(@Nonnull Item coin, boolean allowSideChains) { return API.IsCoin(coin, allowSideChains); }

    /**
     * Whether the given item is a coin.
     * @param allowSideChains Whether coins from a side-chain will return a positive response or if coins from a side-chain will be ignored.
     */
    public abstract boolean IsCoin(@Nonnull Item coin, boolean allowSideChains);

    /**
     * Allows you to add a filter allowing more items to be placed in Coin Containers such as wallets.<br>
     * @see #IsAllowedInCoinContainer(ItemStack, boolean)
     */
    public abstract void RegisterCoinContainerFilter(@Nonnull BiPredicate<ItemStack,Boolean> filter);

    /**
     * Whether the given item should be treated like a coin even though it's not registered as such in the MasterCoinList
     */
    public abstract boolean IsAllowedInCoinContainer(@Nonnull ItemStack coin, boolean allowSideChains);
    /**
     * Whether the given item should be treated like a coin even though it's not registered as such in the MasterCoinList
     */
    public abstract boolean IsAllowedInCoinContainer(@Nonnull Item coin, boolean allowSideChains);

    /**
     * @deprecated Use {@link #CoinExchangeAllUp(Container)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.0.4")
    public static void ExchangeAllCoinsUp(@Nonnull Container container) { API.CoinExchangeAllUp(container); }

    /**
     * Exchanges all coins in the container to the largest value coin possible
     */
    public abstract void CoinExchangeAllUp(@Nonnull Container container);

    /**
     * @deprecated Use {@link #CoinExchangeUp(Container, Item)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.0.4")
    public static void ExchangeCoinsUp(@Nonnull Container container, @Nonnull Item smallCoin) { API.CoinExchangeUp(container, smallCoin); }

    /**
     * Exchanges as many of the small coin that it can for its next largest coin
     */
    public abstract void CoinExchangeUp(@Nonnull Container container, @Nonnull Item smallCoin);

    /**
     * @deprecated Use {@link #CoinExchangeAllDown(Container)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.0.4")
    public static void ExchangeAllCoinsDown(@Nonnull Container container) { API.CoinExchangeAllDown(container); }

    /**
     * Exchanges the coins in the container into the smallest value possible that will fit in the containers space.
     */
    public abstract void CoinExchangeAllDown(@Nonnull Container container);

    /**
     * @deprecated Use {@link #CoinExchangeDown(Container,Item)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.0.4")
    public static void ExchangeCoinsDown(@Nonnull Container container, @Nonnull Item largeCoin) { API.CoinExchangeDown(container,largeCoin); }

    /**
     * Exchanges as many of the large coin into as many of the next smaller coin as it can
     */
    public abstract void CoinExchangeDown(@Nonnull Container container, @Nonnull Item largeCoin);

    /**
     * @deprecated Use {@link #SortCoinsByValue(Container)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.0.4")
    public static void SortCoins(@Nonnull Container container) { API.SortCoinsByValue(container); }

    public abstract void SortCoinsByValue(@Nonnull Container container);


    /**
     * @deprecated Use {@link #SyncCoinDataWith(PacketDistributor.PacketTarget)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.0.4")
    public static void syncDataWith(@Nonnull PacketDistributor.PacketTarget target) { API.SyncCoinDataWith(target); }

    /**
     * Sends a network packet to the given target to sync their coin data.<br>
     * Typically, shouldn't need to be called manually as this is done automatically when a player joins the server
     * or the data is reloaded via command.
     */
    public abstract void SyncCoinDataWith(@Nonnull PacketDistributor.PacketTarget target);

    /**
     * @deprecated Use {@link #HandleSyncPacket(SPacketSyncCoinData)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.0.4")
    public static void handleSyncPacket(@Nonnull SPacketSyncCoinData packet) { API.HandleSyncPacket(packet); }

    /**
     * Handles the coin data sync packet from the logical client.
     */
    public abstract void HandleSyncPacket(@Nonnull SPacketSyncCoinData packet);

    public abstract void RegisterCustomSorter(@Nonnull Comparator<ItemStack> sorter);

}
