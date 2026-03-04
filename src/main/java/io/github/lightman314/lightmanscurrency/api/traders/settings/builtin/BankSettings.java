package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderNodeSettings;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.BankNode;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public class BankSettings extends EasyTraderNodeSettings<TraderData, BankNode> {

    public BankSettings(TraderData trader,BankNode node) { super("bank", trader, node); }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_TRADER_BANK.get(); }

    @Override
    protected String getRequiredPermission() { return Permissions.BANK_LINK; }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        data.setBooleanValue("linked_to_bank",this.node.isLinkedToBank());
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        this.node.setLinkedToBank(null,data.getBooleanValue("linked_to_bank"));
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_BANK_LINK.get(), data.getBooleanValue("linked_to_bank")));
    }

}
