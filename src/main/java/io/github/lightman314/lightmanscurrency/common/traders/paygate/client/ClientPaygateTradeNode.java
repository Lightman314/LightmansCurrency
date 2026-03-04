package io.github.lightman314.lightmanscurrency.common.traders.paygate.client;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin.misc.MiscTabAddon;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.ClientTradeOfferNode;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.OutputConflictHandling;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.nodes.PaygateTradeNode;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ClientPaygateTradeNode extends ClientTradeOfferNode<PaygateTradeNode> {

    public ClientPaygateTradeNode(PaygateTradeNode node) {
        super(node);
    }

    @Override
    public void addMiscTabAddons(TraderData trader, List<MiscTabAddon> addons) {
        addons.add(new PaygateMiscAddon());
    }
    
    private static class PaygateMiscAddon extends MiscTabAddon
    {

        @Override
        public void addWidgets(SettingsSubTab tab, ScreenArea screenArea, boolean firstOpen) {
            //Label for the dropdown
            this.addLabel(LCText.GUI_TRADER_PAYGATE_CONFLICT_LABEL);

            List<Component> options = new ArrayList<>();
            for(OutputConflictHandling type : OutputConflictHandling.values())
                options.add(LCText.GUI_TRADER_PAYGATE_CONFLICT_HANDLING.get(type).get());
            tab.addChild(DropdownWidget.builder()
                    .width(DEFAULT_WIDTH)
                    .options(options)
                    .selected(this.getSelected())
                    .selectAction(this::selectType)
                    .build());
        }

        private int getSelected() {
            PaygateTradeNode node = this.getNode(PaygateTradeNode.TYPE);
            return node == null ? 0 : node.getConflictHandling().ordinal();
        }

        private void selectType(int selected)
        {
            if(this.getTab() == null)
                return;
            this.getTab().sendMessage(this.builder().setInt("ChangeConflictMode",selected));
        }

    }
}
