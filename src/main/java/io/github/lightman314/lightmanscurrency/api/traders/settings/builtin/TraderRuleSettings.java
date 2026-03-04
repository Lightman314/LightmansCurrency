package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderNodeSettings;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.rules.RuleSubNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.TraderRulesNode;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public class TraderRuleSettings extends EasyTraderNodeSettings<TraderData,TraderRulesNode> {

    //Powered by the sub-node, but acts like a full node
    private final RuleSubNode subNode;
    public TraderRuleSettings(TraderData trader,TraderRulesNode node) {
        super("trade_rules", trader, node);
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
