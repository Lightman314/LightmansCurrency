package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerHolder;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IOwnerListener;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IOwnerSource;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionSource;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;

import java.util.Optional;

public class OwnerNode extends SimpleSyncedNode implements IOwnerSource, IPermissionSource {

    private static final MapCodec<OwnerNode> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            OwnerHolder.CODEC.fieldOf("owner").forGetter(OwnerNode::getOwner)
    ).apply(builder,OwnerNode::new));

    public static final TraderNodeType<OwnerNode> TYPE = TraderNodeType.simple(OwnerNode::new,CODEC);

    private final OwnerHolder owner = new OwnerHolder(this,this::onOwnerChanged);
    public OwnerHolder getOwner() { return this.owner; }

    private OwnerNode() {}
    private OwnerNode(OwnerHolder owner) { this.owner.copyFrom(owner); }

    @Override
    public void updateArgument(Optional<Object> argument) {
        if(argument.isPresent() && argument.get() instanceof Owner startingOwner)
            this.owner.setOwner(startingOwner);
    }

    private void onOwnerChanged() {
        this.setChanged(packet -> packet.set("owner", LCFancyPacketTypes.OWNER_HOLDER,this.owner));
        if(this.isServer())
        {
            for(IOwnerListener listener : this.trader.getNodes(IOwnerListener.class))
                listener.onOwnerChanged();
        }
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void onDataSync(FancyPacketMap data) {
        if(data.contains("owner"))
            this.owner.copyFrom(data.get("owner", LCFancyPacketTypes.OWNER_HOLDER,this.owner));
    }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        builder.set("owner", LCFancyPacketTypes.OWNER_HOLDER,this.owner);
    }

    @Override
    public Optional<OwnerHolder> getValidOwner() { return Optional.of(this.owner); }

    @Override
    public <T> T getPlayerPermission(PlayerReference player, Permission<T> permission) {
        if(this.owner.isAdmin(player))
            return permission.getMaxValue();
        //Get ally permissions if member
        if(this.owner.isMember(player))
            return this.getAllyPermission(permission);
        return permission.getEmpty();
    }

    private <T> T getAllyPermission(Permission<T> permission)
    {
        AlliesNode node = this.trader.getNode(AlliesNode.TYPE);
        if(node != null)
            return node.getAllyPermissionValue(permission);
        return permission.getEmpty();
    }

}