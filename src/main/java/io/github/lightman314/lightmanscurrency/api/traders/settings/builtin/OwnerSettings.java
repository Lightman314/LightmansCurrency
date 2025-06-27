package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderSettingsNode;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OwnerSettings extends EasyTraderSettingsNode<TraderData> {

    public OwnerSettings(TraderData trader) {super("ownership", trader, 1000); }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_OWNERSHIP.get(); }

    @Override
    protected String getRequiredPermission() { return Permissions.TRANSFER_OWNERSHIP; }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        data.setCompoundValue("owner",this.trader.getOwner().save());
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        if(data.hasCompoundValue("owner"))
        {
            this.trader.getOwner().load(data.getCompoundValue("owner"));
            this.trader.getOwner().setChanged();
            this.trader.setLinkedToBank(false);
            context.updateOwner(this.trader.getOwner());
        }
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        if(data.hasCompoundValue("owner"))
        {
            OwnerData owner = new OwnerData(this.host);
            owner.load(data.getCompoundValue("owner"));
            lineWriter.accept(formatEntry(LCText.DATA_ENTRY_OWNER.get(),owner.getName()));
        }
    }

}