package io.github.lightman314.lightmanscurrency.common.traders.gacha.tradedata.client;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.display.ItemEntry;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.tradedata.GachaTradeData;
import io.github.lightman314.lightmanscurrency.util.ListUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GachaTradeButtonRenderer extends TradeRenderManager<GachaTradeData> {

    public GachaTradeButtonRenderer(GachaTradeData trade) { super(trade); }

    @Override
    public int tradeButtonWidth(TradeContext context) { return 94; }

    @Override
    public Optional<ScreenPosition> arrowPosition(TradeContext context) { return ScreenPosition.ofOptional(36, 1); }

    @Override
    public DisplayData inputDisplayArea(TradeContext context) { return new DisplayData(1, 1, 34, 16); }

    @Override
    public List<DisplayEntry> getInputDisplays(TradeContext context) { return this.lazyPriceDisplayList(context); }

    @Override
    public DisplayData outputDisplayArea(TradeContext context) { return new DisplayData(59, 1, 34, 16); }

    @Override
    public List<DisplayEntry> getOutputDisplays(TradeContext context) {
        List<ItemStack> items = this.trade.trader.getStorage().getContents();
        if(items.isEmpty())
            return ImmutableList.of();
        ItemStack display = ListUtil.randomItemFromList(items,ItemStack.EMPTY);
        return ImmutableList.of(ItemEntry.of(display,this.getItemTooltips(items)));
    }

    private List<Component> getItemTooltips(List<ItemStack> items) {
        List<Component> list = new ArrayList<>();
        list.add(LCText.TOOLTIP_TRADER_GACHA_CONTENTS_LABEL.get());
        for(ItemStack item : items)
            list.add(LCText.TOOLTIP_TRADER_GACHA_CONTENTS.get(item.getCount(),item.getHoverName()));
        return list;
    }

    @Override
    protected void getAdditionalAlertData(TradeContext context, List<AlertData> alerts) {
        if(this.trade.trader.getStorage().isEmpty())
            alerts.add(AlertData.warn(LCText.TOOLTIP_OUT_OF_STOCK));
        if(!context.hasFunds(this.trade.getCost(context)))
            alerts.add(AlertData.warn(LCText.TOOLTIP_CANNOT_AFFORD));
    }

}
