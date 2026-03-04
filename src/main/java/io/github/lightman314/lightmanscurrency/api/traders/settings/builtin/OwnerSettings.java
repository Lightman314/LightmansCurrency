package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.OwnerNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IOwnerListener;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderNodeSettings;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public class OwnerSettings extends EasyTraderNodeSettings<TraderData,OwnerNode> {

    public OwnerSettings(TraderData trader,OwnerNode node) {super("ownership", trader,node, 1000); }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_OWNERSHIP.get(); }

    @Override
    protected String getRequiredPermission() { return Permissions.TRANSFER_OWNERSHIP; }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        data.setCustom("owner",this.node.getOwner(),OwnerData.CODEC);
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        if(data.hasCompoundValue("owner"))
        {
            this.node.getOwner().copyFrom(data.getCustomValue("owner",OwnerData.CODEC));
            //Flag the owner as changed
            this.node.setOwnerChanged();
            //Inform relevant parties that the owner has changed
            for(TraderNode node : this.trader.getNodeIterable())
            {
                if(node instanceof IOwnerListener listener)
                    listener.onOwnerChanged();
            }
            context.updateOwner(this.node.getOwner());
        }
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        if(data.hasCompoundValue("owner"))
        {
            OwnerData owner = data.getCustomValue("owner",OwnerData.CODEC);
            if(owner == null)
                return;
            owner.withParent(this.host);
            lineWriter.accept(formatEntry(LCText.DATA_ENTRY_OWNER.get(),owner.getName()));
        }
    }

}
