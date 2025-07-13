package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderSettingsNode;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.rules.RuleSubNode;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TraderRuleSettings extends EasyTraderSettingsNode<TraderData> {

    //Powered by the sub-node, but acts like a full node
    private final RuleSubNode subNode;
    public TraderRuleSettings(TraderData trader) {
        super("trade_rules", trader);
        this.subNode = new RuleSubNode(this,trader,this::allowLoading,EasyText.empty());
    }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_RULES_TRADER.get(); }

    @Override
    protected String getRequiredPermission() { return Permissions.EDIT_TRADE_RULES; }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        this.subNode.saveSettings(data);
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        this.subNode.loadSettings(data,context);
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        this.subNode.writeLines(data,lineWriter);
    }

}