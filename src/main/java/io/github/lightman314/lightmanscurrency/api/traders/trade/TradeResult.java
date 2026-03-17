package io.github.lightman314.lightmanscurrency.api.traders.trade;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.List;

public class TradeResult {

    /**
     * Trade failed as the trader is out of stock
     */
    public static final TradeResult FAIL_OUT_OF_STOCK = failure("fail_out_of_stock");
    /**
     * Trade failed as the player could not afford the trade
     */
    public static final TradeResult FAIL_CANNOT_AFFORD = failure("fail_cannot_afford");
    /**
     * Trade failed as there's no room for the output items
     */
    public static final TradeResult FAIL_NO_OUTPUT_SPACE = failure("fail_no_output_space");
    /**
     * Trade failed as there's no room for the input items
     */
    public static final TradeResult FAIL_NO_INPUT_SPACE = failure("fail_no_input_space");
    /**
     * Trade failed as the trade rules denied the trade
     */
    public static final TradeResult FAIL_TRADE_RULE_DENIAL = failure("fail_trade_rule_denial");
    /**
     * Trade failed as the trader is locked due to the total tax rate exceeding the traders accepted limits
     */
    public static final TradeResult FAIL_TAX_EXCEEDED_LIMIT = failure("fail_tax_exceeded_limit");
    /**
     * Trade failed as the trade is no longer valid
     */
    public static final TradeResult FAIL_INVALID_TRADE = failure("fail_invalid_trade");
    /**
     * Trade failed as this trader does not support remote trades
     */
    public static final TradeResult FAIL_NOT_SUPPORTED = failure("fail_not_supported");
    /**
     * Trade failed as the trader was null
     */
    public static final TradeResult FAIL_NULL = failure("fail_null");

    public static final List<TradeResult> ALL_WITH_MESSAGES = ImmutableList.of(FAIL_OUT_OF_STOCK,FAIL_CANNOT_AFFORD,FAIL_NO_OUTPUT_SPACE,FAIL_NO_INPUT_SPACE,FAIL_TRADE_RULE_DENIAL,FAIL_TAX_EXCEEDED_LIMIT,FAIL_INVALID_TRADE,FAIL_NOT_SUPPORTED,FAIL_NULL);

    protected static final String SUCCESS_KEY = "success";

    private final String key;
    public String getKey() { return this.key; }
    public String getTranslationKey() { return "gui.lightmanscurrency.trade_result." + this.key; }
    @Nullable
    public final TradeEvent.PostTradeEvent data;
    public boolean isSuccess() { return this.key.equals(SUCCESS_KEY); }
    public boolean hasMessage() { return !this.isSuccess(); }
    @Nullable
    public final Component getMessage() { return Component.translatable(this.getTranslationKey()); }

    //Success Constructor
    private TradeResult(@Nullable TradeEvent.PostTradeEvent event) { this(SUCCESS_KEY,event); }
    //Failure Constructor
    private TradeResult(String key) { this(key,null); }
    //Actual Constructor
    private TradeResult(String key,@Nullable TradeEvent.PostTradeEvent event)
    {
        this.key = key;
        this.data = event;
    }

    /**
     * Creates a {@link TradeResult} for a successful trade<br>
     * Should only be called by one of the various {@link io.github.lightman314.lightmanscurrency.api.traders.data.TraderData#runPostTradeEvent(TradeData, TradeContext, MoneyValue, MoneyValue) TraderData#runPostTradeEvent(...)} methods, and returned as the final result of the {@link io.github.lightman314.lightmanscurrency.api.traders.data.TraderData#TryExecuteTrade(TradeContext, int)} method.
     * @param event The {@link TradeEvent.PostTradeEvent PostTradeEvent} that was publicly posted. It may contain additional context such as the actual rewards given depending on the trader type.
     * @return A {@link TradeResult} instance with the {@link TradeEvent.PostTradeEvent PostTradeEvent} stored as context, that will return <code>true</code> in its {@link TradeResult#isSuccess()}
     */
    @ApiStatus.Internal
    public static TradeResult success(@Nullable TradeEvent.PostTradeEvent event) { return new TradeResult(event); }

    /**
     * Should be used the generate failure trade result constants. Don't call frequently if not necessary
     */
    private static TradeResult failure(String key) { return new TradeResult(key); }

}
