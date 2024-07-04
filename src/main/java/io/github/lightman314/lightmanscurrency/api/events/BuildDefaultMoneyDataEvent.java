package io.github.lightman314.lightmanscurrency.api.events;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import net.neoforged.bus.api.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Event to get the default {@link ChainData} values for the <code>MasterCoinList.json</code><br>
 * Only called if the <code>MasterCoinList.json</code> config file is missing (not yet been created) or unreadable (json syntax error, corrupted file, etc.)<br>
 * Can be run multiple times per session.
 */
public final class BuildDefaultMoneyDataEvent extends Event {

    private static final List<CoinEntry> existingEntries = new ArrayList<>();

    /**
     * A list of existing entries currently being handled by this event.
     * Used to prevent the same item from being registered in multiple chains.
     */
    public static List<CoinEntry> getExistingEntries() { return existingEntries; }

    public BuildDefaultMoneyDataEvent() { existingEntries.clear(); }

    private final Map<String, ChainData.Builder> builders = new HashMap<>();

    /**
     * Returns a list of all added money data builders.<br>
     * Used internally to actually build the default values.<br>
     * Result is immutable and cannot be edited.
     */
    public Map<String,ChainData.Builder> getFinalResult() { return ImmutableMap.copyOf(this.builders); }

    /**
     * Returns the builder for the given chain if it's already been added by another mod through this event<br>
     * Make sure to check if said chain has an existing builder via {@link #exists(String)} 
     */
    @Nullable
    public ChainData.Builder getExistingBuilder(@Nonnull String chain) { return this.builders.get(chain); }

    /**
     * Whether a builder for the given chain has already been registered to this event.
     */
    public boolean exists(@Nonnull String chain) { return this.builders.containsKey(Objects.requireNonNull(chain)); }
    /**
     * Whether the given chain has not yet had any builders registered to this event.
     */
    public boolean available(@Nonnull String chain) { return !this.builders.containsKey(Objects.requireNonNull(chain)); }

    /**
     * Registers a data builder.<br>
     * Throws an {@link IllegalArgumentException} if a data builder has already been registered for that chain.<br>
     * Check {@link #available(String)} or {@link #exists(String)} to see if a given chain has already been registered.
     */
    public void addDefault(@Nonnull ChainData.Builder builder) { this.addDefault(builder, false); }

    /**
     * Registers a data builder.
     * Throws an {@link IllegalArgumentException} if a data builder has already been registered for that chain and <code>allowOverride</code> is false.
     * Check {@link #available(String)} or {@link #exists(String)} to see if a given chain has already been registered.
     */
    public void addDefault(@Nonnull ChainData.Builder builder, boolean allowOverride)
    {
        if(this.builders.containsKey(Objects.requireNonNull(builder).chain) && !allowOverride)
            throw new IllegalArgumentException("Builder already exists for Money Data chain '" + builder.chain + "'!");
        this.builders.put(builder.chain, builder);
    }

}
