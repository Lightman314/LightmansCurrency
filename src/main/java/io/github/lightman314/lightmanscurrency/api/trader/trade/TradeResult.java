package io.github.lightman314.lightmanscurrency.api.trader.trade;

import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class TradeResult {

    private static final String SUCCESS = "success";

    public static final TradeResult FAIL_OUT_OF_STOCK = failure("fail_out_of_stock");

    public static final TradeResult FAIL_CANNOT_AFFORD = failure("fail_cannot_afford");

    public static final TradeResult FAIL_NO_OUTPUT_SPACE = failure("fail_no_output_space");

    public static final TradeResult FAIL_NO_INPUT_SPACE = failure("no_input_space");

    public static final TradeResult FAIL_EVENT_DENIAL = failure("fail_trade_rule_denial");

    public static final TradeResult FAIL_TAX_EXCEEDED_LIMIT = failure("fail_tax_exceeded_limit");

    public static final TradeResult FAIL_INVALID_TRADE = failure("fail_invalid_trade");

    public static final TradeResult FAIL_NOT_SUPPORTED = failure("fail_not_supported");

    public static final TradeResult FAIL_NULL = failure("fail_null");


    private final String key;
    public String getKey() { return this.key; }
    public String getTranslationKey() { return "gui.lightmanscurrency.trade_result." + this.key; }
    private final Optional<TradeEvent.Post> event;
    //Success Accessors
    public boolean isSuccess() { return this.event.isPresent(); }
    public Optional<TradeEvent.Post> getResults() { return this.event; }
    //Failure Accessors
    public boolean isFailure() { return !this.isSuccess(); }
    public Component getMessage() { return Component.translatable(this.getTranslationKey()); }

    //Success Constructor
    private TradeResult(TradeEvent.Post event) { this(SUCCESS,Optional.of(event)); }
    //Failure Constructor
    private TradeResult(String key) { this(key,Optional.empty()); }
    //Actual Constructor
    private TradeResult(String key, Optional<TradeEvent.Post> event)
    {
        this.key = key;
        this.event = event;
    }

    /**
     * Should only be created by {@link TradingNode#finishSuccessfulTrade(TradeContext,TradeData,MoneyValue,MoneyValue,List) TradingNode#finishSuccessfulTrade(TradeContext,TradeData,MoneyValue,MoneyValue,List)}
     */
    @ApiStatus.Internal
    public static TradeResult success(TradeEvent.Post event) { return new TradeResult(Objects.requireNonNull(event)); }

    public static TradeResult failure(String key) { return new TradeResult(key); }

}