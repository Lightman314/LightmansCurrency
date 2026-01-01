package io.github.lightman314.lightmanscurrency.common.traders.input.client;

import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input.InputTabAddon;
import io.github.lightman314.lightmanscurrency.common.traders.input.InputTraderData;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ClientInputTraderHooks {

    @SuppressWarnings("deprecation")
    public static IconData getSettingsTabIcon(InputTraderData trader) {
        for(Object ca : trader.getClientAttachments())
        {
            if(ca instanceof ClientInputSubtraderAttachment attachment)
                return attachment.getSettingsTabIcon(trader);
        }
        return trader.inputSettingsTabIcon();
    }

    @SuppressWarnings("deprecation")
    public static Component getSettingsTabTooltip(InputTraderData trader) {
        for(Object ca : trader.getClientAttachments())
        {
            if(ca instanceof ClientInputSubtraderAttachment attachment)
                return attachment.getSettingsTabTooltip(trader);
        }
        return trader.inputSettingsTabTooltip();
    }

    @SuppressWarnings("deprecation")
    public static List<InputTabAddon> getSettingsTabAddons(InputTraderData trader) {
        List<InputTabAddon> addons = new ArrayList<>();
        for(Object ca : trader.getClientAttachments())
        {
            if(ca instanceof ClientInputSubtraderAttachment attachment)
                addons.addAll(attachment.getInputSettingsAddons(trader));
        }
        addons.addAll(trader.inputSettingsAddons());
        return addons;
    }

}