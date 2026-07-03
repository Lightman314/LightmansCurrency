package io.github.lightman314.lightmanscurrency.api.trader.nodes;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.IRegistryAccess;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class TraderNode implements ISidedContext, IRegistryAccess, INodeAccess {

    public static final Codec<Map<TraderNodeType<?>,TraderNode>> MAP_CODEC = Codec.dispatchedMap(LCRegistries.Trader.TRADER_NODE_TYPE.byNameCodec(),TraderNodeType::codec);
    public static final Codec<TraderNode> CODEC = LCRegistries.Trader.TRADER_NODE_TYPE.byNameCodec()
            .dispatch(TraderNode::getType,TraderNodeType::mapCodec);

    @ApiStatus.Internal
    protected TraderData trader = null;
    public final TraderData getTrader() { return Objects.requireNonNull(this.trader,"Attempted to access the nodes trader before it's been attached!"); }
    @Override
    public final HolderLookup.Provider registryAccess() { return this.getTrader().registryAccess(); }

    @Override
    public final boolean isClient() { return this.trader == null || this.trader.isClient(); }

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

    public void updateArgument(Optional<Object> argument) {}

    /**
     * Called after the trader is initialized and is stored in the trader data cache
     */
    public void onAttach() { }

    public final <T> T getPermission(Player player, Permission<T> permission) { return this.trader.getPermission(player,permission); }
    public final <T> T getPermission(PlayerReference player, Permission<T> permission) { return this.trader.getPermission(player,permission); }

}