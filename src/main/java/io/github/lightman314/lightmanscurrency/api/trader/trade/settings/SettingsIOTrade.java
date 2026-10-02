package io.github.lightman314.lightmanscurrency.api.trader.trade.settings;

import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleHolder;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleSettingsWrapper;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.ISettingsStorageIO;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.function.Consumer;

public interface SettingsIOTrade {

    void encodeSettings(ValueOutput output);
    void decodeSettings(ValueInput data);

    static void getSettingsForTrade(TradeData trade,DualKey parentKey,int tradeIndex, Consumer<ISettingsStorageIO> builder) {
        int displayIndex = tradeIndex + 1;
        if(trade instanceof SettingsIOTrade iot)
            builder.accept(new TradeSettingsWrapper(iot,parentKey,displayIndex));
        if(trade instanceof TradeRuleHolder ruleTrade)
            builder.accept(new TradeRuleSettingsWrapper(ruleTrade,parentKey,"_" + displayIndex,TradingNode.VALUE_TRADE_RULES.get(displayIndex), c -> c.getPermission(LCPermissions.EDIT_TRADES) && c.getPermission(LCPermissions.EDIT_TRADE_RULES)));
    }

}