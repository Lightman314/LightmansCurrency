package io.github.lightman314.lightmanscurrency.api.trader.trade.settings;

import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.ISettingsStorageIO;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsDisplayOutput;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsLoadContext;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TradeSettingsWrapper implements ISettingsStorageIO {
    private final SettingsIOTrade trade;
    private final DualKey key;
    private final int displayIndex;
    public TradeSettingsWrapper(SettingsIOTrade trade,DualKey parentKey,int displayIndex) {
        this.trade = trade;
        this.key = parentKey.addToKey("trade_" + displayIndex);
        this.displayIndex = displayIndex;
    }

    @Override
    public DualKey getSettingsKey() { return this.key; }

    @Override
    public Component getSettingsName() { return TradingNode.VALUE_TRADE.get(this.displayIndex); }

    @Override
    public void encodeSettings(ValueOutput output) {
        this.trade.encodeSettings(output);
    }

    @Override
    public void decodeSettings(ValueInput data,SettingsLoadContext context) {
        if(context.getPermission(LCPermissions.EDIT_TRADES))
            this.trade.decodeSettings(data);
    }

    @Override
    public void appendDisplay(ValueInput data, SettingsDisplayOutput output) { }

}
