package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderSettingsNode;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public class TaxSettings extends EasyTraderSettingsNode<TraderData> {

    public TaxSettings(TraderData trader) { super("taxes", trader); }

    @Override
    protected String getRequiredPermission() { return Permissions.EDIT_SETTINGS; }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_TRADER_TAXES.get(); }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        data.setIntValue("acceptable_rate",this.trader.getAcceptableTaxRate());
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        this.trader.setAcceptableTaxRate(data.getIntValue("acceptable_rate"));
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_TAXES_RATE.get(),data.getIntValue("acceptable_rate")));
    }

}