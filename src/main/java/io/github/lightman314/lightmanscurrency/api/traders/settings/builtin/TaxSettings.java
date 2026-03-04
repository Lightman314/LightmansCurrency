package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.TaxesNode;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderNodeSettings;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public class TaxSettings extends EasyTraderNodeSettings<TraderData, TaxesNode> {

    public TaxSettings(TraderData trader,TaxesNode node) { super("taxes", trader, node); }

    @Override
    protected String getRequiredPermission() { return Permissions.EDIT_SETTINGS; }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_TRADER_TAXES.get(); }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        data.setIntValue("acceptable_rate",this.node.getAcceptableTaxRate());
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        this.node.setAcceptableTaxRate(null,data.getIntValue("acceptable_rate"));
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_TAXES_RATE.get(),data.getIntValue("acceptable_rate")));
    }

}
