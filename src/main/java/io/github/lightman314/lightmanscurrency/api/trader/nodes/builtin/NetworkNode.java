package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderArguments;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.INetworkController;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IUpgradeUser;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCUpgrades;

import java.util.Optional;

public class NetworkNode extends SimpleSyncedNode implements INetworkController, IUpgradeUser {

    private static final MapCodec<NetworkNode> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.BOOL.fieldOf("alwaysShow").forGetter(n -> n.alwaysShow)
    ).apply(builder,NetworkNode::new));
    public static final TraderNodeType<NetworkNode> TYPE = TraderNodeType.simple(NetworkNode::new,CODEC);

    private boolean alwaysShow = false;
    private NetworkNode() { }
    private NetworkNode(boolean alwaysShow) { this.alwaysShow = alwaysShow; }

    @Override
    public void updateArgument(TraderArguments arguments) {
        Optional<Boolean> arg = arguments.tryGet(TYPE,Boolean.class);
        if(arg.isPresent())
            this.alwaysShow = arg.get();
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public boolean visibleToNetwork() {
        if(this.alwaysShow)
            return true;
        //Check for network upgrade
        return this.getNodeArgValue(UpgradeNode.TYPE,LCUpgrades.NETWORK,UpgradeNode::hasUpgrade,false);
    }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        builder.setBoolean("alwaysShow",this.alwaysShow);
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        this.alwaysShow = data.getBoolean("alwaysShow",this.alwaysShow);
    }

    @Override
    public boolean allowUpgrade(UpgradeType type) {
        if(this.alwaysShow)
            return false;
        //Allow network upgrades if we aren't permanently visible to the network
        return type.is(LCUpgrades.NETWORK);
    }

}