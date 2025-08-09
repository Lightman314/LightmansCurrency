package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.filters.BasicSearchFilter;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FilterUtils {

    private FilterUtils() {}

    public static void boolCheck(PendingSearch search, String filter, boolean value) {
        search.processStrictFilter(filter,bool -> "true".contains(bool.toLowerCase()) == value);
    }

    public static void intRange(PendingSearch search,String filter,int value) { search.processUnfiltered(intRange(filter,value)); }

    public static Predicate<String> intRange(String filter, int value)
    {
        return input -> {
            try {
                if(!input.startsWith(filter))
                    return false;
                input = input.substring(filter.length());
                if(input.startsWith(">="))
                    return value >= Integer.parseInt(input.substring(2));
                if(input.startsWith(">"))
                    return value > Integer.parseInt(input.substring(1));
                if(input.startsWith("<="))
                    return value <= Integer.parseInt(input.substring(2));
                if(input.startsWith("<"))
                    return value < Integer.parseInt(input.substring(1));
                if(input.startsWith("="))
                {
                    if(input.startsWith("=="))
                        return value == Integer.parseInt(input.substring(2));
                    return value == Integer.parseInt(input.substring(1));
                }
                if((input.startsWith("[") || input.startsWith("(")) && (input.endsWith("]") || input.endsWith(")")))
                {
                    boolean startInclusive = input.startsWith("[");
                    boolean endInclusive = input.endsWith("]");
                    input = input.substring(1,input.length() - 1);
                    String[] split = input.split(",",2);
                    int start = Integer.parseInt(split[0]);
                    int end = Integer.parseInt(split[1]);
                    if(end < start)
                        return false;
                    return (startInclusive && value == start) || (endInclusive && value == end) || (value < end && value > start);
                }
                //Unsupported integer range input
                return false;
            } catch (NumberFormatException e) { return false; }
        };
    }

    public static Predicate<Integer> intRange(PendingSearch search, String filter)
    {
        AtomicReference<Predicate<Integer>> holder = new AtomicReference<>();
        search.setupUnfiltered((input) -> {
            try {
                if(!input.startsWith(filter))
                    return PredicateWithResult.createNull(holder);
                input = input.substring(filter.length());
                if(input.startsWith(">="))
                {
                    final int minValue = Integer.parseInt(input.substring(2));
                    return PredicateWithResult.create(v -> v >= minValue,holder);
                }
                if(input.startsWith(">"))
                {
                    final int minValue = Integer.parseInt(input.substring(1));
                    return PredicateWithResult.create(v -> v > minValue,holder);
                }
                if(input.startsWith("<="))
                {
                    final int maxValue = Integer.parseInt(input.substring(2));
                    return PredicateWithResult.create(v -> v <= maxValue,holder);
                }
                if(input.startsWith("<"))
                {
                    final int maxValue = Integer.parseInt(input.substring(1));
                    return PredicateWithResult.create(v -> v <= maxValue,holder);
                }
                if(input.startsWith("="))
                {
                    final int value;
                    if(input.startsWith("=="))
                        value = Integer.parseInt(input.substring(2));
                    else
                        value = Integer.parseInt(input.substring(1));
                    return PredicateWithResult.create(v -> v == value,holder);
                }
                if((input.startsWith("[") || input.startsWith("(")) && (input.endsWith("]") || input.endsWith(")")))
                {
                    final boolean startInclusive = input.startsWith("[");
                    final boolean endInclusive = input.endsWith("]");
                    input = input.substring(1,input.length() - 1);
                    String[] split = input.split(",",2);
                    final int start = Integer.parseInt(split[0]);
                    final int end = Integer.parseInt(split[1]);
                    if(end < start)
                        return PredicateWithResult.createNull(holder);
                    return PredicateWithResult.create(v -> (startInclusive && v == start) || (endInclusive && v == end) || (v < end && v > start),holder);
                }
                //Unsupported integer range input
                return PredicateWithResult.createNull(holder);
            } catch (NumberFormatException e) { return PredicateWithResult.createNull(holder); }
        });
        return holder.get();
    }

    public static void longRange(PendingSearch search, String filter,long value) { search.processUnfiltered(longRange(filter,value)); }

    public static Predicate<String> longRange(String filter, long value)
    {
        return input -> {
            try {
                if(!input.startsWith(filter))
                    return false;
                input = input.substring(filter.length());
                if(input.startsWith(">="))
                    return value >= Long.parseLong(input.substring(2));
                if(input.startsWith(">"))
                    return value > Long.parseLong(input.substring(1));
                if(input.startsWith("<="))
                    return value <= Long.parseLong(input.substring(2));
                if(input.startsWith("<"))
                    return value < Long.parseLong(input.substring(1));
                if(input.startsWith("="))
                {
                    if(input.startsWith("=="))
                        return value == Long.parseLong(input.substring(2));
                    return value == Long.parseLong(input.substring(1));
                }
                if((input.startsWith("[") || input.startsWith("(")) && (input.endsWith("]") || input.endsWith(")")))
                {
                    boolean startInclusive = input.startsWith("[");
                    boolean endInclusive = input.endsWith("]");
                    input = input.substring(1,input.length() - 1);
                    String[] split = input.split(",",2);
                    long start = Long.parseLong(split[0]);
                    long end = Long.parseLong(split[1]);
                    if(end < start)
                        return false;
                    return (startInclusive && value == start) || (endInclusive && value == end) || (value < end && value > start);
                }
                //Unsupported integer range input
                return false;
            } catch (NumberFormatException e) { return false; }
        };
    }

    public static Predicate<Long> longRange(PendingSearch search, String filter)
    {
        AtomicReference<Predicate<Long>> holder = new AtomicReference<>();
        search.setupUnfiltered((input) -> {
            try {
                if(!input.startsWith(filter))
                    return PredicateWithResult.createNull(holder);
                input = input.substring(filter.length());
                if(input.startsWith(">="))
                {
                    final long minValue = Long.parseLong(input.substring(2));
                    return PredicateWithResult.create(v -> v >= minValue,holder);
                }
                if(input.startsWith(">"))
                {
                    final long minValue = Long.parseLong(input.substring(1));
                    return PredicateWithResult.create(v -> v > minValue,holder);
                }
                if(input.startsWith("<="))
                {
                    final long maxValue = Long.parseLong(input.substring(2));
                    return PredicateWithResult.create(v -> v <= maxValue,holder);
                }
                if(input.startsWith("<"))
                {
                    final long maxValue = Long.parseLong(input.substring(1));
                    return PredicateWithResult.create(v -> v <= maxValue,holder);
                }
                if(input.startsWith("="))
                {
                    final long value;
                    if(input.startsWith("=="))
                        value = Long.parseLong(input.substring(2));
                    else
                        value = Long.parseLong(input.substring(1));
                    return PredicateWithResult.create(v -> v == value,holder);
                }
                if((input.startsWith("[") || input.startsWith("(")) && (input.endsWith("]") || input.endsWith(")")))
                {
                    final boolean startInclusive = input.startsWith("[");
                    final boolean endInclusive = input.endsWith("]");
                    input = input.substring(1,input.length() - 1);
                    String[] split = input.split(",",2);
                    final long start = Long.parseLong(split[0]);
                    final long end = Long.parseLong(split[1]);
                    if(end < start)
                        return PredicateWithResult.createNull(holder);
                    return PredicateWithResult.create(v -> (startInclusive && v == start) || (endInclusive && v == end) || (v < end && v > start),holder);
                }
                //Unsupported integer range input
                return PredicateWithResult.createNull(holder);
            } catch (NumberFormatException e) { return PredicateWithResult.createNull(holder); }
        });
        return holder.get();
    }

    public static void floatRange(PendingSearch search, String filter,float value) { search.processUnfiltered(floatRange(filter,value)); }

    public static Predicate<String> floatRange(String filter, float value)
    {
        return input -> {
            try {
                if(!input.startsWith(filter))
                    return false;
                input = input.substring(filter.length());
                if(input.startsWith(">="))
                    return value >= Float.parseFloat(input.substring(2));
                if(input.startsWith(">"))
                    return value > Float.parseFloat(input.substring(1));
                if(input.startsWith("<="))
                    return value <= Float.parseFloat(input.substring(2));
                if(input.startsWith("<"))
                    return value < Float.parseFloat(input.substring(1));
                if(input.startsWith("="))
                {
                    if(input.startsWith("=="))
                        return value == Float.parseFloat(input.substring(2));
                    return value == Float.parseFloat(input.substring(1));
                }
                if((input.startsWith("[") || input.startsWith("(")) && (input.endsWith("]") || input.endsWith(")")))
                {
                    boolean startInclusive = input.startsWith("[");
                    boolean endInclusive = input.endsWith("]");
                    input = input.substring(1,input.length() - 2);
                    String[] split = input.split(",",2);
                    float start = Float.parseFloat(split[0]);
                    float end = Float.parseFloat(split[1]);
                    if(end < start)
                        return false;
                    return (startInclusive && value == start) || (endInclusive && value == end) || (value < end && value > start);
                }
                //Unsupported float range input
                return false;
            } catch (NumberFormatException e) { return false; }
        };
    }

    public static Predicate<Float> floatRange(PendingSearch search, String filter)
    {
        AtomicReference<Predicate<Float>> holder = new AtomicReference<>();
        search.setupUnfiltered((input) -> {
            try {
                if(!input.startsWith(filter))
                    return PredicateWithResult.createNull(holder);
                input = input.substring(filter.length());
                if(input.startsWith(">="))
                {
                    final float minValue = Float.parseFloat(input.substring(2));
                    return PredicateWithResult.create(v -> v >= minValue,holder);
                }
                if(input.startsWith(">"))
                {
                    final float minValue = Float.parseFloat(input.substring(1));
                    return PredicateWithResult.create(v -> v > minValue,holder);
                }
                if(input.startsWith("<="))
                {
                    final float maxValue = Float.parseFloat(input.substring(2));
                    return PredicateWithResult.create(v -> v <= maxValue,holder);
                }
                if(input.startsWith("<"))
                {
                    final float maxValue = Float.parseFloat(input.substring(1));
                    return PredicateWithResult.create(v -> v <= maxValue,holder);
                }
                if(input.startsWith("="))
                {
                    final float value;
                    if(input.startsWith("=="))
                        value = Float.parseFloat(input.substring(2));
                    else
                        value = Float.parseFloat(input.substring(1));
                    return PredicateWithResult.create(v -> v == value,holder);
                }
                if((input.startsWith("[") || input.startsWith("(")) && (input.endsWith("]") || input.endsWith(")")))
                {
                    final boolean startInclusive = input.startsWith("[");
                    final boolean endInclusive = input.endsWith("]");
                    input = input.substring(1,input.length() - 1);
                    String[] split = input.split(",",2);
                    final float start = Float.parseFloat(split[0]);
                    final float end = Float.parseFloat(split[1]);
                    if(end < start)
                        return PredicateWithResult.createNull(holder);
                    return PredicateWithResult.create(v -> (startInclusive && v == start) || (endInclusive && v == end) || (v < end && v > start),holder);
                }
                //Unsupported integer range input
                return PredicateWithResult.createNull(holder);
            } catch (NumberFormatException e) { return PredicateWithResult.createNull(holder); }
        });
        return holder.get();
    }

    public static void checkStockCount(PendingSearch search, TraderData trader) {
        int stockCount = 0;
        TradeContext context = TradeContext.createStorageMode(trader);
        for(TradeData trade : trader.getTradeData())
        {
            if(trade.isValid() && trade.hasStock(context))
                stockCount++;
        }
        intRange(search,BasicSearchFilter.STOCK_COUNT,stockCount);
    }

    public static Predicate<TradeData> getTradeFilter(PendingSearch search, TraderData trader)
    {
        TradeContext context = TradeContext.createStorageMode(trader);
        Predicate<Integer> stockTest = intRange(search,BasicSearchFilter.TRADE_STOCK);
        return t -> stockTest.test(t.getStock(context));
    }



}