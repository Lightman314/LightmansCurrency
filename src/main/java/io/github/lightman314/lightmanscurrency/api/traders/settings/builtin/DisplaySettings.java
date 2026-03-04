package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderNodeSettings;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.DisplayNode;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public class DisplaySettings extends EasyTraderNodeSettings<TraderData, DisplayNode> {

    public DisplaySettings(TraderData trader,DisplayNode node) { super("display", trader, node); }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_TRADER_DISPLAY.get(); }

    @Override
    protected String getRequiredPermission() { return Permissions.CHANGE_NAME; }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        Component text = this.node.getInternalCustomNameText();
        if(text != null)
            data.setStringValue("custom_name_text",Component.Serializer.toJson(text,this.registryAccess()));
        else
            data.setStringValue("custom_name",this.node.getInternalCustomName());
        data.setCustom("custom_icon",this.node.getCustomIcon(),IconData.CODEC);
        data.setBooleanValue("alwaysShowSearchBox",this.node.alwaysShowSearchBox());
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        if(data.hasStringValue("custom_name_text"))
            this.node.setCustomName(null,Component.Serializer.fromJson(data.getStringValue("custom_name_text"),this.registryAccess()));
        else if(data.hasStringValue("custom_name"))
            this.node.setCustomName(null,data.getStringValue("custom_name"));
        if(data.hasCompoundValue("custom_icon"))
            this.node.setCustomIcon(null,data.getCustomValue("custom_icon",IconData.CODEC,IconData.Null()));
        if(data.hasBoolValue("alwaysShowSearchBox"))
            this.node.setAlwaysShowSearchBox(null,data.getBooleanValue("alwaysShowSearchBox"));
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_NAME.get(),data.getStringValue("custom_name")));
        if(data.hasCompoundValue("custom_icon"))
        {
            IconData icon = IconData.loadOldData(data.getCompoundValue("custom_icon"),this.registryAccess());
            if(icon != null && !icon.isNull())
                lineWriter.accept(LCText.DATA_ENTRY_TRADER_ICON.get());
        }
        if(data.hasBoolValue("alwaysShowSearchBox"))
            lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_ALWAYS_SHOW_SEARCH_BOX.get(),data.getBooleanValue("alwaysShowSearchBox")));
    }

}
