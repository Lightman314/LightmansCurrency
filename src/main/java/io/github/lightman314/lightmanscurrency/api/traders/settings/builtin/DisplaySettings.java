package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderSettingsNode;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DisplaySettings extends EasyTraderSettingsNode<TraderData> {

    public DisplaySettings(TraderData trader) { super("display", trader); }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_TRADER_DISPLAY.get(); }

    @Override
    protected String getRequiredPermission() { return Permissions.CHANGE_NAME; }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        data.setStringValue("custom_name",this.trader.getCustomName());
        data.setCompoundValue("custom_icon",this.trader.getCustomIcon().save());
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        if(data.hasStringValue("custom_name"))
            this.trader.setCustomName(data.getStringValue("custom_name"));
        if(data.hasCompoundValue("custom_icon"))
            this.trader.setCustomIcon(IconData.load(data.getCompoundValue("custom_icon")));
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_NAME.get(),data.getStringValue("custom_name")));
        if(data.hasCompoundValue("custom_icon"))
        {
            IconData icon = IconData.load(data.getCompoundValue("custom_icon"));
            if(icon != null && !icon.isNull())
                lineWriter.accept(LCText.DATA_ENTRY_TRADER_ICON.get());
        }
    }

}