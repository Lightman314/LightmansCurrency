package io.github.lightman314.lightmanscurrency.api.traders;

import io.github.lightman314.lightmanscurrency.LCText;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;

public enum TradeResult {
    /**
     * Remote trade was successfully executed
     */
    SUCCESS,
    /**
     * Trade failed as the trader is out of stock
     */
    FAIL_OUT_OF_STOCK,

    /**
     * Trade failed as the player could not afford the trade
     */
    FAIL_CANNOT_AFFORD,
    /**
     * Trade failed as there's no room for the output items
     */
    FAIL_NO_OUTPUT_SPACE,
    /**
     * Trade failed as there's no room for the input items
     */
    FAIL_NO_INPUT_SPACE,
    /**
     * Trade failed as the trade rules denied the trade
     */
    FAIL_TRADE_RULE_DENIAL,
    /**
     * Trade failed as the trader is locked due to the total tax rate exceeding the traders accepted limits
     */
    FAIL_TAX_EXCEEDED_LIMIT,
    /**
     * Trade failed as the trade is no longer valid
     */
    FAIL_INVALID_TRADE,
    /**
     * Trade failed as this trader does not support remote trades
     */
    FAIL_NOT_SUPPORTED,
    /**
     * Trade failed as the trader was null
     */
    FAIL_NULL;
    public static final TradeResult[] ALL_WITH_MESSAGES = new TradeResult[]{FAIL_OUT_OF_STOCK,FAIL_CANNOT_AFFORD,FAIL_NO_OUTPUT_SPACE,FAIL_NO_INPUT_SPACE,FAIL_TRADE_RULE_DENIAL,FAIL_TAX_EXCEEDED_LIMIT,FAIL_INVALID_TRADE,FAIL_NOT_SUPPORTED,FAIL_NULL};
    public boolean isSuccess() { return this == SUCCESS; }
    public boolean hasMessage() { return this.getMessage() != null; }
    @Nullable
    public final MutableComponent getMessage() { return LCText.GUI_TRADE_RESULT.get(this).get(); }
}
