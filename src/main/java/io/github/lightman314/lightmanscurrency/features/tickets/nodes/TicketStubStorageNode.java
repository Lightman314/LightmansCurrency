package io.github.lightman314.lightmanscurrency.features.tickets.nodes;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnerHolder;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ITradeResourceProvider;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ITraderDestructionListener;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceCollector;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import io.github.lightman314.lightmanscurrency.features.tickets.TicketTradeResources;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub.UnlimitedStubStorage;
import io.github.lightman314.lightmanscurrency.features.tickets.trader.TicketPrice;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Consumer;


public class TicketStubStorageNode extends SimpleSyncedNode implements ITradeResourceProvider, ITraderDestructionListener {

    public static final TraderNodeType<TicketStubStorageNode> TYPE = TraderNodeType.simple(TicketStubStorageNode::new,UnlimitedStubStorage.CODEC.xmap(TicketStubStorageNode::new, n -> n.storage).fieldOf("storage"));

    public static final TextEntry TOOLTIP_COLLECT_STUBS = TextEntry.tooltip(LCApi.MODID,"trader.ticket_stub_storage.collect");

    private final UnlimitedStubStorage storage = new UnlimitedStubStorage()
            .withListener(this::setStorageChanged);

    private TicketStubStorageNode() {}
    private TicketStubStorageNode(UnlimitedStubStorage storage) { this.storage.copyFrom(storage); }

    public boolean areTicketStubsRelevant() {
        //If a ticket stub is currently stored, then ticket stubs are relevant
        if(!this.storage.isEmpty())
            return true;
        //Ticket Stubs are only relevant if a valid trade is a ticket price and th
        for(TradingNode<?> node : this.getTradingNodes()) {
            for(TradeData trade : node.getTrades()) {
                if(trade.isCustomerReady() && trade.getInternalPrice() instanceof TicketPrice price && price.storeTicketStubs())
                    return true;
            }
        }
        return false;
    }
    public long getStubCount() { return this.storage.getStubCount(); }

    @Override
    public TraderNodeType<?> getType() {
        return TYPE;
    }

    private void setStorageChanged(Consumer<FancyPacketMap.Mutable> change) {
        this.setChanged(change);
    }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        builder.write(this.storage);
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        this.storage.handlePacket(data);
    }

    public void tryGiveToPlayer(Player player) {
        if(this.getPermission(player,LCPermissions.OPEN_STORAGE))
            this.storage.giveToPlayer(player);
    }

    @Override
    public void attachResource(ResourceCollector collector) {
        collector.addResource(TicketTradeResources.STUB_COLLECTOR,this.storage);
    }

    @Override
    public void onTraderDestroyed(IOwnerHolder owner,Consumer<ItemStack> itemSpawner,Optional<MoneyResourceHandler> playerMoney) {
        this.storage.dumpContents(itemSpawner,false);
    }

}