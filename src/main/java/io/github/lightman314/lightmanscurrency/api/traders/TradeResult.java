package io.github.lightman314.lightmanscurrency.api.traders;

import net.minecraft.network.chat.Component;

public enum TradeResult {
    /**
     * Remote trade was successfully executed
     */
    SUCCESS(null),
    /**
     * Trade failed as the trader is out of stock
     */
    FAIL_OUT_OF_STOCK("lightmanscurrency.remotetrade.fail.nostock"),

    /**
     * Trade failed as the player could not afford the trade
     */
    FAIL_CANNOT_AFFORD("lightmanscurrency.remotetrade.fail.cantafford"),
    /**
     * Trade failed as there's no room for the output items
     */
    FAIL_NO_OUTPUT_SPACE("lightmanscurrency.remotetrade.fail.nospace.output"),
    /**
     * Trade failed as there's no room for the input items
     */
    FAIL_NO_INPUT_SPACE("lightmanscurrency.remotetrade.fail.nospace.input"),
    /**
     * Trade failed as the trade rules denied the trade
     */
    FAIL_TRADE_RULE_DENIAL("lightmanscurrency.remotetrade.fail.traderule"),
    /**
     * Trade failed as the trader is locked due to the total tax rate exceeding the traders accepted limits
     */
    FAIL_TAX_EXCEEDED_LIMIT("lightmanscurrency.remotetrade.fail.tax_limit"),
    /**
     * Trade failed as the trade is no longer valid
     */
    FAIL_INVALID_TRADE("lightmanscurrency.remotetrade.fail.invalid"),
    /**
     * Trade failed as this trader does not support remote trades
     */
    FAIL_NOT_SUPPORTED("lightmanscurrency.remotetrade.fail.notsupported"),
    /**
     * Trade failed as the trader was null
     */
    FAIL_NULL("lightmanscurrency.remotetrade.fail.null");
    public boolean isSuccess() { return this.failMessage == null; }
    public boolean hasMessage() { return this.failMessage != null; }
    public final Component failMessage;
    TradeResult(String message) { this.failMessage = message == null ? null : Component.translatable(message); }
}
