package io.github.lightman314.lightmanscurrency.api.coins;

import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.BiPredicate;

public interface CoinAPI {

    String DEFAULT_CHAIN = "main";
    String DATA_LOCATION = "config/lightmanscurrency/coin_data";

    /**
     * Whether the coin data has been loaded from file
     */
    boolean isDataLoaded();
    /**
     * Whether the coin data has <b>NOT</b> yet been loaded from file
     */
    default boolean dataNotLoaded() { return !this.isDataLoaded(); }

    /**
     * Reloads the coin data config files, and sends it to all connected players
     */
    default void reloadCoinData(HolderLookup.Provider registryAccess) { this.reloadCoinData(registryAccess,true); }
    /**
     * Reloads the coin data config files
     * @param sync Whether we should send the sync packet to all connected players
     */
    void reloadCoinData(HolderLookup.Provider registryAccess,boolean sync);


    ItemStack getEquippedWallet(Entity player);

    Collection<ChainData> lookupAllChains();

    /**
     * Attempts to obtain the {@link ChainData} for the given chain ID
     */
    @Nullable
    ChainData lookupChain(String chain);

    /**
     * Attempts to obtain the {@link ChainData} for the given coin item
     */
    @Nullable
    default ChainData lookupChain(ItemInstance coin) { return this.lookupChain(coin.typeHolder().value()); }
    /**
     * Attempts to obtain the {@link ChainData} for the given coin item
     */
    default ChainData lookupChain(ItemResource coin) { return this.lookupChain(coin.getItem()); }
    /**
     * Attempts to obtain the {@link ChainData} for the given coin item
     */
    @Nullable
    ChainData lookupChain(Item coin);

    /**
     * Whether the given item is a coin.
     * @param allowSideChains Whether coins from a side-chain will return a positive response, or if coins from a side-chain will be ignored
     */
    default boolean isCoin(ItemStack coin,boolean allowSideChains) { return this.isCoin(coin.typeHolder().value(),allowSideChains); }
    /**
     * Whether the given item is a coin.
     * @param allowSideChains Whether coins from a side-chain will return a positive response, or if coins from a side-chain will be ignored
     */
    boolean isCoin(Item coin,boolean allowSideChains);

    /**
     * Allows you to add a filter allowing more items to be placed in Coin Containers such as wallets.
     * @see #isAllowedInCoinContainer(ItemStack, boolean)
     * @see #isAllowedInCoinContainer(Item, boolean)
     */
    void registerCoinContainerFilter(BiPredicate<ItemStack,Boolean> filter);

    /**
     * Whether the given item should be allowed in Coin Containers such as wallets.<br>
     * May differ from {@link #isCoin(ItemStack, boolean)} if a custom coin container filter was registered for this item.
     * @see #registerCoinContainerFilter(BiPredicate)
     */
    boolean isAllowedInCoinContainer(ItemStack coin,boolean allowSideChains);
    /**
     * Whether the given item should be allowed in Coin Containers such as wallets.<br>
     * May differ from {@link #isCoin(Item,boolean)} if a custom coin container filter was registered for this item.
     * @see #registerCoinContainerFilter(BiPredicate)
     */
    default boolean isAllowedInCoinContainer(Item coin,boolean allowSideChains) { return this.isAllowedInCoinContainer(new ItemStack(coin),allowSideChains); }


    // Coin Exchange Methods
    /**
     * Exchanges all coins in the container to the largest value coin possible.
     */
    void exchangeCoinsAllUp(ResourceHandler<ItemResource> container,@Nullable Transaction transaction);
    /**
     * Exchanges the small coin into as many of its next largest coin that will fit in the containers space.
     */
    boolean exchangeCoinsUp(ResourceHandler<ItemResource> container,Item smallCoin,@Nullable Transaction transaction);

    /**
     * Exchanges the coin in the container into the smallest value possible that will fit in the containers space.
     */
    void exchangeCoinsAllDown(ResourceHandler<ItemResource> container,@Nullable Transaction transaction);

    /**
     * Exchanges the large coin into as many of the next smallest value that will fit into the containers space.
     */
    boolean exchangeCoinsDown(ResourceHandler<ItemResource> container,Item largeCoin,@Nullable Transaction transaction);

    /**
     * Obtains a {@link Comparator} that can properly sort coin items
     */
    Comparator<ItemStack> getCoinSorter();

    /**
     * Registers a custom coin sorter that will be merged into the {@link #getCoinSorter()} sorter.
     */
    void registerCustomSorter(Comparator<ItemStack> sorter);

    /**
     * Sorts the coins within the container using the {@link #getCoinSorter()} sorter.
     * @see #registerCustomSorter(Comparator)
     */
    void sortCoinsByValue(ResourceHandler<ItemResource> container,@Nullable Transaction transaction);

}