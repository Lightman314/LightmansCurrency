package io.github.lightman314.lightmanscurrency.api.events;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Event run when the <code>MasterCoinList.json</code> file is loaded.<br>
 * {@link Pre} is called before finalizing the results, and allows adding or removing any existing coin chains.<br>
 * {@link Post} is called after the results are finalized, and cannot be edited.
 */
public abstract class ChainDataReloadedEvent extends Event {

    /**
     * An uneditable copy of the chain data.
     */
    @Nonnull
    public abstract Map<String,ChainData> getChainMap();

    /**
     * Whether a chain exists with the given id.
     */
    public boolean chainExists(@Nonnull String chain) { return this.getChainMap().containsKey(chain); }

    /**
     * Gets the chain data with the given id.<br>
     * Returns {@code null} if no chain with that id is present.
     */
    @Nullable
    public ChainData getChain(@Nonnull String chain) { return this.getChainMap().get(chain); }

    /**
     * An uneditable list of the chain data.
     */
    @Nonnull
    public final List<ChainData> getChains() { return ImmutableList.copyOf(this.getChainMap().values()); }

    public static class Pre extends ChainDataReloadedEvent
    {

        private final Map<String,ChainData> dataMap;
        @Nonnull
        @Override
        public Map<String, ChainData> getChainMap() { return ImmutableMap.copyOf(this.dataMap); }

        public Pre(Map<String,ChainData> dataMap) { this.dataMap = new HashMap<>(dataMap); }

        /**
         * Adds the given chain to the map if no conflicting chains are present.<br>
         * Use {@link #addEntry(ChainData, boolean)} to override any conflicting chains.
         */
        public void addEntry(@Nonnull ChainData chain) { this.addEntry(chain, false); }

        /**
         * Adds the given chain to the map.
         * @param allowOverride Whether to override any conflicting chains that have the same chain id.
         */
        public void addEntry(@Nonnull ChainData chain, boolean allowOverride)
        {
            if(this.dataMap.containsKey(chain.chain) && !allowOverride)
                return;
            this.dataMap.put(chain.chain, chain);
        }

        /**
         * Removes the chain with the given id.
         */
        public void removeEntry(@Nonnull String chain) { this.dataMap.remove(chain); }

    }

    public static class Post extends ChainDataReloadedEvent
    {

        private final Map<String,ChainData> dataMap;
        @Nonnull
        @Override
        public Map<String, ChainData> getChainMap() { return this.dataMap; }

        public Post(@Nonnull Map<String,ChainData> dataMap) { this.dataMap = dataMap; }

    }

}
