package io.github.lightman314.lightmanscurrency.common.traders.input.client;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.traders.client.ClientTraderAttachment;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input.InputTabAddon;
import io.github.lightman314.lightmanscurrency.common.traders.input.InputTraderData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ClientInputSubtraderAttachment extends ClientTraderAttachment {

    public static final ClientInputSubtraderAttachment ITEM_TRADER = new ClientInputSubtraderAttachment(ItemIcon.ofItem(Items.HOPPER),LCText.TOOLTIP_TRADER_SETTINGS_INPUT_ITEM.get());

    private final IconData settingsTabIcon;
    private final Component settingsTabTooltip;
    public ClientInputSubtraderAttachment(IconData settingsTabIcon, Component settingsTabName) { this.settingsTabIcon = settingsTabIcon; this.settingsTabTooltip = settingsTabName; }

    public IconData getSettingsTabIcon(InputTraderData trader) { return this.settingsTabIcon; }
    public Component getSettingsTabTooltip(InputTraderData trader) { return this.settingsTabTooltip; }

    public List<? extends InputTabAddon> getInputSettingsAddons(InputTraderData trader) { return new ArrayList<>(); }

}
