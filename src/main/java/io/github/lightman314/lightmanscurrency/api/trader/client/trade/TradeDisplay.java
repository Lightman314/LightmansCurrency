package io.github.lightman314.lightmanscurrency.api.trader.client.trade;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.client.ClientPairedRegistry;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.TradeDisplayEntry;
import io.github.lightman314.lightmanscurrency.api.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDataType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlotType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;

public abstract class TradeDisplay {

    public static final ClientPairedRegistry<TradeDataType<?>,TradeDisplay> REGISTRY = new ClientPairedRegistry<>(LCRegistries.Trader.TRADE_DATA_TYPE);

    public int getButtonWidth(TradeData trade,TradeContext context) {
        //Calculate based on input/output width
        return this.getInputWidth(trade,context) + this.getOutputWidth(trade,context) + 100;
    }

    public abstract int getInputWidth(TradeData trade,TradeContext context);
    public abstract int getOutputWidth(TradeData trade,TradeContext context);

    public abstract List<TradeDisplayEntry> getDisplayEntries(TradeData trade, TradeSlotType section, TradeContext context, ScreenArea buttonSize);

    protected final List<TradeDisplayEntry> single(TradeDisplayEntry entry) { return Lists.newArrayList(entry); }

    public static List<TradeDisplayEntry> forPrice(TradePrice price) {
        //TODO get the display entry for the given price
        throw new NotImplementedException();
    }


}