package io.github.lightman314.lightmanscurrency.api.traders.data;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.events.TraderEvent;
import io.github.lightman314.lightmanscurrency.api.misc.IPermissions;
import io.github.lightman314.lightmanscurrency.api.misc.ISidedObject;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.ownership.IOwnable;
import io.github.lightman314.lightmanscurrency.api.settings.ISaveableSettingsHolder;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxableContext;
import io.github.lightman314.lightmanscurrency.api.traders.*;
import io.github.lightman314.lightmanscurrency.api.traders.client.TraderClientHooks;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.*;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.UnitNode;
import io.github.lightman314.lightmanscurrency.api.traders.tracking.TrackingLevel;
import io.github.lightman314.lightmanscurrency.api.traders.tracking.TraderTrackingData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.trade.*;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.*;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.*;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.TradeOfferSourceNode;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.core.ModStats;
import io.github.lightman314.lightmanscurrency.common.data.types.TraderDataCache;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.TraderEjectionData;
import io.github.lightman314.lightmanscurrency.common.items.data.TraderItemData;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeStackHandler;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxable;
import io.github.lightman314.lightmanscurrency.api.misc.world.WorldPosition;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxableReference;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.builtin.TaxableTraderReference;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import io.github.lightman314.lightmanscurrency.util.*;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.ITraderBlock;
import io.github.lightman314.lightmanscurrency.api.ejection.IDumpable;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.*;
import io.github.lightman314.lightmanscurrency.network.message.trader.SPacketSyncUsers;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public abstract class TraderData implements ISidedObject, IDumpable, IUpgradeable, ITraderSource, ITradeRuleHost, ITaxable, IOwnable, ISaveableSettingsHolder, IPermissions, LazyPacketData.IBuilderProvider {

    public static final Codec<TraderData> CODEC = Codec.withAlternative(
            //Desired Codec
            TraderType.CODEC.dispatch(TraderData::getType,TraderType::mapCodec),
            //Deprecated fallback codec
            CodecHelper.oldValueLoader(TraderData::parseOldTrader,"Trader Data"));

	public static final int GLOBAL_TRADE_LIMIT = 100;
	public static final Predicate<Notification> LOGS_NORMAL_FILTER = n -> !n.getCategory().matches(NullCategory.INSTANCE);
	public static final Predicate<Notification> LOGS_SETTINGS_FILTER = n -> n.getCategory().matches(NullCategory.INSTANCE);

    private TraderTrackingData trackingData = new TraderTrackingData();

    //Set of changed trader nodes that need to send sync packets
    private final Set<TraderNodeType<?>> changedNodes = new HashSet<>();
    //Set of players who have ended their tracking of this trader recently
    //Will be cleared when the next sync packet is sent
    //If a new tracking request occurs before this is cleared an intialization packet won't be sent
    private final Map<TrackingLevel,Set<UUID>> removedPlayerTracking = new HashMap<>();
    private Set<UUID> getRemovedPlayerSet(TrackingLevel level)
    {
        if(level == TrackingLevel.NONE)
            return new HashSet<>();
        if(!this.removedPlayerTracking.containsKey(level))
            this.removedPlayerTracking.put(level,new HashSet<>());
        return this.removedPlayerTracking.get(level);
    }

    /**
     * Used to copy tracking data for persistent trader data after they are reloaded from file
     * @param other The old trader that the tracking data should be copied from
     */
    public final void copyTrackingData(TraderData other) { this.trackingData = other.trackingData; }
    public final TrackingLevel getTrackingLevel(Player player) { return this.trackingData.getLevel(player); }
    public long requestTracking(Player player,TrackingLevel level) {
        if(this.isClient || level == TrackingLevel.NONE)
            return -1;
        TrackingLevel oldLevel = this.getTrackingLevel(player);
        TraderTrackingData.Result result = this.trackingData.requestTracking(player,level);
        if(level.ordinal() > oldLevel.ordinal())
        {
            //Don't send a fresh packet if we only just removed them from this level
            Set<UUID> set = this.getRemovedPlayerSet(level);
            if(set.contains(player.getUUID()))
                set.remove(player.getUUID());
            else
            {
                TraderDataCache.TYPE.get(false).sendUpdatePacket(player,this,this.fullSyncPacket(player,oldLevel,level,result.changedNodes()));
            }
            for(TraderNode node : this.getNodeIterable())
            {
                if(node instanceof ISyncingNode sn)
                    sn.afterTrackingChange(player,oldLevel,level);
            }
        }
        return result.key();
    }
    public void endTracking(Player player,long key)
    {
        if(this.isClient)
            return;
        TrackingLevel oldLevel = this.getTrackingLevel(player);
        this.trackingData.endTracking(player,key);
        TrackingLevel newLevel = this.trackingData.getLevel(player);
        if(newLevel.ordinal() < oldLevel.ordinal())
        {
            Set<UUID> set = this.getRemovedPlayerSet(oldLevel);
            set.add(player.getUUID());
            for(TraderNode node : this.getNodeIterable())
            {
                if(node instanceof ISyncingNode n)
                    n.afterTrackingChange(player,oldLevel,newLevel);
            }
        }
    }
    public void onPlayerLeave(Player player) {
        this.trackingData.clearPlayer(player.getUUID());
        for(TraderNode node : this.getNodeIterable())
        {
            if(node instanceof ISyncingNode n)
                n.afterTrackingEnded(player.getUUID());
        }
    }

    public final LazyPacketData.Builder fullSyncPacket(Player player) {
        return this.fullSyncPacket(player,TrackingLevel.NONE,this.getTrackingLevel(player),null);
    }
    public final LazyPacketData.Builder fullSyncPacket(Player player,TrackingLevel oldLevel,TrackingLevel newLevel) { return this.fullSyncPacket(player,oldLevel,newLevel,null); }
    public final LazyPacketData.Builder fullSyncPacket(Player player,TrackingLevel oldLevel,TrackingLevel newLevel,@Nullable Set<TraderNodeType<?>> changedNodes)
    {
        if(newLevel == TrackingLevel.NONE)
            return this.builder();
        LazyPacketData.Builder builder = this.builder();
        for(TraderNode n : this.nodes.values())
        {
            if(n instanceof ISyncingNode node && (changedNodes == null || changedNodes.contains(n.getType())))
            {
                if(node.sendTo(player,newLevel) && !node.sendTo(player,oldLevel))
                {
                    LazyPacketData.Builder entry = this.builder();
                    node.createSyncPacket(entry,player);
                    builder.setMap(LCRegistries.TRADER_NODE.getKey(n.getType()).toString(),entry);
                }
            }
        }
        return builder;
    }

    public final void handleSyncPacket(LazyPacketData packet) {
        for(String key : packet.keySet())
        {
            try {
                TraderNodeType<?> type = LCRegistries.TRADER_NODE.get(ResourceLocation.parse(key));
                if(type != null && this.nodes.containsKey(type))
                    this.nodes.get(type).handleSyncPacket(packet.getMap(key));
            } catch (ResourceLocationException ignored) {}
        }
    }

	private final Map<String,SettingsNode> settingsNodes = new HashMap<>();
	@Nullable
	@Override
	public SettingsNode getSettingsNode(String nodeKey) { return this.settingsNodes.get(nodeKey); }
	@Override
	public List<SettingsNode> getAllSettingNodes() {
		List<SettingsNode> nodes = new ArrayList<>(this.settingsNodes.values());
		nodes.sort(SettingsNode.SORTER);
		return new ArrayList<>(nodes);
	}

    @Override
    public void buildLoadContext(LoadContext.Builder builder) {
        builder.withOwner(this);
        this.ifNodePresent(AlliesNode.TYPE, node ->
            builder.withAllies(node.getAllies())
                    .withAllyPermissions(node.getAllyPermissionsMap()));
        builder.withBlockedPermissions(s -> this.nodes.values().stream().anyMatch(n -> n.blockPermission(s)));
    }

    private boolean canMarkDirty = false;
	public final void initialize() {
        this.canMarkDirty = true;
        for(TraderNode node : this.nodes.values())
            node.onAttach();
    }

    @Override
    public final HolderLookup.Provider registryAccess() { return LookupHelper.getRegistryAccess(); }

    @Override
    public final LazyPacketData.Builder builder() { return LazyPacketData.builder(this.registryAccess()); }

    private long id = -1;
	public final long getID() { return this.id; }
	public final void setID(long id) { this.id = id; }

    private final List<Object> clientAttachments = new ArrayList<>();
    public List<Object> getClientAttachments() { return ImmutableList.copyOf(this.clientAttachments); }

    public LazyPacketData getChangedData(Player player)
    {
        LazyPacketData.Builder builder = LazyPacketData.builder(this.registryAccess());
        for(var type : new HashSet<>(this.changedNodes))
        {
            TraderNode node = this.getNode(type);
            if(node instanceof ISyncingNode n)
                builder.setMap(LCRegistries.TRADER_NODE.getKey(type).toString(),n.getChangedData(player));
        }
        return builder.build();
    }

    public final void clean()
    {
        this.changedNodes.clear();
        this.getRemovedPlayerSet(TrackingLevel.STORAGE).clear();
        this.getRemovedPlayerSet(TrackingLevel.CUSTOMER).clear();
        for(TraderNode node : this.getNodeIterable())
        {
            if(node instanceof ISyncingNode sn)
                sn.clean();
        }
    }

    private final Map<TraderNodeType<?>, TraderNode> nodes;
    protected final Map<TraderNodeType<?>, TraderNode> getNodes() { return this.nodes; }
    protected final Map<TraderNodeType<?>, TraderNode> getWritableNodes() {
        Map<TraderNodeType<?>, TraderNode> temp = new HashMap<>();
        this.nodes.forEach((type,node) -> {
            if(!(node instanceof UnitNode))
                temp.put(type,node);
        });
        return temp;
    }
    public final Iterable<TraderNode> getNodeIterable() { return this.nodes.values(); }
    public final boolean hasNode(TraderNodeType<?> type) { return this.nodes.containsKey(type); }
    @Nullable
    public final <T extends TraderNode> T getNode(TraderNodeType<T> type)
    {
        if(this.nodes.containsKey(type)){
            try {
                return (T)this.nodes.get(type);
            }catch (ClassCastException e) { LightmansCurrency.LogError("Error getting Trader Node!",e); }
        }
        return null;
    }
    public final <T extends TraderNode> void ifNodePresent(TraderNodeType<T> type, Consumer<T> action) {
        T node = this.getNode(type);
        if(node != null)
            action.accept(node);
    }
    @Nullable
    public final <R,T extends TraderNode> R findNodeValue(TraderNodeType<T> type, Function<T,R> getter)  { return this.findNodeValue(type,getter,null); }
    public final <R,T extends TraderNode> R findNodeValue(TraderNodeType<T> type, Function<T,R> getter, R defaultValue)
    {
        T node = this.getNode(type);
        if(node != null)
            return getter.apply(node);
        return defaultValue;
    }
    @Nullable
    public final List<TraderNode> getNodes(Predicate<TraderNode> filter)
    {
        List<TraderNode> results = new ArrayList<>();
        for(TraderNode n : this.nodes.values())
        {
            if(filter.test(n))
                results.add(n);
        }
        return results;
    }
    @Nullable
    public TradeOfferSourceNode<?> getTradeOfferNode() {
        for(TraderNode node : this.nodes.values())
        {
            if(node instanceof TradeOfferSourceNode<?> source)
                return source;
        }
        return null;
    }
    @Nullable
    public final <T> T findTradeOfferNodeValue(Function<TradeOfferSourceNode<?>,T> getter) { return this.findTradeOfferNodeValue(getter,null); }
    @Nullable
    public final <T> T findTradeOfferNodeValue(Function<TradeOfferSourceNode<?>,T> getter,@Nullable T defaultValue) {
        TradeOfferSourceNode<?> node = this.getTradeOfferNode();
        return node == null ? defaultValue : getter.apply(node);
    }

    @Override
    public OwnerData getOwner() {
        for(TraderNode node : this.nodes.values())
        {
            if(node instanceof IOwnerSource source)
            {
                OwnerData owner = source.getValidOwner();
                if(owner != null)
                    return owner;
            }
        }
        return new OwnerData(this);
    }

	/**
	 * Whether this trader is in a state that can be accessed through <b>any</b> means
	 */
	public final boolean allowAccess() { return this.getState().allowAccess; }
	/**
	 * Whether the traders current state would allow the trader to be recovered with the <code>/lcadmin traderdata recover TRADER_ID</code> command`
	 */
	public final boolean isRecoverable() { return this.getState().allowRecovery; }
	/**
	 * Whether the traders current state would make it possible for the block to be located in a world (if said chunk is loaded of course)
	 */
	public boolean hasWorldPosition() { return !this.getWorldPosition().isVoid() && this.getState().validateWorldPosition; }

	public boolean canBeNetworkAccessible() { return !this.getNodes(n -> n instanceof INetworkController).isEmpty(); }

	public boolean readyForCustomers() { return this.hasValidTrade(); }
	
	public TraderState getState() { return this.findNodeValue(WorldStateNode.TYPE, WorldStateNode::getState,TraderState.NORMAL); }
    public void setState(TraderState state)
	{
        WorldStateNode node = this.getNode(WorldStateNode.TYPE);
        if(node != null)
            node.setState(state);
	}
	public void PickupTrader(Player player, boolean adminState)
	{
		if(this.isClient() || this.getState() != TraderState.NORMAL)
			return;
		if(!LCAdminMode.isAdminPlayer(player))
			adminState = false;
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			if(this.shouldRemove(server))
				return;
			TraderBlockEntity<?> be = this.getBlockEntity();
			if(be != null)
			{
				ItemStack result = be.PickupTrader(player, this);
				if(!result.isEmpty())
				{
					//Change trader state
					this.setState(adminState ? TraderState.ADMIN_HELD_AS_ITEM : TraderState.HELD_AS_ITEM);
					//Give the item this traders ID for future loading
					result.set(ModDataComponents.TRADER_ITEM_DATA,new TraderItemData(this.id));
					//Add the model variant to the item
					this.ifNodePresent(WorldStateNode.TYPE,node -> node.addVariantToStack(result));
					//Give the item to the player
					ItemHandlerHelper.giveItemToPlayer(player,result);
				}
			}
		}
	}
	public void OnTraderMoved(WorldPosition newPosition)
	{
        WorldStateNode node = this.getNode(WorldStateNode.TYPE);
        if(node != null)
        {
            node.setState(TraderState.NORMAL);
            node.setPosition(newPosition);
        }
	}

    public final boolean hasInfiniteStock() {
        AtomicBoolean result = new AtomicBoolean(false);
        for(TraderNode node : this.nodes.values())
        {
            if(node instanceof IBaseRuleModifier modifier)
                modifier.hasInfiniteStock(result);
        }
        return result.get();
    }

    public final boolean shouldStoreMoney() {
        AtomicBoolean result = new AtomicBoolean(true);
        for(TraderNode node : this.nodes.values())
        {
            if(node instanceof IBaseRuleModifier modifier)
                modifier.shouldStoreMoney(result);
        }
        return result.get();
    }

    public final boolean shouldStorePurchases() { return !this.hasUpgrade(Upgrades.VOID); }

	private boolean isClient = false;
	@Override
	public final TraderData flagAsClient() { return this.flagAsClient(true); }
	@Override
	public final TraderData flagAsClient(boolean isClient) {
        if(this.isClient == isClient)
            return this;
        this.isClient = isClient;
        if(this.isClient)
        {
            //Flag the nodes as client-only
            for(TraderNode node : this.nodes.values()){
                if(node instanceof ISidedListener sided && this.isClient)
                    sided.onClientFlagSet();
            }
        }
        if(this.isClient && this.clientAttachments.isEmpty())
            this.clientAttachments.addAll(TraderClientHooks.collectClientAttachments(this));
        return this;
    }
	@Override
	public final TraderData flagAsClient(IClientTracker context) { return this.flagAsClient(context.isClient()); }
	public final boolean isClient() { return this.isClient; }
    public final boolean isServer() { return ISidedObject.super.isServer(); }

    public void initializeAllyPermissions(BiConsumer<String,Integer> defaultConsumer) {
        defaultConsumer.accept(Permissions.OPEN_STORAGE, 1);
        defaultConsumer.accept(Permissions.EDIT_SETTINGS, 1);
    }

    public final boolean isPermissionBlocked(String permission) { return this.nodes.values().stream().anyMatch(n -> n.blockPermission(permission)); }

	public boolean hasPermission(@Nullable Player player, String permission) { return this.getPermissionLevel(player, permission) > 0; }
	public boolean hasPermission(@Nullable PlayerReference player, String permission) { return this.getPermissionLevel(player, permission) > 0; }
	
	public int getPermissionLevel(@Nullable Player player, String permission) {
        var result = this.getPermissionLevelInternal(PlayerReference.of(player),permission);
        if(result.getFirst())
            return result.getSecond();
        return player == null || LCAdminMode.isAdminPlayer(player) ? Integer.MAX_VALUE : result.getSecond();
	}
	public int getPermissionLevel(@Nullable PlayerReference player, String permission) { return this.getPermissionLevelInternal(player,permission).getSecond(); }

    private Pair<Boolean,Integer> getPermissionLevelInternal(@Nullable PlayerReference player, String permission)
    {
        int result = 0;
        for(TraderNode node : this.nodes.values())
        {
            if(node.blockPermission(permission))
                return Pair.of(true,0);
            if(player != null && node instanceof IPermissionProvider permNode)
                result = Math.max(result,permNode.getPermissionLevel(permission,player));
        }
        return Pair.of(false,result);
    }

	public final ImmutableList<String> persistentTraderBlockedPermissions() {
		List<String> blockedPermissions = Lists.newArrayList(Permissions.EDIT_TRADES, Permissions.EDIT_SETTINGS, Permissions.INTERACTION_LINK, Permissions.TRANSFER_OWNERSHIP, Permissions.COLLECT_COINS, Permissions.STORE_COINS);
		this.blockPermissionsForPersistentTrader(blockedPermissions);
		return ImmutableList.copyOf(blockedPermissions);
	}

	protected void blockPermissionsForPersistentTrader(List<String> list) { }

	public boolean hasCustomName() { return this.findNodeValue(DisplayNode.TYPE,DisplayNode::hasCustomName,false); }

	public IconData getDisplayIcon() {
        DisplayNode node = this.getNode(DisplayNode.TYPE);
        if(node != null && node.hasCustomIcon())
            return node.getCustomIcon();
        return this.getIcon();
    }

	public abstract IconData getIcon();

	@Override
	public Component getName() {
        DisplayNode node = this.getNode(DisplayNode.TYPE);
		if(node != null && node.hasCustomName())
			return node.getCustomName();
		return this.getDefaultName();
	}

    public final boolean showOwnerInTitle() {
        AtomicBoolean result = new AtomicBoolean(true);
        for(TraderNode node : this.nodes.values()){
            if(node instanceof IBaseRuleModifier modifier)
                modifier.showOwnerInTitle(result);
        }
        return result.get();
    }
	
	public final Component getTitle() {
		if(!this.showOwnerInTitle() || this.getOwner().getValidOwner().isNull())
			return this.getName();
		return LCText.GUI_TRADER_TITLE.get(this.getName(), this.getOwner().getName());
	}

    public final IconData getIconForItem(ItemStack stack) { return this.getIconForItem(stack,this.findNodeValue(DisplayNode.TYPE,DisplayNode::getCustomIcon,IconData.Null())); }
	/**
	 * Can be overridden by child traders to make special icons from certain items<br>
	 * (i.e. an icon that renders lava if the item stack is a lava bucket, etc.)<br><br>
	 * By default, returns a simple item icon for the given item
	 */
	protected IconData getIconForItem(ItemStack stack,IconData originalIcon) { return ItemIcon.ofItem(stack.copyWithCount(1)); }

	@Nullable
	public Item getTraderBlock() { return this.findNodeValue(WorldStateNode.TYPE,WorldStateNode::getTraderBlock); }
	protected Component getDefaultName() {
        Item traderBlock = this.getTraderBlock();
		if(traderBlock != null)
			return new ItemStack(traderBlock).getHoverName();
		return LCText.GUI_TRADER_DEFAULT_NAME.get();
	}
    @Nullable
	public ResourceLocation getTraderBlockVariant() { return this.findNodeValue(WorldStateNode.TYPE,WorldStateNode::getBlockVariant); }
	public void setTraderBlockVariant(@Nullable ResourceLocation blockVariant, boolean variantLocked) {
        WorldStateNode node = this.getNode(WorldStateNode.TYPE);
        if(node != null)
            node.setTraderBlockVariant(blockVariant,variantLocked);
	}

	public IMoneyHolder getStoredMoney()
	{
        IBankAccount ba = this.findNodeValue(BankNode.TYPE,BankNode::getBankAccount);
		if(ba != null)
			return ba.getMoneyStorage();
		return this.findNodeValue(MoneyStorageNode.TYPE,MoneyStorageNode::getStorage,IMoneyHolder.EMPTY);
	}

	public final MoneyValue payTaxesOn(MoneyValue amount, ITaxableContext context)
	{
        TaxesNode node = this.getNode(TaxesNode.TYPE);
        if(node != null)
            return node.payTaxesOn(amount,context);
		return MoneyValue.empty();
	}

	public final boolean isInQuarantine() {
		ResourceKey<Level> level = this.getWorldPosition().getDimension();
		return level != null && QuarantineAPI.IsDimensionQuarantined(level);
	}

    @Override
    public final UpgradeStackHandler getUpgrades()
    {
        if(this.hasNode(UpgradesNode.TYPE))
            return this.getNode(UpgradesNode.TYPE).getContainer();
        return UpgradeStackHandler.EMPTY;
    }

    public boolean hasUpgrade(UpgradeType type)
    {
        UpgradesNode node = this.getNode(UpgradesNode.TYPE);
        if(node == null)
            return false;
        return UpgradeType.hasUpgrade(type,node.getContainer());
    }

	@Override
	public final boolean allowUpgrade(UpgradeType type) {
        for(TraderNode node : this.nodes.values())
        {
            if(node instanceof IUpgradeHandler handler && handler.allowUpgrade(type))
                return true;
        }
        return false;
	}

	@Override
	public boolean showSearchBox() { return this.findNodeValue(DisplayNode.TYPE,DisplayNode::alwaysShowSearchBox,false); }

	public final int getTradeCount() { return this.findTradeOfferNodeValue(TradeOfferSourceNode::getTradeCount,0); }
    public final boolean canEditTradeCount() { return this.findTradeOfferNodeValue(TradeOfferSourceNode::canEasilyChangeQuantity,false); }
	public final int getTradeStock(int index) { return this.findTradeOfferNodeValue(node -> node.getTradeStock(index),0); }
	public int validTradeCount() { return (int)this.getTradeData().stream().filter(TradeData::isValid).count(); }
	public boolean hasValidTrade() { return this.getTradeData().stream().anyMatch(TradeData::isValid); }
	public int tradesWithStock() {
		TradeContext context = TradeContext.createStorageMode(this);
		return (int)this.getTradeData().stream().filter(t -> t.isValid() && t.hasStock(context)).count();
	}
	public boolean anyTradeHasStock()
	{
		TradeContext context = TradeContext.createStorageMode(this);
		return this.getTradeData().stream().anyMatch(t -> t.isValid() && t.hasStock(context));
	}

    public List<TradeDirection> validDirectionOptions() { return List.of(); }

    /**
     * The {@link WorldPosition} of the trader, which contains both its dimension and block position.<br>
     * May be {@link WorldPosition#VOID} if the trader doesn't have a physical block in the world.<br>
     * May be inaccurate if the traders state is not {@link TraderState#NORMAL}
     */
    public WorldPosition getWorldPosition() { return this.findNodeValue(WorldStateNode.TYPE,WorldStateNode::getPos,WorldPosition.VOID); }

	/**
	 * Gets the in-game Block Entity of this Trader Block (if one exists)
	 */
	@Nullable
	public TraderBlockEntity<?> getBlockEntity()
	{
        WorldPosition pos = this.getWorldPosition();
		Level level = LightmansCurrency.getProxy().getDimension(this.isClient,pos.getDimension());
		if(level != null && level.isLoaded(pos.getPos()) && level.getBlockEntity(pos.getPos()) instanceof TraderBlockEntity<?> be && be.getTraderID() == this.id)
			return be;
		return null;
	}

    @Override
	public TaxableReference getReference() { return new TaxableTraderReference(this.getID()); }

    @Override
    public boolean isNetworkAccessible() {
        if(!this.allowAccess() || this.isInQuarantine()) //Hide from terminal if not accessible or if this trader is in a quarantined dimension
            return false;
        for(TraderNode node : this.nodes.values())
        {
            if(node instanceof INetworkController nc && nc.visibleToNetwork())
                return true;
        }
        return false;
    }

    /**
     * Updates the world position of the trader<br>
     * Should be overridden by child classes if they don't utilize the {@link WorldStateNode}
     * @param level The Level the trader was moved to
     * @param pos The Block Position of the traders direct block (bottom,left,front)
     */
	//
	public void move(Level level, BlockPos pos)
	{
        WorldStateNode node = this.getNode(WorldStateNode.TYPE);
        if(node != null)
            node.setPosition(WorldPosition.ofLevel(level, pos));
	}
	
	protected TraderData() { this(new HashMap<>()); }
	protected TraderData(Map<TraderNodeType<?>,Object> arguments) {
        //Register Attachments first
        this.nodes = this.registerTraderNodes(new HashMap<>(),arguments);
        for(TraderNode n : this.nodes.values())
            n.attach(this);
		this.registerSettingsNodes(n -> this.settingsNodes.put(n.key,n));
	}

    protected TraderData(long id,Map<TraderNodeType<?>, TraderNode> nodes) { this(new HashMap<>(),id,nodes); }
    protected TraderData(Map<TraderNodeType<?>,Object> args,long id,Map<TraderNodeType<?>, TraderNode> nodes) {
        this.id = id;
        this.nodes = this.registerTraderNodes(nodes,args);
        for(TraderNode n : this.nodes.values())
            n.attach(this);
        this.registerSettingsNodes(n -> this.settingsNodes.put(n.key,n));
    }

    public abstract TraderType<?> getType();

	protected final void registerSettingsNodes(Consumer<SettingsNode> builder)
	{
		//Misc Settings
        for(TraderNode node : this.nodes.values())
            node.registerSettingsNodes(this,builder);
	}

    protected final ImmutableMap<TraderNodeType<?>, TraderNode> registerTraderNodes(Map<TraderNodeType<?>, TraderNode> loadedMap, Map<TraderNodeType<?>,Object> arguments)
    {
        //Post node event
        Map<TraderNodeType<?>,Object> nodes = new HashMap<>();
        NodeCollector c = NodeCollector.forMap(nodes,arguments);
        this.addDefaultNodes(c);
        TraderEvent.RegisterNodesEvent event = VersionUtil.postEvent(new TraderEvent.RegisterNodesEvent(this,nodes));
        //Assemble attachments
        Map<TraderNodeType<?>, TraderNode> temp = new HashMap<>();
        event.getNodes().forEach((type,argument) -> {
            //Put loaded node in if present
            TraderNode node;
            if(loadedMap.containsKey(type))
            {
                node = loadedMap.get(type);
                node.updateArgument(argument);
            }
            //Otherwise, create a new node
            else
                node = type.create(argument);
            if(node.hasNoConflicts(this,temp))
                temp.put(type,node);
        });
        return ImmutableMap.copyOf(temp);
    }

    public abstract void addDefaultNodes(NodeCollector collector);

    public boolean shouldValidateRules() { return true; }

    public final void setChangedNoPacket() {
        if(this.isClient || !this.canMarkDirty)
            return;
        TraderDataCache.TYPE.get(false).setChanged();
    }

    public final void setChanged(@Nullable TraderNode node)
    {
        if(this.isClient)
            return;
        if(node != null)
        {
            this.changedNodes.add(node.getType());
            this.trackingData.afterNodeChanged(node.getType());
        }
        TraderDataCache.TYPE.get(false).markTraderDirty(this);
    }

	public final CompoundTag save(DataContext<Tag> context) { return (CompoundTag)CODEC.encodeStart(context.ops(),this).getOrThrow(); }

    public static TraderData load(CompoundTag tag, DataContext<Tag> context) { return CODEC.decode(context.ops(),tag).getOrThrow().getFirst(); }

    @Deprecated
	public final void loadOldData(CompoundTag compound, HolderLookup.Provider lookup) {

		if(compound.contains("ID", Tag.TAG_LONG))
			this.setID(compound.getLong("ID"));

        //Load Attachments
        for(TraderNode node : this.nodes.values())
            node.loadOldData(compound,lookup);

		//Load trader-specific data
		this.loadAdditional(compound,lookup);
		
	}

	/**
	 * Code ran when the Trader is in it's fully registered/added state.
	 * Does not promise that other traders are also fully loaded and/or registered to the Trader Save Data.
	 * Run on both Server and Client, so ensure you check this.isClient() or this.isServer()
	 * to confirm what logical side this trader is loaded on.
	 */
	public void OnRegisteredToOffice() { for(TraderNode node : this.nodes.values()) node.onRegisteredToOffice(); }

    /**
     * Used to load old data from before Trader Nodes and Codecs were added<br>
     * Not <code>abstract</code> since **most** use-cases should be handled by {@link TraderNode#loadOldData(CompoundTag, HolderLookup.Provider)}
     */
    @Deprecated
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {}

	public void openTraderMenu(Player player, MenuValidator validator) {
		if(player instanceof ServerPlayer)
			player.openMenu(this.getTraderMenuProvider(validator), EasyMenu.encoder(this.getMenuDataWriter(), validator));
	}
	
	protected MenuProvider getTraderMenuProvider(MenuValidator validator) { return new TraderMenuProvider(this.id, validator); }

	private record TraderMenuProvider(long traderID, MenuValidator validator) implements EasyMenuProvider {

		@Override
		public AbstractContainerMenu createMenu(int windowID, Inventory inventory, Player player) { return new TraderMenu(windowID, inventory, this.traderID, validator); }

	}

	public void openStorageMenu(Player player, MenuValidator validator) {
		if(!this.hasPermission(player, Permissions.OPEN_STORAGE))
			return;
		if(player instanceof ServerPlayer sp)
			player.openMenu(this.getTraderStorageMenuProvider(validator), EasyMenu.encoder(this.getMenuDataWriter(), validator));
	}
	
	protected MenuProvider getTraderStorageMenuProvider(MenuValidator validator)  { return new TraderStorageMenuProvider(this.id, validator); }

	private record TraderStorageMenuProvider(long traderID, MenuValidator validator) implements EasyMenuProvider {

		@Override
		public AbstractContainerMenu createMenu(int windowID, Inventory inventory, Player player) { return new TraderStorageMenu(windowID, inventory, this.traderID, this.validator); }

	}

	public Consumer<RegistryFriendlyByteBuf> getMenuDataWriter() { return b -> b.writeLong(this.id); }
	
	public PreTradeEvent runPreTradeEvent(TradeData trade, TradeContext context)
	{
		PreTradeEvent event = new PreTradeEvent(trade, context);

        //Node posting
        for(TraderNode node : this.nodes.values()){
            if(node instanceof ITradeListener listener)
                listener.beforeTrade(event);
        }

        //Trades trade rules
        if(trade instanceof ITradeListener l)
            l.beforeTrade(event);

		//Public posting
		NeoForge.EVENT_BUS.post(event);
		
		return event;
	}

	public TradeCostEvent runTradeCostEvent(TradeData trade, TradeContext context)
	{
		return runTradeCostEvent(trade, context, TradeRule.getBaseCost(trade,context));
	}
	public TradeCostEvent runTradeCostEvent(TradeData trade, TradeContext context, MoneyValue baseCost)
	{
		TradeCostEvent event = new TradeCostEvent(trade, context, baseCost);

        //Node posting
        for(TraderNode node : this.nodes.values()){
            if(node instanceof ITradeListener listener)
                listener.tradeCost(event);
        }

        //Trades trade rules
        if(trade instanceof ITradeListener l)
            l.tradeCost(event);

		//Public posting
		NeoForge.EVENT_BUS.post(event);
		
		return event;
	}

	public TradeResult runPostTradeEvent(TradeData trade, TradeContext context, MoneyValue cost, MoneyValue taxesPaid) { return this.runPostTradeEvent(trade,context,cost,taxesPaid,new ArrayList<>()); }
	public TradeResult runPostTradeEvent(TradeData trade, TradeContext context, MoneyValue cost, MoneyValue taxesPaid,Object product) { return this.runPostTradeEvent(trade,context,cost,taxesPaid,Lists.newArrayList(product)); }
	public TradeResult runPostTradeEvent(TradeData trade, TradeContext context, MoneyValue cost, MoneyValue taxesPaid,List<?> product)
	{
		PostTradeEvent event = new PostTradeEvent(trade,context,cost,taxesPaid,product);

        //Node posting (i.e. Global Trader Rules)
        for(TraderNode node : this.nodes.values()){
            if(node instanceof ITradeListener listener)
                listener.afterTrade(event);
        }

        //Trades trade rules
        if(trade instanceof ITradeListener l)
            l.afterTrade(event);
		
		//Public posting
		NeoForge.EVENT_BUS.post(event);

        return TradeResult.success(event);
	}

	//Content drops
	public final List<ItemStack> getContents(Level level, BlockPos pos, @Nullable BlockState state, boolean dropBlock) {

		if(dropBlock)
		{
            ItemStack blockStack = ItemStack.EMPTY;
			Block block = state != null ? state.getBlock() : null;
			if(block != null)
				blockStack = new ItemStack(block);
			if(block instanceof ITraderBlock b)
				blockStack = b.getDropBlockItem(level, pos, state);
			if(blockStack.isEmpty())
				LightmansCurrency.LogWarning("Block drop for trader is empty!");
            final ItemStack result = blockStack;
            this.ifNodePresent(WorldStateNode.TYPE,node -> node.addVariantToStack(result));
            return this.getContents(result);
		}
		return this.getContents(ItemStack.EMPTY);
	}

	public final List<ItemStack> getContents(ItemStack item)
	{
		List<ItemStack> results = new ArrayList<>();
		if(!item.isEmpty())
			results.add(item);

        for(TraderNode node : this.nodes.values()){
            if(node instanceof IContentProvider provider)
                results.addAll(provider.getContents());
        }

		//Add trader-specific drops
		this.getAdditionalContents(results);

		return results;
	}

    protected void getAdditionalContents(List<ItemStack> list) {}

	@Override
	public EjectionData buildEjectionData(@Nullable Level level, @Nullable BlockPos pos, @Nullable BlockState state) {
		ItemStack item = ItemStack.EMPTY;
		if(state == null)
		{
            WorldStateNode node = this.getNode(WorldStateNode.TYPE);
            if(node != null && node.hasTraderBlock())
				item = new ItemStack(node.getTraderBlock());
		}
		else
			item = new ItemStack(state.getBlock());
		if(!item.isEmpty())
		{
			item.set(ModDataComponents.TRADER_ITEM_DATA,new TraderItemData(this.getID()));
            final ItemStack result = item;
            this.ifNodePresent(WorldStateNode.TYPE,node -> node.addVariantToStack(result));
            item = result;
		}
		//Set State to Ejected
		this.setState(TraderState.EJECTED);
		return new TraderEjectionData(this.getID(),item);
	}

    @Deprecated
	private static TraderData parseOldTrader(CompoundTag compound, HolderLookup.Provider lookup)
	{
        if(compound.contains("Type"))
        {
            String typeString = compound.getString("Type");
            TraderType<?> type = LCRegistries.TRADER_TYPES.get(ResourceLocation.parse(typeString));
            if(type != null)
            {
                TraderData trader = type.create();
                trader.loadOldData(compound,lookup);
                return trader;
            }
            else
                return null;
        }
        return null;
	}
	
	//Network stuff
	public boolean shouldRemove(MinecraftServer server) {
		if(!this.hasWorldPosition())
			return false;
		TraderBlockEntity<?> be = this.getBlockEntity();
		return be != null && be.getTraderID() != this.id;
	}

	//User data
	private int userCount = 0;
	private final List<Player> currentUsers = new ArrayList<>();
	public List<Player> getUsers() { return new ArrayList<>(this.currentUsers); }
	public int getUserCount() { return this.userCount; }
	
	public void userOpen(Player player) {
        this.currentUsers.add(player);
        this.updateUserCount();
    }
	public void userClose(Player player) { this.currentUsers.remove(player); this.updateUserCount(); }
	
	private void updateUserCount() {
		if(this.isServer())
		{
			this.userCount = this.currentUsers.size();
			new SPacketSyncUsers(this.id, this.userCount).sendToAll();
		}
	}
	public void updateUserCount(int userCount)
	{
		if(this.isClient)
			this.userCount = userCount;
	}

	public final List<? extends TradeData> getTradeData() {
        TradeOfferSourceNode<?> source = this.getTradeOfferNode();
        return source == null ? new ArrayList<>() : source.getAllTrades();
    }

	@Nullable
	public final TradeData getTrade(int tradeIndex) {
        for(TraderNode node : this.nodes.values())
        {
            if(node instanceof TradeOfferSourceNode<?> source)
                return source.getTrade(tradeIndex);
        }
        return null;
    }

	public int indexOfTrade(TradeData trade) { return this.getTradeData().indexOf(trade); }

	//ITradeRuleHost Overrides
	@Override
	public final boolean isTrader() { return true; }
	@Override
	public final boolean isTrade() { return false; }

    @Override
    public final Map<TradeRuleType<?>,TradeRule> getRuleMap() { return this.findNodeValue(TraderRulesNode.TYPE, TraderRulesNode::getRules,new HashMap<>()); }
    @Override
    public final void setRuleChanged(TradeRuleType<?> type) {
        this.ifNodePresent(TraderRulesNode.TYPE,node -> node.setRuleChanged(type));
    }
    @Override
    @Nullable
    public final TradeRule addRule(TradeRuleType<?> type) {
        TraderRulesNode node = this.getNode(TraderRulesNode.TYPE);
        if(node != null)
            return node.addRule(type);
        return null;
    }

    @Override
	public boolean canMoneyBeRelevant() {
		List<? extends TradeData> trades = this.getTradeData();
		if(trades != null)
			return trades.stream().anyMatch(t -> t instanceof ITradeRuleHost h && h.canMoneyBeRelevant());
		return true;
	}

	//For Traders, allow rules that affect money if money can be relevant at any point.
	@Override
	public boolean isMoneyRelevant() { return this.canMoneyBeRelevant(); }

    public final TradeResult TryExecuteTrade(TradeContext context, int tradeIndex)
	{

        TradeResult result;
        try {
            result = this.ExecuteTrade(context,tradeIndex);
        } catch (TradeFailedException exception) {
            result = exception.result;
        }
		if(result.isSuccess())
		{
			//Increment trades executed
			this.incrementStat(StatKeys.Traders.TRADES_EXECUTED,1);
            Player player = context.getPlayer();
            //Award stats
            if(player != null)
                player.awardStat(ModStats.STAT_TRADES);
		}
		return result;
	}

	protected abstract TradeResult ExecuteTrade(TradeContext context, int tradeIndex) throws TradeFailedException;

    /**
     * Method to be used with {@link #ExecuteTrade(TradeContext, int)} to obtain a non-null instance of a TraderNode<br>
     * If the node is not present, a {@link TradeFailedException} will be thrown and caught by the parent {@link #TryExecuteTrade(TradeContext, int)} method resulting in the trade failing.<br>
     * Used to assert that the given node <b>must</b> be present in the trader for it to properly execute its trades
     * @param type The Trader Nodes type
     * @return The Node attached to this trader
     * @param <T> The Trader Nodes class
     * @throws TradeFailedException if this trader does not have the given node attached
     */
    protected final <T extends TraderNode> T assertNode(TraderNodeType<T> type) throws TradeFailedException {
        T node = this.getNode(type);
        if(node == null)
            throw new TradeFailedException(type);
        return node;
    }

	public void addInteractionSlots(List<InteractionSlotData> interactionSlots) {}

    public final void initStorageTabs(ITraderStorageMenu menu)
    {
        for(TraderNode node : this.nodes.values())
            node.applyStorageTabs(menu);
        //Second pass for cases where a node wants to override the tab provided by a different node
        for(TraderNode node : this.nodes.values())
            node.applyLateStorageTabs(menu);
    }

	public final void handleSettingsChange(Player player, LazyPacketData message)
	{
        for(TraderNode node : this.nodes.values())
            node.handleSettingsChange(player,message);
	}

	public final void pushLocalNotification(Notification notification)
	{
		if(this.isClient)
			return;
        this.ifNodePresent(LoggerNode.TYPE, node -> node.addLog(notification));
	}
	
	public final void pushNotification(Supplier<Notification> notificationSource) {
		if(this.isClient)
			return;
        this.ifNodePresent(LoggerNode.TYPE,node -> node.postLogs(notificationSource));
	}

	public final <T> void incrementStat(StatKey<?,T> key, T addValue)
	{
        this.ifNodePresent(LoggerNode.TYPE,node -> node.statTracker.incrementStat(key,addValue));
		this.getOwner().getValidOwner().incrementStat(key,addValue);
	}
	
	public final TraderCategory getNotificationCategory() {
		return new TraderCategory(this.findNodeValue(WorldStateNode.TYPE,WorldStateNode::getTraderCategoryBlock,ModItems.TRADING_CORE.get()), this.getName(), this.id, this.findNodeValue(DisplayNode.TYPE,DisplayNode::getCustomIcon));
	}

	
	public final List<TraderData> getTraders() { return this.allowAccess() ? Lists.newArrayList(this) : new ArrayList<>(); }
	public final boolean isSingleTrader() { return true; }
	
	public static MenuProvider getTraderMenuProvider(BlockPos traderSourcePosition, MenuValidator validator) { return new TraderMenuProviderBlock(traderSourcePosition, validator); }

	private record TraderMenuProviderBlock(BlockPos traderSourcePosition, MenuValidator validator) implements EasyMenuProvider {

		@Override
		public AbstractContainerMenu createMenu(int windowID, Inventory inventory,  Player player) { return new TraderMenu.TraderMenuBlockSource(windowID, inventory, this.traderSourcePosition, this.validator); }

	}

	public static MenuProvider getTraderMenuForAllNetworkTraders(MenuValidator validator) { return new TraderMenuAllNetworkProvider(validator); }

	private record TraderMenuAllNetworkProvider(MenuValidator validator) implements EasyMenuProvider{
		@Nullable
		@Override
		public AbstractContainerMenu createMenu(int windowID, Inventory inventory, Player player) {
			return new TraderMenu.TraderMenuAllNetwork(windowID,inventory,this.validator);
		}
	}

	
	public final List<Component> getTerminalInfo(@Nullable Player player)
	{
		List<Component> info = new ArrayList<>();
		for(TraderNode node : this.nodes.values()){
            if(node instanceof ITerminalDisplay display)
                display.addTerminalInfo(info,player);
        }
		return info;
	}

	public int getTerminalTextColor()
	{
		if(!this.hasValidTrade())
			return 0xFF0000;
		if(!this.anyTradeHasStock())
			return 0xFFAA00;
        AtomicInteger color = new AtomicInteger(0x404040);
        for(TraderNode node : this.nodes.values())
        {
            if(node instanceof ITerminalDisplay display)
                display.applyTerminalTextColor(color);
        }
		return color.get();
	}

    protected static <T extends TraderData> Products.P2<RecordCodecBuilder.Mu<T>,Long,Map<TraderNodeType<?>, TraderNode>> baseFields(RecordCodecBuilder.Instance<T> builder) {
        return builder.group(Codec.LONG.fieldOf("id").forGetter(TraderData::getID),
                Codec.unboundedMap(TraderNodeType.CODEC, TraderNode.CODEC).fieldOf("nodes").forGetter(TraderData::getWritableNodes));
    }

}
