package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.client.widgets.ToggleOption;
import io.github.lightman314.lightmanscurrency.api.traders.client.IClientMiscTabAddonProvider;
import io.github.lightman314.lightmanscurrency.api.traders.client.IClientPermissionProvider;
import io.github.lightman314.lightmanscurrency.api.traders.client.PermissionOptionsBuilder;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin.misc.MiscTabAddon;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.BankNode;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;

import java.util.List;

public class ClientBankNode extends ClientTraderNode<BankNode> implements IClientPermissionProvider, IClientMiscTabAddonProvider {

    public ClientBankNode(BankNode node) { super(node); }

    @Override
    public void addPermissionOptions(TraderData trader, PermissionOptionsBuilder builder) {
        builder.addSimple(Permissions.BANK_LINK);
    }

    @Override
    public void addMiscTabAddons(TraderData trader, List<MiscTabAddon> addons) {
        addons.add(new BankMiscAddon());
    }

    private static class BankMiscAddon extends MiscTabAddon
    {
        @Override
        public void addWidgets(SettingsSubTab tab, ScreenArea screenArea, boolean firstOpen) {
            this.addWidget(ToggleOption.builder()
                    .width(DEFAULT_WIDTH)
                    .currentValue(this::linkedToBank)
                    .handler(this::ToggleBankLink)
                    .label(LCText.GUI_SETTINGS_BANK_LINK)
                    .addon(EasyAddonHelper.visibleCheck(this::showBankLink))
                    .addon(EasyAddonHelper.activeCheck(this::bankLinkPossible))
                    .build());
        }

        private boolean linkedToBank() {
            BankNode node = this.getNode(BankNode.TYPE);
            return node != null && node.isLinkedToBank();
        }
        private boolean showBankLink() {
            TraderData t = this.getTrader();
            return this.hasPermissions(Permissions.BANK_LINK) && t.shouldStoreMoney();
        }
        private boolean bankLinkPossible() {
            BankNode node = this.getNode(BankNode.TYPE);
            return node != null && (node.canLinkBankAccount() || node.isLinkedToBank());
        }

        private void ToggleBankLink()
        {
            BankNode node = this.getNode(BankNode.TYPE);
            if(node == null)
                return;
            this.sendMessage(this.builder().setBoolean("LinkToBankAccount", !node.isLinkedToBank()));
        }

    }

}
