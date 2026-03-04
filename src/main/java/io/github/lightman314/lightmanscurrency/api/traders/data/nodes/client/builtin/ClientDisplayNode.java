package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.client.widgets.ToggleOption;
import io.github.lightman314.lightmanscurrency.api.traders.client.*;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin.NameTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin.misc.MiscTabAddon;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.DisplayNode;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;

import java.util.List;

public class ClientDisplayNode extends ClientTraderNode<DisplayNode> implements IClientPermissionProvider, IClientSettingsTabProvider, IClientMiscTabAddonProvider {

    public ClientDisplayNode(DisplayNode node) { super(node); }

    @Override
    public void addPermissionOptions(TraderData trader, PermissionOptionsBuilder builder) {
        builder.addSimple(Permissions.CHANGE_NAME);
    }

    @Override
    public void addSettingsTabs(TraderData trader, TraderSettingsClientTab tab, SettingsTabBuilder builder) {
        builder.add(new NameTab(tab),-100);
    }

    @Override
    public void addMiscTabAddons(TraderData trader, List<MiscTabAddon> addons) {
        addons.add(new DisplayMiscAddon());
    }

    private static class DisplayMiscAddon extends MiscTabAddon
    {
        @Override
        public void addWidgets(SettingsSubTab tab, ScreenArea screenArea, boolean firstOpen) {
            this.addWidget(ToggleOption.builder()
                    .width(DEFAULT_WIDTH)
                    .currentValue(this::alwaysShowSearchBox)
                    .handler(this::ToggleShowSearchBox)
                    .label(LCText.GUI_TRADER_SETTINGS_ENABLE_SHOW_SEARCH_BOX)
                    .build());
        }
        private boolean alwaysShowSearchBox() {
            DisplayNode node = this.getNode(DisplayNode.TYPE);
            return node != null && node.alwaysShowSearchBox();
        }
        private void ToggleShowSearchBox() {
            DisplayNode node = this.getNode(DisplayNode.TYPE);
            if(node == null)
                return;
            this.sendMessage(this.builder().setBoolean("AlwaysShowSearchBox", !node.alwaysShowSearchBox()));
        }
    }

}
