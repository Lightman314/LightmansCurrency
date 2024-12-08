package io.github.lightman314.lightmanscurrency.api.trader_interface.data;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.TradeResult;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TradeReference {

    private final TraderInterfaceTargets parent;
    private final int tradeIndex;
    public int getTradeIndex() { return this.tradeIndex; }
    private TradeData tradeData;
    public boolean hasTrade() { return this.tradeIndex >= 0 && this.tradeData != null; }
    public TradeData getLocalTrade() { return this.tradeData; }
    private TradeResult lastResult = TradeResult.SUCCESS;
    public TradeResult getLastResult() { return this.lastResult; }
    public void setLastResult(TradeResult result) { this.lastResult = result; }

    @Nullable
    public TradeData getTrueTrade() {
        if(this.tradeIndex < 0)
            return null;
        TraderData trader = this.parent.getTrader();
        if(trader != null)
            return trader.getTrade(this.tradeIndex);
        return null;
    }

    private TradeReference(TraderInterfaceTargets parent, int tradeIndex, TradeData trade) {
        this.parent = parent;
        this.tradeIndex = tradeIndex;
        this.tradeData = trade;
    }

    public static TradeReference of(TraderInterfaceTargets parent, int tradeIndex, TradeData trade) { return new TradeReference(parent,tradeIndex,trade); }
    @Nullable
    public static TradeReference of(TraderInterfaceTargets parent, int tradeIndex) {
        if(tradeIndex < 0)
            return null;
        TraderData trader = parent.getTrader();
        if(trader != null)
        {
            TradeData trade = trader.getTrade(tradeIndex);
            if(trade != null)
                return of(parent,tradeIndex,parent.copyTrade(trade));
        }
        return null;
    }

    public CompoundTag save(HolderLookup.Provider lookup) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Index",this.tradeIndex);
        if(this.tradeData != null)
            tag.put("Trade",this.tradeData.getAsNBT(lookup));
        return tag;
    }

    @Nullable
    public static TradeReference load(TraderInterfaceTargets parent, CompoundTag tag, HolderLookup.Provider lookup)
    {
        if(tag.contains("Trade") && tag.contains("Index"))
        {
            int index = tag.getInt("Index");
            TradeData trade = parent.loadTrade(tag.getCompound("Trade"),lookup);
            if(trade == null)
            {
                LightmansCurrency.LogWarning("Error loading cached trade from reference!");
                return null;
            }
            return new TradeReference(parent,index,trade);
        }
        return null;
    }

    public void refreshTrade() {
        if(!this.hasTrade())
            return;
        TradeData newTrade = this.parent.copyTrade(this.getTrueTrade());
        if(newTrade != null)
            this.tradeData = newTrade;
    }

}
