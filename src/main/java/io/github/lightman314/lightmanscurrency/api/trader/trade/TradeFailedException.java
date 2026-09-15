package io.github.lightman314.lightmanscurrency.api.trader.trade;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;

/**
 * An exception to be thrown during a trade interaction for the purpose of making it easier for sub-methods
 * to fail a trade interaction early without needing to pass the fail reason back to its caller as a return value.<br>
 * When thrown it will be safely caught by a catch statement within the {@link io.github.lightman314.lightmanscurrency.api.trader.data.TraderData#attemptTrade(TradeContext.Builder, int, int) TraderData#attemptTrade(TradeContext.Builder, int, int)} method
 */
public class TradeFailedException extends Exception {

    public final TradeResult result;
    public TradeFailedException(String message,TradeResult result) { super(message); this.result = result; }
    public TradeFailedException(TradeResult result) { this(buildMessage(result),result); }
    public TradeFailedException(TraderNodeType<?> type) { this("Missing Trade Node of type " + type + "!",TradeResult.FAIL_NULL); }

    private static String buildMessage(TradeResult result) { return result.getMessage().getString(); }

}