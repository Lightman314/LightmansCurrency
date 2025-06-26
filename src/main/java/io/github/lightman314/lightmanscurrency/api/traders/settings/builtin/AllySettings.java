package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderSettingsNode;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AllySettings extends EasyTraderSettingsNode<TraderData> {

    public AllySettings(TraderData trader) { super("allies", trader, 505); }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_TRADER_ALLIES.get(); }

    @Override
    protected String getRequiredPermission() { return Permissions.ADD_REMOVE_ALLIES; }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        CompoundTag tag = new CompoundTag();
        PlayerReference.saveList(tag,this.trader.getAllies(),"Allies");
        data.setCompoundValue("allies",tag);
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        if(data.hasCompoundValue("allies"))
        {
            CompoundTag tag = data.getCompoundValue("allies");
            this.trader.overwriteAllies(PlayerReference.loadList(tag,"Allies"));
        }
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        if(data.hasCompoundValue("allies"))
        {
            CompoundTag tag = data.getCompoundValue("allies");
            int count = 0;
            if(tag.contains("Allies", Tag.TAG_LIST))
                count = tag.getList("Allies",Tag.TAG_COMPOUND).size();
            lineWriter.accept(formatEntry(LCText.DATA_ENTRY_ALLIES.get(), LCText.DATA_ENTRY_ALLIES_COUNT.get(count)));
        }
    }

}
