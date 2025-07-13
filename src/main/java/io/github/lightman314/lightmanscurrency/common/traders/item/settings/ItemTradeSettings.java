package io.github.lightman314.lightmanscurrency.common.traders.item.settings;

import io.github.lightman314.lightmanscurrency.api.settings.SettingsSubNode;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.trades.TradeSettings;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.trades.TradeSubNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemTradeSettings extends TradeSettings<ItemTraderData> {

    public ItemTradeSettings(String key, ItemTraderData trader) { super(key, trader); }

    @Nullable
    @Override
    protected ItemTradeData getRuleHost(int tradeIndex) {
        if(tradeIndex < 0 || tradeIndex >= this.trader.getTradeCount())
            return null;
        return this.trader.getTrade(tradeIndex);
    }

    @Override
    protected SettingsSubNode<?> createTradeNode(int tradeIndex) {
        return new TradeNode(this,tradeIndex);
    }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        data.setIntValue("trade_count",this.trader.getTradeCount());
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        //Allow admins to forcibly override the trade count if not enough trades are present
        if(context.isServerAdmin() && data.hasIntValue("trade_count"))
        {
            int newCount = data.getIntValue("trade_count");
            if(this.trader.getTradeCount() < newCount)
                this.trader.overrideTradeCount(newCount);
        }
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) { }

    private static class TradeNode extends TradeSubNode<ItemTradeData,ItemTradeSettings>
    {

        public TradeNode(ItemTradeSettings parent, int index) { super(parent, index); }

        @Nullable
        @Override
        protected ItemTradeData getTrade() { return this.parent.getRuleHost(this.index); }

        @Override
        protected void saveTrade(SavedSettingData.MutableNodeAccess node, ItemTradeData trade) {
            node.setStringValue("type",trade.getTradeDirection().toString());
            if(!trade.isBarter())
                node.setCompoundValue("price",trade.getCost().save());
            for(int i = 0; i < (trade.isBarter() ? 4 : 2); ++i)
            {
                node.setCompoundValue("item_" + i, InventoryUtil.saveItemNoLimits(trade.getItem(0),this.registryAccess()));
                node.setBooleanValue("item_" + i + "_nbt",trade.getEnforceNBT(i));
                if(i < 2)
                    node.setStringValue("item_" + i + "_name",trade.getCustomName(0));
            }
        }

        @Override
        protected void loadTrade(SavedSettingData.NodeAccess node, ItemTradeData trade, LoadContext context) {

        }

        @Override
        protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {



        }

    }

}
