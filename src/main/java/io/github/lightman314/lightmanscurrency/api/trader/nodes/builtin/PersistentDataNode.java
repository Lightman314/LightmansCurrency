package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.ownership.holder.LockedOwnerHolder;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnerHolder;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IAdminSettingProvider;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.INetworkController;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IOwnerSource;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IUnitNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

public class PersistentDataNode extends SimpleSyncedNode implements IUnitNode, INetworkController, IAdminSettingProvider, IOwnerSource {

    public static final TraderNodeType<PersistentDataNode> TYPE = TraderNodeType.unit(PersistentDataNode::new);

    private Optional<Data> persistentKey = Optional.empty();
    public boolean isPersistent() { return this.persistentKey.isPresent(); }
    public String getPersistentKey() { return this.persistentKey.isPresent() ? this.persistentKey.get().key() : ""; }
    @ApiStatus.Internal
    public void flagAsPersistent(String key,Component owner) { this.persistentKey = Optional.of(new Data(key,owner)); }
    private Optional<IOwnerHolder> owner = Optional.empty();

    private PersistentDataNode() {}

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) { }

    @Override
    public void traderCreatePacket(FancyPacketMap.Mutable builder) {
        if(this.persistentKey.isPresent()) {
            Data data = this.persistentKey.get();
            builder.setString("key",data.key)
                    .setText("owner",data.owner);
        }
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        if(data.contains("key") && data.contains("owner")) {
            Data pd = new Data(data.getString("key"),data.getText("owner"));
            this.persistentKey = Optional.of(pd);
            this.owner = Optional.empty();
        }
    }

    @Override
    public boolean hasInfiniteStock(boolean currentState) { return this.isPersistent(); }

    @Override
    public boolean visibleToNetwork() { return this.isPersistent(); }

    @Override
    public Optional<IOwnerHolder> getValidOwner() {
        this.verifyOwnerState();
        return this.owner;
    }

    private void verifyOwnerState() {
        if(this.persistentKey.isPresent() && this.owner.isEmpty())
            this.owner = Optional.of(new LockedOwnerHolder(this.persistentKey.get().owner(),this));
    }

    private record Data(String key,Component owner) { }

}