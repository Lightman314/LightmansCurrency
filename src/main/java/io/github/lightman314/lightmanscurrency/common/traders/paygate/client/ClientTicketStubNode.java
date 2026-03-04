package io.github.lightman314.lightmanscurrency.common.traders.paygate.client;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.traders.client.IClientScreenListener;
import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.client.ITraderScreen;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.ITraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.nodes.TicketStubNode;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.network.message.paygate.CPacketCollectTicketStubs;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientTicketStubNode extends ClientTraderNode<TicketStubNode> implements IClientScreenListener {

    public ClientTicketStubNode(TicketStubNode node) { super(node); }

    @Override
    public void onScreenInit(TraderData trader, ITraderScreen screen, Consumer<Object> addWidget) {
        if(trader instanceof PaygateTraderData paygate)
        {
            //Add Collect Ticket Stub button
            IconButton button = this.createTicketStubCollectionButton(paygate,() -> screen.getMenu().getPlayer(),() -> true);
            addWidget.accept(button);
            screen.getRightEdgePositioner().addWidget(button);
        }
    }

    @Override
    public void onStorageScreenInit(TraderData trader, ITraderStorageScreen screen, Consumer<Object> addWidget) {
        if(trader instanceof PaygateTraderData paygate)
        {
            //Add Collect Ticket Stub button
            IconButton button = this.createTicketStubCollectionButton(paygate,() -> screen.getMenu().getPlayer(),() -> true);
            addWidget.accept(button);
            screen.getRightEdgePositioner().addWidget(button);
        }
    }

    private IconButton createTicketStubCollectionButton(TraderData trader,Supplier<Player> playerSource, Supplier<Boolean> visible)
    {
        return IconButton.builder()
                .pressAction(() -> new CPacketCollectTicketStubs(trader.getID()).sendToServer())
                .icon(ItemIcon.ofItem(ModItems.TICKET_STUB))
                .addon(EasyAddonHelper.toggleTooltip(() -> this.node.getStoredTicketStubs() > 0, () -> LCText.TOOLTIP_TRADER_PAYGATE_COLLECT_TICKET_STUBS.get(this.node.getStoredTicketStubs()), EasyText::empty))
                .addon(EasyAddonHelper.visibleCheck(() -> this.node.areTicketStubsRelevant() && trader.hasPermission(playerSource.get(), Permissions.OPEN_STORAGE) && visible.get()))
                .addon(EasyAddonHelper.activeCheck(() -> this.node.getStoredTicketStubs() > 0))
                .build();
    }

}
