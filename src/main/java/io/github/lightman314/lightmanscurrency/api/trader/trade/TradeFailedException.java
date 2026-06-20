package io.github.lightman314.lightmanscurrency.api.trader.trade;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;

public class TradeFailedException extends RuntimeException {

    public final TradeResult result;
    private TradeFailedException(String message,TradeResult result) { super(message); this.result = result; }
    public TradeFailedException(TradeResult result) { this(buildMessage(result),result); }
    public TradeFailedException(TraderNodeType<?> type) { this("Missing Trade Node of type " + type + "!",TradeResult.FAIL_NULL); }

    private static String buildMessage(TradeResult result) { return result.getMessage().getString(); }

}