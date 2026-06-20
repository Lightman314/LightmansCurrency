package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.UnlimitedMoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionUser;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ITradeResourceProvider;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceCollector;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;

import java.util.List;
import java.util.function.Consumer;

public class MoneyStorageNode extends SimpleSyncedNode implements IPermissionUser, ITradeResourceProvider {

    private static final MapCodec<MoneyStorageNode> MAP_CODEC = MoneyValue.NON_EMPTY_OR_FREE_CODEC.listOf().fieldOf("storage")
            .xmap(MoneyStorageNode::new,n -> n.getStorage().getAllResources());
    public static final TraderNodeType<MoneyStorageNode> TYPE = TraderNodeType.simple(MoneyStorageNode::new,MAP_CODEC);

    private final UnlimitedMoneyStorage storage = new UnlimitedMoneyStorage().withListener(this::onStorageChanged);
    public UnlimitedMoneyStorage getStorage() { return this.storage; }
    private MoneyStorageNode() { }
    private MoneyStorageNode(List<MoneyValue> storage) {
        this.storage.copyFrom(storage);
    }

    private void onStorageChanged() {
        this.setChanged(builder -> builder.setList("storage",LCFancyPacketTypes.MONEY,this.storage.getAllResources()));
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder,ISyncingContext context) {
        builder.setList("storage",LCFancyPacketTypes.MONEY,this.storage.getAllResources());
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        if(data.contains("storage"))
            this.storage.copyFrom(data.getList("storage",LCFancyPacketTypes.MONEY));
    }

    @Override
    public void addDefaultAllyPermission(Consumer<Permission<?>> handler) {
        handler.accept(BuiltInPermissions.COLLECT_MONEY);
        handler.accept(BuiltInPermissions.STORE_MONEY);
    }

    @Override
    public void attachResource(ResourceCollector collector) {

    }

}