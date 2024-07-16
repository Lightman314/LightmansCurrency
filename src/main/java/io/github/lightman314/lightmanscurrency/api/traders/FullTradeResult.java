package io.github.lightman314.lightmanscurrency.api.traders;

import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FullTradeResult {

    public final TradeResult simpleResult;
    public boolean isSuccess() { return this.simpleResult.isSuccess() && this.data != null; }
    @Nullable
    public final TradeEvent.PostTradeEvent data;

    private FullTradeResult(@Nonnull TradeResult result, @Nullable TradeEvent.PostTradeEvent event) {
        this.simpleResult = result;
        this.data = event;
    }

    public static FullTradeResult failure(@Nonnull TradeResult result) { return new FullTradeResult(result,null); }
    public static FullTradeResult success(@Nonnull TradeEvent.PostTradeEvent event) { return new FullTradeResult(TradeResult.SUCCESS,event); }

}