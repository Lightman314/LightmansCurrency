package io.github.lightman314.lightmanscurrency.api.trader.nodes;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.IRegistryAccess;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import javax.annotation.Nullable;

import java.util.*;

public abstract class TraderNode implements TypedInstance<TraderNodeType<?>>,ISidedContext,IRegistryAccess,INodeAccess {

    public static final Codec<Map<TraderNodeType<?>,TraderNode>> SET_CODEC = Codec.dispatchedMap(LCRegistries.Trader.TRADER_NODE_TYPE.byNameCodec(),TraderNodeType::fullCodec);
    public static final Codec<TraderNode> CODEC = LCRegistries.Trader.TRADER_NODE_TYPE.byNameCodec()
            .dispatch(TraderNode::getType,TraderNodeType::codec);

    private TraderData trader = null;
    @Override
    public final TraderData getTrader() { return Objects.requireNonNull(this.trader,"Attempted to access the nodes trader before it's been attached!"); }

    @Override
    public final boolean isClient() { return this.trader == null || this.trader.isClient(); }

    @Override
    public Holder<TraderNodeType<?>> typeHolder() { return LCRegistries.Trader.TRADER_NODE_TYPE.wrapAsHolder(this.getType()); }

    @Override
    public final HolderLookup.Provider registryAccess() { return this.trader.registryAccess(); }

    public final void setChanged() { this.trader.setChanged(this); }
    public final void setChangedNoPacket() { this.trader.setChangedNoPacket(); }

    public final String getKey() { return LCRegistries.Trader.TRADER_NODE_TYPE.getKey(this.getType()).toString(); }
    public abstract TraderNodeType<?> getType();

    /**
     * Called during the TraderData constructor to give the node access to its trader for future methods<br>
     * Should not be called externally
     */
    @ApiStatus.Internal
    public final void pairWithTrader(TraderData trader) {
        if(this.trader != null)
            throw new IllegalStateException("Cannot initialize a node more than once!");
        this.trader = trader;
    }

    @Override
    public final boolean hasNode(TraderNodeType<?> type) { return this.trader != null && this.trader.hasNode(type); }

    @Override
    @Nullable
    public final <T extends TraderNode> T getNode(TraderNodeType<T> type) { return this.trader == null ? null : this.trader.getNode(type); }

    @Override
    public final List<TraderNode> getAllNodes() { return this.trader == null ? ImmutableList.of() : this.trader.getAllNodes(); }

    public void updateArgument(TraderArguments arguments) {}

    /**
     * Called after the trader is initialized and is stored in the trader data cache
     */
    public void onAttach() { }

    protected final void postNotification(Notification notification) {
        if(this.trader != null)
            this.trader.postNotification(notification,true);
    }
    protected final void postInternalNotification(Notification notification) {
        if(this.trader != null)
            this.trader.postNotification(notification,false);
    }

    public final <T> T getPermission(Player player, Permission<T> permission) { return this.trader.getPermission(player,permission); }
    public final <T> T getPermission(PlayerReference player, Permission<T> permission) { return this.trader.getPermission(player,permission); }

}