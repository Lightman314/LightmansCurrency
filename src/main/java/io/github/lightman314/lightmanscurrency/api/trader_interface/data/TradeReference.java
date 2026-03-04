package io.github.lightman314.lightmanscurrency.api.trader_interface.data;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.Optional;

public class TradeReference<T extends TradeData> {

    private final TraderInterfaceTargets<T> parent;
    private final int tradeIndex;
    public int getTradeIndex() { return this.tradeIndex; }
    private T tradeData;
    public boolean hasTrade() { return this.tradeIndex >= 0 && this.tradeData != null; }
    public T getLocalTrade() { return this.tradeData; }
    private Optional<TradeResult> lastResult = Optional.empty();
    public Optional<TradeResult> getLastResult() { return this.lastResult; }
    public void setLastResult(TradeResult result) { this.lastResult = Optional.of(result); }

    @Nullable
    public T getTrueTrade() {
        if(this.tradeIndex < 0)
            return null;
        TraderData trader = this.parent.getTrader();
        if(trader != null)
        {
            try { return (T)trader.getTrade(this.tradeIndex);
            } catch (ClassCastException ignored) {}
        }
        return null;
    }

    private TradeReference(TraderInterfaceTargets<T> parent, int tradeIndex, T trade) {
        this.parent = parent;
        this.tradeIndex = tradeIndex;
        this.tradeData = trade;
    }

    public static <T extends TradeData> TradeReference<T> of(TraderInterfaceTargets<T> parent, int tradeIndex, T trade) { return new TradeReference<>(parent,tradeIndex,trade); }
    @Nullable
    public static <T extends TradeData> TradeReference<T> of(TraderInterfaceTargets<T> parent, int tradeIndex) {
        if(tradeIndex < 0)
            return null;
        TraderData trader = parent.getTrader();
        if(trader != null)
        {
            TradeData trade = trader.getTrade(tradeIndex);
            if(trade != null)
            {
                try {return of(parent,tradeIndex,parent.copyTrade((T)trade));
                } catch (ClassCastException ignored) {}
            }
        }
        return null;
    }

    public CompoundTag save(DataContext<Tag> context) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Index",this.tradeIndex);
        if(this.tradeData != null)
            tag.put("Trade",this.parent.tradeCodec().encodeStart(context.ops(),this.tradeData).getOrThrow());
        return tag;
    }

    @Nullable
    public static <T extends TradeData> TradeReference<T> load(TraderInterfaceTargets<T> parent, CompoundTag tag, DataContext<Tag> context)
    {
        if(tag.contains("Trade") && tag.contains("Index"))
        {
            int index = tag.getInt("Index");
            T trade = context.read(tag.get("Trade"),parent.tradeCodec());
            if(trade == null)
            {
                LightmansCurrency.LogWarning("Error loading cached trade from reference!");
                return null;
            }
            try { return new TradeReference<>(parent,index,trade);
            } catch (ClassCastException ignored) {}

        }
        return null;
    }

    public void refreshTrade() {
        if(!this.hasTrade())
            return;
        T newTrade = this.parent.copyTrade(this.getTrueTrade());
        if(newTrade != null)
            this.tradeData = newTrade;
    }

}
