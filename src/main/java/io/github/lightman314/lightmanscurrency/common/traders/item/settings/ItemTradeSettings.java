package io.github.lightman314.lightmanscurrency.common.traders.item.settings;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsSubNode;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.trades.TradeSettings;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.trades.TradeSubNode;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemTradeSettings extends TradeSettings<ItemTraderData> {

    public ItemTradeSettings(ItemTraderData trader) { super("item_trades",trader); }

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
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_TRADE_COUNT.get(),data.getIntValue("trade_count")));
    }

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
                String prefix = "item_" + i;
                node.setCompoundValue(prefix, InventoryUtil.saveItemNoLimits(trade.getActualItem(i),this.registryAccess()));
                node.setBooleanValue(prefix + "_nbt",trade.getEnforceNBT(i));
                if(i < 2)
                    node.setStringValue(prefix + "_name",trade.getCustomName(i));
            }
            //Save custom trade settings
            trade.saveAdditionalSetings(node);
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
                    trade.setItem(InventoryUtil.loadItemNoLimits(node.getCompoundValue(prefix),this.registryAccess()),i);
                trade.setEnforceNBT(i,node.getBooleanValue(prefix + "_nbt"));
                if(i < 2)
                    trade.setCustomName(i,node.getStringValue(prefix + "_name"));
            }
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
                ItemStack item = InventoryUtil.loadItemNoLimits(data.getCompoundValue("item_" + i),this.registryAccess());
                count += item.getCount();
            }
            lineWriter.accept(type == TradeDirection.PURCHASE ? LCText.DATA_ENTRY_TRADER_TRADE_ITEM_PURCHASE_ITEMS.get(count) : LCText.DATA_ENTRY_TRADER_TRADE_ITEM_SELL_ITEMS.get(count));
            //Barter Items
            if(type == TradeDirection.BARTER)
            {
                count = 0;
                for(int i = 2; i < 4; ++i)
                {
                    ItemStack item = InventoryUtil.loadItemNoLimits(data.getCompoundValue("item_" + i),this.registryAccess());
                    count += item.getCount();
                }
                lineWriter.accept(LCText.DATA_ENTRY_TRADER_TRADE_ITEM_BARTER_ITEMS.get(count));
            }
        }

    }

}
