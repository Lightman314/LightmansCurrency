package io.github.lightman314.lightmanscurrency.common.traders.paygate.client;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.client.ClientTraderAttachment;
import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.ITraderScreen;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core.addons.MiscTabAddon;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.paygate.PaygateSettingAddon;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.network.message.paygate.CPacketCollectTicketStubs;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientPaygateAttachment extends ClientTraderAttachment {

    public static final ClientTraderAttachment INSTANCE = new ClientPaygateAttachment();

    private ClientPaygateAttachment() { super(); }

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

    private IconButton createTicketStubCollectionButton(PaygateTraderData paygate, Supplier<Player> playerSource, Supplier<Boolean> visible)
    {
        return IconButton.builder()
                .pressAction(() -> new CPacketCollectTicketStubs(paygate.getID()).send())
                .icon(ItemIcon.ofItem(ModItems.TICKET_STUB))
                .addon(EasyAddonHelper.toggleTooltip(() -> paygate.getStoredTicketStubs() > 0, () -> LCText.TOOLTIP_TRADER_PAYGATE_COLLECT_TICKET_STUBS.get(paygate.getStoredTicketStubs()), EasyText::empty))
                .addon(EasyAddonHelper.visibleCheck(() -> paygate.areTicketStubsRelevant() && paygate.hasPermission(playerSource.get(), Permissions.OPEN_STORAGE) && visible.get()))
                .addon(EasyAddonHelper.activeCheck(() -> paygate.getStoredTicketStubs() > 0))
                .build();
    }

    @Override
    public void addMiscTabAddons(TraderData trader, List<MiscTabAddon> addons) {
        addons.add(PaygateSettingAddon.INSTANCE);
    }

}