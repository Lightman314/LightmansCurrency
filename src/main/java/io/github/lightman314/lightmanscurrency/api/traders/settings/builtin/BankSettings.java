package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
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
public class BankSettings extends EasyTraderSettingsNode<TraderData> {

    public BankSettings(TraderData trader) { super("bank", trader); }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_TRADER_BANK.get(); }

    @Override
    protected String getRequiredPermission() { return Permissions.BANK_LINK; }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        data.setBooleanValue("linked_to_bank",this.trader.isLinkedToBank());
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        this.trader.setLinkedToBank(data.getBooleanValue("linked_to_bank"));
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_BANK_LINK.get(), data.getBooleanValue("linked_to_bank")));
    }

}
