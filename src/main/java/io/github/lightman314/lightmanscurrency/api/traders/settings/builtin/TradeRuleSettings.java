package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderSettingsNode;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TradeRuleSettings extends EasyTraderSettingsNode<TraderData> {

    public TradeRuleSettings(TraderData trader) { super("trade_rules", trader); }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_TRADER_RULES.get(); }

    @Override
    protected String getRequiredPermission() { return Permissions.EDIT_TRADE_RULES; }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        saveRuleSettings(data,this.trader.getRules());
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        if(loadRuleSettings(data,this.trader.getRules()))
            this.trader.markTradeRulesDirty();
    }

    public static void saveRuleSettings(SavedSettingData.MutableNodeAccess data, List<TradeRule> rule)
    {
        CompoundTag tag = new CompoundTag();
        TradeRule.saveRules(tag,rule,"Rules");
        data.setCompoundValue("trade_rules",tag);
    }

    public static boolean loadRuleSettings(SavedSettingData.NodeAccess data, List<TradeRule> rules)
    {
        if(data.hasCompoundValue("trade_rules"))
        {
            CompoundTag tag = data.getCompoundValue("trade_rules");
            List<TradeRule> duplicateRules = TradeRule.loadRules(tag,"Rules",null);
            for(TradeRule dupe : duplicateRules)
            {
                TradeRule actual = TradeRule.getRule(dupe.type.type,rules);
                if(actual != null)
                    actual.load(dupe.save());
            }
            return true;
        }
        return false;
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        if(data.hasCompoundValue("trade_rules"))
        {
            CompoundTag tag = data.getCompoundValue("trade_rules");
        }
    }

}