package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.ownership.holder.LockedOwnerHolder;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnerHolder;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.*;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.features.commands.LCAdminCommand;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

public class PersistentDataNode extends SimpleSyncedNode implements IUnitNode, INetworkController, IAdminSettingProvider, IOwnerSource, IAdminInfoProvider {

    public static final TraderNodeType<PersistentDataNode> TYPE = TraderNodeType.unit(PersistentDataNode::new);

    private Optional<Data> persistentKey = Optional.empty();
    public boolean isPersistent() { return this.persistentKey.isPresent(); }
    public String getPersistentKey() { return this.persistentKey.map(Data::key).orElse(""); }
    @ApiStatus.Internal
    public void flagAsPersistent(String key,Component owner) { this.persistentKey = Optional.of(new Data(key,owner)); }
    private Optional<IOwnerHolder> owner = Optional.empty();

    public static final TextEntry COMMAND_PERSISTENT_ID = LCAdminCommand.traderListText(TYPE,"persistent_id");

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
    public void modifyInfiniteStock(MutableBoolean currentState) {
        if(this.isPersistent())
            currentState.setTrue();
    }

    @Override
    public void modifyStorePrice(MutableBoolean currentState) {
        if(this.isPersistent())
            currentState.setFalse();
    }

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

    @Override
    public void displayAdminListData(Builder builder) {
        if(this.persistentKey.isPresent())
            builder.accept(-990000,COMMAND_PERSISTENT_ID.get(this.persistentKey.get().key));
    }

    private record Data(String key,Component owner) { }

}