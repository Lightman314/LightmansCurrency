package io.github.lightman314.lightmanscurrency.client.features.tickets;

import io.github.lightman314.lightmanscurrency.api.client.gui.screen.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces.ICustomerScreenListener;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces.IStorageScreenListener;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.TraderCustomerMenu;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import io.github.lightman314.lightmanscurrency.features.tickets.nodes.TicketStubStorageNode;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketCollectTicketStubs;
import net.minecraft.world.entity.player.Player;

import java.util.function.BooleanSupplier;

public class ClientTicketStubStorageNode extends ClientTraderNode implements IStorageScreenListener, ICustomerScreenListener {

    public static final ClientTraderNode INSTANCE = new ClientTicketStubStorageNode();

    @Override
    public void onCustomerScreenInit(TraderSource source,IWidgetPositioner edgePositioner,IWidgetHolder screen,TraderCustomerMenu menu) {
        if(source.isSimple())
            this.addCollectTicketButton(menu.getPlayer(),screen,edgePositioner,() -> true,source);
    }

    @Override
    public void onStorageScreenInit(TraderStorageScreen screen) {
        this.addCollectTicketButton(screen.getPlayer(),screen,screen.rightSidePositioner(),screen::showRightEdgeWidgets,TraderSource.deferred(screen::getTrader));
    }

    private void addCollectTicketButton(Player player,IWidgetHolder screen,IWidgetPositioner positioner,BooleanSupplier edgeVisible,TraderSource source) {
        IconButton button = IconButton.builder()
                .withIcon(ItemIcon.of(LCItems.TICKET_STUB))
                .visible(() -> {
                    TraderData trader = source.getSimpleTrader();
                    return trader != null && trader.getNodeValue(TicketStubStorageNode.TYPE,TicketStubStorageNode::areTicketStubsRelevant,false) && edgeVisible.getAsBoolean() && trader.getPermission(player,LCPermissions.OPEN_STORAGE);
                })
                .active(() -> {
                    TraderData trader = source.getSimpleTrader();
                    return trader != null && trader.getNodeValue(TicketStubStorageNode.TYPE,TicketStubStorageNode::getStubCount,0L) > 0;
                })
                .tooltip(TooltipSource.deferredSingle(() -> {
                    TraderData trader = source.getSimpleTrader();
                    long count = trader == null ? 0 : trader.getNodeValue(TicketStubStorageNode.TYPE,TicketStubStorageNode::getStubCount);
                    return TicketStubStorageNode.TOOLTIP_COLLECT_STUBS.get(count);
                }))
                .onPress(CPacketCollectTicketStubs::sendToServer)
                .build();
        screen.addChild(button);
        positioner.addWidgets(button);
    }

}