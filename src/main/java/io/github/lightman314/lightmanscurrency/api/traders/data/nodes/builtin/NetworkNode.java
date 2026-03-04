package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.events.TraderEvent;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.INetworkController;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IUpgradeHandler;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeStackHandler;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class NetworkNode extends SyncedTraderNode implements IUpgradeHandler, INetworkController {

    private static final MapCodec<NetworkNode> MAP_CODEC = Codec.BOOL.fieldOf("network").xmap(NetworkNode::new,NetworkNode::isAlwaysNetworkTrader);

    public static final TraderNodeType<NetworkNode> TYPE = TraderNodeType.advanced(NetworkNode::factory,MAP_CODEC);

    private boolean hadNetworkUpgrade = false;
    private boolean alwaysNetworkTrader;
    public boolean isAlwaysNetworkTrader() { return this.alwaysNetworkTrader; }
    @Override
    public boolean visibleToNetwork() {
        if(this.trader != null)
            return this.isAlwaysNetworkTrader() || this.trader.hasUpgrade(Upgrades.NETWORK);
        return false;
    }

    private NetworkNode(boolean networkTrader) { this.alwaysNetworkTrader = networkTrader; }

    @Override
    public void onAttach() {
        if(!this.alwaysNetworkTrader)
            this.hadNetworkUpgrade = this.trader.hasUpgrade(Upgrades.NETWORK);
    }

    @Override
    public void updateArgument(@Nullable Object argument) {
        if(argument instanceof Boolean network)
            this.alwaysNetworkTrader = network;
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,Player player) {
        builder.setBoolean("network",this.alwaysNetworkTrader);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("network"))
            this.alwaysNetworkTrader = data.getBoolean("network");
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains("AlwaysShowOnTerminal"))
            this.alwaysNetworkTrader = tag.getBoolean("AlwaysShowOnTerminal");
    }

    @Override
    public boolean allowUpgrade(UpgradeType upgrade) { return !this.alwaysNetworkTrader && upgrade == Upgrades.NETWORK; }

    @Override
    public void afterUpgradesChanged(UpgradeStackHandler container) {
        if(!this.alwaysNetworkTrader)
        {
            boolean newState = container.hasUpgrade(Upgrades.NETWORK);
            //Send event informing traders of the change in network status
            if(newState != this.hadNetworkUpgrade)
                NeoForge.EVENT_BUS.post(new TraderEvent.TraderNetworkStatusUpdated(this.trader.getID()));
            this.hadNetworkUpgrade = newState;
        }
    }

    @Override
    public void initializeAllyPermissions(BiConsumer<String, Integer> defaultConsumer) {
        defaultConsumer.accept(Permissions.INTERACTION_LINK, 0);
    }

    private static NetworkNode factory(@Nullable Object argument) {
        if(argument instanceof Boolean network)
            return new NetworkNode(network);
        return new NetworkNode(false);
    }

}
