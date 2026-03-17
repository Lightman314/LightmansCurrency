package io.github.lightman314.lightmanscurrency.api.traders.data.nodes;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.IBuilderProvider;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.ISyncingNode;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;

import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class TraderNode implements IClientTracker, IBuilderProvider {

    public static final Codec<TraderNode> CODEC = LCRegistries.TRADER_NODE.byNameCodec().dispatch(TraderNode::getType,TraderNodeType::codec);
    public static final Codec<Map<TraderNodeType<?>, TraderNode>> SET_CODEC = Codec.unboundedMap(TraderNodeType.CODEC,CODEC);

    @Nullable
    protected TraderData trader = null;
    @Nullable
    public TraderData getTrader() { return this.trader; }
    @Override
    public final HolderLookup.Provider registryAccess() { return this.trader.registryAccess(); }

    protected TraderNode() { }

    @Override
    public final boolean isClient() { return this.trader == null || this.trader.isClient(); }
    @Override
    public final boolean isServer() { return IClientTracker.super.isServer(); }

    public final void attach(TraderData trader) {
        if(this.trader != null)
            throw new IllegalStateException("Cannot initialize a node more than once!");
        this.trader = trader;
    }

    public void updateArgument(@Nullable Object argument) { }

    /**
     * Called after the trader is initialized
     */
    public void onAttach() {}

    public boolean hasNoConflicts(TraderData trader,Map<TraderNodeType<?>, TraderNode> partialMap) { return true; }

    public void onRegisteredToOffice() {}

    protected final void setChangedNoPacket() { this.trader.setChangedNoPacket(); }

    public abstract TraderNodeType<?> getType();
    public final void handleSyncPacket(LazyPacketData data)
    {
        if(this.isServer())
            return;
        if(this instanceof ISyncingNode node)
            node.onDataSync(data);
    }

    public abstract void loadOldData(CompoundTag tag, HolderLookup.Provider lookup);

    //Easy Permissions Access
    protected final boolean hasPermission(Player player,String permission) { return this.trader != null && this.trader.hasPermission(player, permission); }
    protected final boolean hasPermission(PlayerReference player, String permission) { return this.trader != null && this.trader.hasPermission(player, permission); }
    protected final int getPermissionLevel(Player player, String permission) { return this.trader == null ? 0 : this.trader.getPermissionLevel(player, permission); }
    protected final int getPermissionLevel(PlayerReference player, String permission) { return this.trader == null ? 0 : this.trader.getPermissionLevel(player, permission); }

    protected final void pushLocalNotification(Notification notification) { if(this.trader != null) this.trader.pushLocalNotification(notification); }
    protected final void pushNotification(Supplier<Notification> notification) { if(this.trader != null) this.trader.pushNotification(notification); }

    public void initializeAllyPermissions(BiConsumer<String,Integer> defaultConsumer) { }
    public boolean blockPermission(String permission) { return false; }
    public void handleSettingsChange(Player player, LazyPacketData message) { }

    public void registerSettingsNodes(TraderData trader, Consumer<SettingsNode> consumer) { }

    public void applyStorageTabs(ITraderStorageMenu menu) { }
    public void applyLateStorageTabs(ITraderStorageMenu menu) { }

}