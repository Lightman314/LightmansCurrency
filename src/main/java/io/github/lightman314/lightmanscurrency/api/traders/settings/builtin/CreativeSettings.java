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
import java.util.ArrayList;
import java.util.List;
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
    public boolean allowLoading(LoadContext context) { return context.isServerAdmin(); }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        data.setBooleanValue("creative",this.trader.isCreative());
        data.setBooleanValue("storeMoneyInCreative",this.trader.shouldStoreCreativeMoney());
        data.setBooleanValue("ignore_all_taxes",this.trader.ShouldIgnoreAllTaxes());
        int index = 0;
        for(Long collector : this.trader.getIgnoredTaxCollectors())
            data.setLongValue("ignore_tc_" + index++,collector);
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        this.trader.setCreative(data.getBooleanValue("creative"));
        this.trader.setStoreCreativeMoney(data.getBooleanValue("storeMoneyInCreative"));
        this.trader.setIgnoreAllTaxes(data.getBooleanValue("ignore_all_taxes"));
        List<Long> ids = new ArrayList<>();
        for(int i = 0; data.hasLongValue("ignore_tc_" + i); ++i)
            ids.add(data.getLongValue("igonre_tc_" + i));
        this.trader.setIgnoredTaxCollectors(ids);
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_CREATIVE.get(),data.getBooleanValue("creative")));
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_STORE_CREATIVE_MONEY.get(),data.getBooleanValue("storeMoneyInCreative")));
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_TAXES_IGNORE_ALL.get(),data.getBooleanValue("ignore_all_taxes")));
        int count = 0;
        while(data.hasLongValue("ignore_tc_" + count))
            count++;
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_TAXES_IGNORED_COUNT.get(),count));
    }

}