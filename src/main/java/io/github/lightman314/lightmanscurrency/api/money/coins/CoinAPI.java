package io.github.lightman314.lightmanscurrency.api.money.coins;

import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.common.impl.CoinAPIImpl;
import io.github.lightman314.lightmanscurrency.network.message.data.SPacketSyncCoinData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.BiPredicate;

/**
 * Utility class with Coin-Related data and functions.
 * Use {@link MoneyAPI} for more generic Money-Related functions that aren't coin-specific.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class CoinAPI {

    private static CoinAPI instance;
    public static CoinAPI getApi()
    {
        if(instance == null)
            instance = new CoinAPIImpl();
        return instance;
    }
    public static final Comparator<ItemStack> COIN_SORTER = new CoinAPIImpl.CoinSorter();

    public static final String MONEY_FILE_LOCATION = "config/lightmanscurrency/MasterCoinList.json";
    public static final String MAIN_CHAIN = "main";

    protected CoinAPI() { if(instance != null)  throw new IllegalCallerException("Cannot create a new CoinAPI instance as one is already present!"); }

    public abstract boolean NoDataAvailable();

    /**
     * Initializes the API. Should not be run by anyone else.
     */
    public abstract void Setup();

    /**
     * Reloads the <code>MasterCoinList.json</code> config file, and sends it to all connected players.
     */
    public abstract void ReloadCoinDataFromFile();

    public abstract ItemStack getEquippedWallet(Player player);

    @Nullable
    public abstract ChainData ChainData(String chain);
    
    public abstract List<ChainData> AllChainData();

    /**
     * Finds the {@link ChainData} that is responsible for the given coin.
     * Returns <code>null</code> of the given item is not a coin.
     */
    @Nullable
    public abstract ChainData ChainDataOfCoin(ItemStack coin);

    /**
     * Finds the {@link ChainData} that is responsible for the given coin.
     * Returns <code>null</code> of the given item is not a coin.
     */
    @Nullable
    public abstract ChainData ChainDataOfCoin(Item coin);

    /**
     * Whether the given item is a coin.
     * @param allowSideChains Whether coins from a side-chain will return a positive response or if coins from a side-chain will be ignored.
     */
    public abstract boolean IsCoin(ItemStack coin, boolean allowSideChains);

    /**
     * Whether the given item is a coin.
     * @param allowSideChains Whether coins from a side-chain will return a positive response or if coins from a side-chain will be ignored.
     */
    public abstract boolean IsCoin(Item coin, boolean allowSideChains);

    /**
     * Allows you to add a filter allowing more items to be placed in Coin Containers such as wallets.<br>
     * @see #IsAllowedInCoinContainer(ItemStack, boolean)
     */
    public abstract void RegisterCoinContainerFilter(BiPredicate<ItemStack,Boolean> filter);

    /**
     * Whether the given item should be treated like a coin even though it's not registered as such in the MasterCoinList
     */
    public abstract boolean IsAllowedInCoinContainer(ItemStack coin, boolean allowSideChains);
    /**
     * Whether the given item should be treated like a coin even though it's not registered as such in the MasterCoinList
     */
    public abstract boolean IsAllowedInCoinContainer(Item coin, boolean allowSideChains);

    /**
     * Exchanges all coins in the container to the largest value coin possible
     */
    public abstract void CoinExchangeAllUp(Container container);

    /**
     * Exchanges as many of the small coin that it can for its next largest coin
     */
    public abstract void CoinExchangeUp(Container container, Item smallCoin);

    /**
     * Exchanges the coins in the container into the smallest value possible that will fit in the containers space.
     */
    public abstract void CoinExchangeAllDown(Container container);

    /**
     * Exchanges as many of the large coin into as many of the next smaller coin as it can
     */
    public abstract void CoinExchangeDown(Container container, Item largeCoin);

    public abstract void SortCoinsByValue(Container container);

    /**
     * Sends a network packet to the given target to sync their coin data.<br>
     * Typically, shouldn't need to be called manually as this is done automatically when a player joins the server
     * or the data is reloaded via command.
     */
    public abstract void SyncCoinDataWith(PacketDistributor.PacketTarget target);

    /**
     * Handles the coin data sync packet from the logical client.
     */
    public abstract void HandleSyncPacket(SPacketSyncCoinData packet);

    public abstract void RegisterCustomSorter(Comparator<ItemStack> sorter);

}
