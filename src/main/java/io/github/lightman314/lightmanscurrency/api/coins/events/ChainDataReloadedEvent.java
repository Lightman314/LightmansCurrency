package io.github.lightman314.lightmanscurrency.api.coins.events;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.*;


/**
 * @see Server
 * @see Client
 */
public abstract class ChainDataReloadedEvent extends Event {

    @ApiStatus.Internal
    public ChainDataReloadedEvent() { }

    /**
     * Whether a chain exists with the given id.
     */
    public boolean chainExists(String chain) { return this.getChain(chain) != null; }

    /**
     * Gets the chain data with the given id.<br>
     * Returns {@code null} if no chain with that id is present.
     */
    @Nullable
    public ChainData getChain(String chain) { return LCApi.getCoinAPI().lookupChain(chain); }

    /**
     * An uneditable list of the chain data.
     */
    public Collection<ChainData> getChains() { return LCApi.getCoinAPI().lookupAllChains(); }

    /**
     * Event run when the coin data file(s) are loaded.<br>
     * This event is called after the results are finalized, and cannot be edited.
     * @see BuildDefaultCoinDataEvent BuildDefaultCoinDataEvent to modify the default chain data
     */
    public static final class Server extends Event {  }

    /**
     * Event run when the coin data file(s) are received from the server.<br>
     * This event is called after the results are finalized, and cannot be edited.
     */
    public static final class Client extends Event {  }

}