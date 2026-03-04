package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.AlliesNode;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderNodeSettings;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public class AllySettings extends EasyTraderNodeSettings<TraderData,AlliesNode> {

    public AllySettings(TraderData trader, AlliesNode node) { super("allies", trader, node,505); }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_TRADER_ALLIES.get(); }

    @Override
    protected String getRequiredPermission() { return Permissions.ADD_REMOVE_ALLIES; }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        data.setCustomList("allies",this.node.getAllies(),PlayerReference.CODEC);
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        this.node.setAllies(data.getCustomList("allies",PlayerReference.CODEC,"Allies"));
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        int count = data.getCustomListSize("allies","Allies");
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_ALLIES.get(), LCText.DATA_ENTRY_ALLIES_COUNT.get(count)));
    }

}
