package io.github.lightman314.lightmanscurrency.api.trader.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.IRegistryAccess;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerHolder;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnable;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.*;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.*;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.event.TraderEvent;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionType;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.TrackingLevel;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.TraderTrackingData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeFailedException;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceCollector;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.TraderDataCache;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.*;

public final class TraderData extends IRegistryAccess.Holder implements ISidedContext.Mutable<TraderData>, IOwnable, TraderSource.Simple, INodeAccess {

    //Constants
    public static int GLOBAL_TRADE_LIMIT = 100;

    //Codec
    public static Codec<TraderData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            LCRegistries.Trader.TRADER_TYPES.byNameCodec().fieldOf("type").forGetter(TraderData::getType),
            Codec.LONG.fieldOf("id").forGetter(TraderData::getID),
            TraderNode.MAP_CODEC.fieldOf("nodes").forGetter(TraderData::getWritableNodes)
    ).apply(builder,TraderData::new));

    //Trader Source Implementation
    @Override
    @Nullable
    public TraderData getSimpleTrader() { return this; }

    //Sided Context implementation
    private ISidedContext sidedContext = ISidedContext.LOGICAL_SERVER;
    @Override
    public TraderData setSidedContext(ISidedContext context) { this.sidedContext = context; return this; }
    @Override
    public boolean isClient() { return this.sidedContext.isClient(); }

    //Tracking & Syncing
    private TraderTrackingData trackingData = new TraderTrackingData();

    //Set of changed trader nodes that need to send sync packets
    private final Set<TraderNodeType<?>> changedNodes = new HashSet<>();
    //Set of players who have ended their tracking of this trader recently
    //Will be cleared when the next sync packet is sent
    //If a new tracking request occurs before this is cleared an initialization packet won't be sent
    private final Map<TrackingLevel,Set<UUID>> removedPlayerTracking = new HashMap<>();
    private Set<UUID> getRemovedPlayerSet(TrackingLevel level)
    {
        if(level == TrackingLevel.NONE)
            return new HashSet<>();
        return this.removedPlayerTracking.computeIfAbsent(level,l -> new HashSet<>());
    }

    /**
     * Used to copy tracking data for persistent trader data after they are reloaded from file
     * @param other The old trader that the tracking data should be copied from
     */
    @ApiStatus.Internal
    public void copyTrackingData(TraderData other) { this.trackingData = other.trackingData; }

    /**
     * Informs the trader that the given player is now tracking this trader at the given tracking level
     * @param player The player who is now tracking the trader
     * @param level The level of information they need to access
     * @return The "key" to use in {@link #endTracking(Player,long)} to end the tracking request.
     */
    public long requestTracking(Player player, TrackingLevel level) {
        if(this.isClient() || level == TrackingLevel.NONE)
            return -1;
        TrackingLevel oldLevel = this.trackingData.getLevel(player);
        TraderTrackingData.Result result = this.trackingData.requestTracking(player,level);
        if(level.ordinal() > oldLevel.ordinal())
        {
            //Don't send a fresh packet if we only just removed them from this level
            Set<UUID> set = this.getRemovedPlayerSet(level);
            if(set.contains(player.getUUID()))
                set.remove(player.getUUID());
            else
                TraderDataCache.TYPE.get(ISidedContext.LOGICAL_SERVER).sendTraderPacket(player,this,this.fullSyncPacket(this.trackingData.getContext(player),oldLevel,result.changedNodes()));
            ISyncingContext context = this.trackingData.getContext(player);
            for(ISyncingNode node : this.getNodes(ISyncingNode.class))
                node.afterTrackingChange(context,oldLevel);
            return result.key();
        }
        return result.key();
    }

    public long requestSpecialTracking(Player player,TrackingLevel level,UUID target)
    {
        if(this.isClient() || level == TrackingLevel.NONE)
            return -1;
        TrackingLevel oldLevel = this.trackingData.getSpecialLevel(player,target);
        long result = this.trackingData.requestSpecialTracking(player,level,target);
        if(level.ordinal() > oldLevel.ordinal())
        {
            ISyncingContext context = this.trackingData.getSpecialContext(player,target);
            //Send a fresh packet with **exclusively** the targeted trader info
            TraderDataCache.TYPE.get(ISidedContext.LOGICAL_SERVER).sendTraderPacket(player,this,this.fullSyncPacket(context,oldLevel));
            for(ISyncingNode node : this.getNodes(ISyncingNode.class))
                node.afterTrackingChange(context,oldLevel);
        }
        return result;
    }

    public void endTracking(Player player,long key)
    {
        if(this.isClient())
            return;
        TrackingLevel oldLevel = this.trackingData.getLevel(player);
        boolean wasSpecial = this.trackingData.endTracking(player,key);
        if(wasSpecial)
            return; //Don't perform any special actions when disabling special tracking
        TrackingLevel newLevel = this.trackingData.getLevel(player);
        if(newLevel.ordinal() < oldLevel.ordinal())
        {
            Set<UUID> set = this.getRemovedPlayerSet(oldLevel);
            set.add(player.getUUID());
            ISyncingContext context = this.trackingData.getContext(player);
            for(ISyncingNode node : this.getNodes(ISyncingNode.class))
                node.afterTrackingChange(context,oldLevel);
        }
    }

    @ApiStatus.Internal
    public void onPlayerLeave(Player player)
    {
        this.trackingData.clearPlayer(player.getUUID());
        ISyncingContext context = new ISyncingContext.Simple(player);
        for(ISyncingNode node : this.getNodes(ISyncingNode.class))
            node.afterTrackingEnded(context);
    }

    public FancyPacketMap fullSyncPacket(Player player) { return this.fullSyncPacket(this.trackingData.getContext(player)); }
    public FancyPacketMap fullSyncPacket(ISyncingContext context) { return this.fullSyncPacket(context,TrackingLevel.NONE,null); }
    public FancyPacketMap fullSyncPacket(ISyncingContext context,TrackingLevel oldLevel) { return this.fullSyncPacket(context,oldLevel,null); }
    public FancyPacketMap fullSyncPacket(ISyncingContext context,TrackingLevel oldLevel,@Nullable Set<TraderNodeType<?>> changedNodes) {
        if(context.getPlayerTrackingLevel() == TrackingLevel.NONE && context.getSpecialCustomerSet().isEmpty())
            return FancyPacketMap.EMPTY;
        FancyPacketMap.Mutable builder = FancyPacketMap.newMutable();
        for(TraderNode n : this.getAllNodes())
        {
            if(n instanceof ISyncingNode node && (changedNodes == null || changedNodes.contains(n.getType())))
            {
                if(node.sendTo(context,oldLevel))
                {
                    FancyPacketMap.Mutable entry = FancyPacketMap.newMutable();
                    node.createSyncPacket(entry,context);
                    builder.setMap(LCRegistries.Trader.TRADER_NODE_TYPE.getKey(n.getType()).toString(),entry);
                }
            }
        }
        return builder.immutable();
    }

    public FancyPacketMap getChangedData(Player player)
    {
        FancyPacketMap.Mutable builder = FancyPacketMap.newMutable();
        ISyncingContext context = this.trackingData.getContext(player);
        for(TraderNodeType<?> type : new HashSet<>(this.changedNodes))
        {
            TraderNode node = this.getNode(type);
            if(node instanceof ISyncingNode n)
                builder.setMap(LCRegistries.Trader.TRADER_NODE_TYPE.getKey(type).toString(),n.getChangedData(context));
        }
        return builder.immutable();
    }

    public void clean()
    {
        this.changedNodes.clear();
        for(TrackingLevel level : TrackingLevel.HIGHEST_TO_LOWEST)
            this.getRemovedPlayerSet(level).clear();
        for(ISyncingNode node : this.getNodes(ISyncingNode.class))
            node.clean();
    }

    public void handleSyncPacket(FancyPacketMap packet)
    {
        //Never process a sync packet on the logical server
        if(this.isServer())
            return;
        for(String key : packet.keySet())
        {
            try {
                TraderNodeType<?> type = LCRegistries.Trader.TRADER_NODE_TYPE.getValue(Identifier.parse(key));
                if(type != null && this.nodes.get(type) instanceof ISyncingNode sn)
                    sn.onDataSync(packet);
            } catch (IdentifierException ignored) {}
        }
    }

    //Nodes
    private boolean initialized = false;
    public boolean isInitialized() { return this.initialized; }
    public void initialize() {
        if(this.initialized)
            throw new IllegalStateException("Cannot initialize a trader twice!");
        this.initialized = true;
        for(TraderNode node : this.nodes.values())
            node.onAttach();
    }

    private long id;
    public long getID() { return this.id; }
    public void setID(long id) { this.id = id; }

    public TraderState getState() {
        for(ITraderStateSource node : this.getNodes(ITraderStateSource.class))
        {
            Optional<TraderState> state = node.getCurrentState();
            if(state.isPresent())
                return state.get();
        }
        return TraderState.NORMAL;
    }

    private final TraderType type;
    public TraderType getType() { return this.type; }

    private final Map<TraderNodeType<?>,TraderNode> nodes;
    private Map<TraderNodeType<?>,TraderNode> getNodes() { return this.nodes; }
    private Map<TraderNodeType<?>,TraderNode> getWritableNodes() {
        Map<TraderNodeType<?>,TraderNode> temp = new HashMap<>();
        this.nodes.forEach((type,node) -> {
            if(!(node instanceof IUnitNode))
                temp.put(type,node);
        });
        return temp;
    }
    public List<TraderNode> getAllNodes() { return ImmutableList.copyOf(this.nodes.values()); }

    @Override
    public boolean hasNode(TraderNodeType<?> type) { return this.nodes.containsKey(type); }
    @Nullable
    @Override
    public <T extends TraderNode> T getNode(TraderNodeType<T> type)
    {
        if(this.nodes.containsKey(type))
        {
            try { return (T) this.nodes.get(type);
            } catch (ClassCastException e) { LightmansCurrency.LogError("Cached Trader Node is the wrong type!",e); }
        }
        return null;
    }

    public void setChangedNoPacket() {
        if(this.isClient() || !this.initialized)
            return;
        TraderDataCache.TYPE.get(ISidedContext.LOGICAL_SERVER).setChanged();
    }
    public void setChanged(TraderNode node)
    {
        if(this.isClient() || !this.initialized)
            return;
        if(node != null)
        {
            this.changedNodes.add(node.getType());
            this.trackingData.afterNodeChanged(node.getType());
        }
        TraderDataCache.TYPE.get(ISidedContext.LOGICAL_SERVER).setTraderChanged(this);
    }

    //Constructors and Node initialization
    public TraderData(TraderType type) { this(type, TraderArguments.builder()); }
    public TraderData(TraderType type,TraderArguments arguments) { this(type,-1,new HashMap<>(),arguments); }
    private TraderData(TraderType type,long id,Map<TraderNodeType<?>,TraderNode> nodes) { this(type,id,nodes,TraderArguments.builder()); }
    private TraderData(TraderType type,long id,Map<TraderNodeType<?>,TraderNode> nodes,TraderArguments arguments) {
        this.type = type;
        this.id = id;
        this.nodes = this.collectNodes(nodes,arguments);
    }

    private ImmutableMap<TraderNodeType<?>,TraderNode> collectNodes(Map<TraderNodeType<?>,TraderNode> loadedNodes,TraderArguments arguments)
    {
        Map<TraderNodeType<?>,Optional<Object>> nodes = new HashMap<>();
        NodeCollector c = NodeCollector.forMap(nodes,arguments);
        this.type.addNodes(c);
        //Post Trader Nodes Event
        NeoForge.EVENT_BUS.post(new TraderEvent.RegisterNodesEvent(this,c));
        //Assemble nodes
        Map<TraderNodeType<?>,TraderNode> result = new HashMap<>();
        nodes.forEach((type,argument) -> {
            //Put loaded node in if present
            TraderNode node;
            if(loadedNodes.containsKey(type))
            {
                node = loadedNodes.get(type);
                node.updateArgument(argument);
            }
            else
                node = type.create(argument);
            result.put(type,node);
            node.pairWithTrader(this);
        });
        return ImmutableMap.copyOf(result);
    }

    //Interface Implementations
    @Override
    public OwnerHolder getOwner() {
        for(IOwnerSource node : this.getNodes(IOwnerSource.class))
        {
            Optional<OwnerHolder> owner = node.getValidOwner();
            if(owner.isPresent())
                return owner.get();
        }
        return new OwnerHolder(this);
    }

    public boolean isPermissionBlocked(Permission<?> permission)
    {
        for(IPermissionBlocker node : this.getNodes(IPermissionBlocker.class))
        {
            if(node.blockPermission(permission))
                return true;
        }
        return false;
    }

    //Permission Access
    public <T> T getPermission(Player player,Permission<T> permission)
    {
        //Ignore if the permission is blocked
        if(this.isPermissionBlocked(permission))
            return permission.getEmpty();
        //Force to max value if the player is an admin
        if(LCApi.isInAdminMode(player))
            return permission.getMaxValue();
        //Otherwise get the value from the PR
        return this.getPermissionInternal(PlayerReference.of(player),permission);
    }

    public <T> T getPermission(PlayerReference player, Permission<T> permission)
    {
        if(this.isPermissionBlocked(permission))
            return permission.getEmpty();
        return this.getPermissionInternal(player,permission);
    }

    private <T> T getPermissionInternal(PlayerReference player,Permission<T> permission)
    {
        PermissionType<T> type = permission.getType();
        T val = type.getEmpty();
        for(IPermissionSource node : this.getNodes(IPermissionSource.class))
            val = type.getHighest(val,node.getPlayerPermission(player,permission));
        return val;
    }

    //Menus
    public void openStorageMenu(Player player,MenuValidator validator)
    {
        for(ITraderMenuProvider node : this.getNodes(ITraderMenuProvider.class))
        {
            MenuProvider menu = node.storageMenuProvider(player,validator);
            if(menu != null)
                player.openMenu(menu);
        }
        LightmansCurrency.LogDebug("Attempting to open storage menu for " + player.getName().getString());
        player.openMenu(TraderStorageMenu.getProvider(this,validator));
    }

    public void openCustomerMenu(Player player,MenuValidator validator)
    {
        for(ITraderMenuProvider node : this.getNodes(ITraderMenuProvider.class))
        {
            MenuProvider menu = node.storageMenuProvider(player,validator);
            if(menu != null)
                player.openMenu(menu);
        }
        //player.openMenu(TraderStorageMenu.getProvider(this,validator));
    }

    //Trade Execution
    /**
     * Called by {@link TradeContext.Builder} during its construction to obtain the traders resources from the traders nodes
     * @param collector The Resource Collector that will handle storing the resources to the trade context
     */
    @ApiStatus.Internal
    public void collectResources(ResourceCollector collector)
    {
        for(ITradeResourceProvider node : this.getNodes(ITradeResourceProvider.class))
            node.attachResource(collector);
    }

    public TradeResult attemptTrade(TradeContext.Builder builder,int tradeIndex)
    {
        try(TradeContext context = builder.build())
        {
            if(context.getTrader() != this)
                throw new IllegalStateException("Attempted to execute the trade for a different traders context!");
            if(tradeIndex < 0)
                throw new TradeFailedException(TradeResult.FAIL_INVALID_TRADE);
            //Get trade node(s) and search for the trade of that index
            for(TradingNode<?> node : this.getTradingNodes())
            {
                int tradeCount = node.getTradeCount();
                if(tradeCount < tradeIndex)
                {
                    TradeEvent.Pre event = node.runPreTradeEvent(context,tradeIndex);
                    if(event.isCanceled())
                        return TradeResult.FAIL_EVENT_DENIAL;
                    return node.executeTrade(context,tradeIndex);
                }
                else
                    tradeIndex -= tradeCount;
                if(tradeIndex < 0)
                    throw new TradeFailedException(TradeResult.FAIL_INVALID_TRADE);
            }
            throw new TradeFailedException(TradeResult.FAIL_INVALID_TRADE);
        } catch (TradeFailedException e) { return e.result; }
    }

}