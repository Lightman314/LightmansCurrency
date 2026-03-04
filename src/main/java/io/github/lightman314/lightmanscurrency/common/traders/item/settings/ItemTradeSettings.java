package io.github.lightman314.lightmanscurrency.common.traders.item.settings;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsSubNode;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.trades.TradeSettings;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.trades.TradeSubNode;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ItemTradeSettings extends TradeSettings<ItemTradeData, ItemTradeNode> {

    public ItemTradeSettings(TraderData trader,ItemTradeNode node) { super("item_trades",trader,node); }

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
            if(this.node.getTradeCount() < newCount)
                this.node.forceTradeCount(newCount);
        }
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_TRADE_COUNT.get(),data.getIntValue("trade_count")));
    }

    private static class TradeNode extends TradeSubNode<ItemTradeData,ItemTradeSettings>
    {

        public TradeNode(ItemTradeSettings parent, int index) { super(parent, index); }

        @Nullable
        @Override
        protected ItemTradeData getTrade() { return this.parent.node.getTrade(this.index); }

        @Override
        protected void saveTrade(SavedSettingData.MutableNodeAccess node, ItemTradeData trade) {
            node.setStringValue("type",trade.getTradeDirection().toString());
            if(!trade.isBarter())
                node.setCustom("price",trade.getCost(),MoneyValue.CODEC);
            for(int i = 0; i < (trade.isBarter() ? 4 : 2); ++i)
            {
                String prefix = "item_" + i;
                node.setCustom(prefix,trade.getActualItem(i),ItemStack.CODEC);
                node.setBooleanValue(prefix + "_nbt",trade.getEnforceNBT(i));
                if(i < 2)
                    node.setStringValue(prefix + "_name",trade.getCustomName(i));
            }
            //Save custom trade settings
            trade.saveAdditionalSettings(node);
        }

        @Override
        protected void loadTrade(SavedSettingData.NodeAccess node, ItemTradeData trade, LoadContext context) {
            trade.setTradeType(EnumUtil.enumFromString(node.getStringValue("type"),TradeDirection.values(),TradeDirection.SALE));
            if(!trade.isBarter())
                trade.setCost(MoneyValue.load(node.getCompoundValue("price")));
            for(int i = 0; i < (trade.isBarter() ? 4 : 2); ++i)
            {
                String prefix = "item_" + i;
                if(node.hasCompoundValue(prefix))
                    trade.setItem(node.getCustomValue(prefix,ItemStack.CODEC,ItemStack.EMPTY),i);
                trade.setEnforceNBT(i,node.getBooleanValue(prefix + "_nbt"));
                if(i < 2)
                    trade.setCustomName(i,node.getStringValue(prefix + "_name"));
            }
            //Load custom trade settings
            trade.loadAdditionalSettings(node);
        }

        @Override
        protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
            TradeDirection type = EnumUtil.enumFromString(data.getStringValue("type"),TradeDirection.values(),TradeDirection.SALE);
            //Trade Type
            lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_TRADE_TYPE.get(),LCText.GUI_TRADE_DIRECTION.get(type).get()));
            //Price
            if(type != TradeDirection.BARTER)
                lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_TRADE_PRICE.get(),MoneyValue.load(data.getCompoundValue("price")).getText()));
            //Items
            int count = 0;
            for(int i = 0; i < 2; ++i)
            {
                ItemStack item = data.getCustomValue("item_" + i,ItemStack.CODEC,ItemStack.EMPTY);
                count += item.getCount();
            }
            lineWriter.accept(type == TradeDirection.PURCHASE ? LCText.DATA_ENTRY_TRADER_TRADE_ITEM_PURCHASE_ITEMS.get(count) : LCText.DATA_ENTRY_TRADER_TRADE_ITEM_SELL_ITEMS.get(count));
            //Barter Items
            if(type == TradeDirection.BARTER)
            {
                count = 0;
                for(int i = 2; i < 4; ++i)
                {
                    ItemStack item = data.getCustomValue("item_" + i,ItemStack.CODEC,ItemStack.EMPTY);
                    count += item.getCount();
                }
                lineWriter.accept(LCText.DATA_ENTRY_TRADER_TRADE_ITEM_BARTER_ITEMS.get(count));
            }
        }

    }

}
