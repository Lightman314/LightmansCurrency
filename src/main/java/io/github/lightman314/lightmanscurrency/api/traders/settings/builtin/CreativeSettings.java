package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.TraderSettingsNode;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CreativeSettings extends TraderSettingsNode<TraderData> {

    public CreativeSettings(TraderData trader) { super("creative",trader); }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_CREATIVE.get(); }

    @Override
    public boolean allowSelecting(@Nullable Player player) { return LCAdminMode.isAdminPlayer(player); }

    @Override
    public boolean allowSaving(@Nullable Player player) { return LCAdminMode.isAdminPlayer(player); }

    @Override
    public boolean allowLoading(LoadContext context) { return LCAdminMode.isAdminPlayer(context.player); }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        data.setBooleanValue("creative",this.trader.isCreative());
        data.setBooleanValue("storeMoneyInCreative",this.trader.shouldStoreCreativeMoney());
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        this.trader.setCreative(data.getBooleanValue("creative"));
        this.trader.setStoreCreativeMoney(data.getBooleanValue("storeMoneyInCreative"));
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_CREATIVE.get(),data.getBooleanValue("creative")));
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_STORE_CREATIVE_MONEY.get(),data.getBooleanValue("storeMoneyInCreative")));
    }

}