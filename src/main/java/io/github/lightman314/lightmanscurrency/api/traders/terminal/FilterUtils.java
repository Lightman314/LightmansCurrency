package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.filters.ItemTraderSearchFilter;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FilterUtils {

    private FilterUtils() {}

    public static void boolCheck(PendingSearch search, String filter, boolean value) {
        search.processStrictFilter(filter,bool -> "true".contains(bool.toLowerCase()) == value);
    }

    public static void intRange(PendingSearch search, String filter,int value) {
        search.processUnfiltered(input -> {
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
        });
    }

    public static void longRange(PendingSearch search, String filter,long value) {
        search.processUnfiltered(input -> {
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
        });
    }

    public static void floatRange(PendingSearch search, String filter,float value) {
        search.processUnfiltered(input -> {
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
        });
    }

    public static StockRequirement getStockRequirement(PendingSearch search, TraderData trader) {
        AtomicBoolean hasFrom = new AtomicBoolean(false);
        AtomicInteger from = new AtomicInteger(0);
        AtomicBoolean hasTo = new AtomicBoolean(false);
        AtomicInteger to = new AtomicInteger(0);
        var filter = ItemTraderSearchFilter.STOCK_COUNT;
        search.processUnfiltered(input -> {
            try {
                if (!input.startsWith(filter))
                    return false;
                input = input.substring(filter.length());
                if (input.startsWith(">=")) {
                    from.set(Integer.parseInt(input.substring(2)));
                    hasFrom.set(true);
                    return true;
                }
                if (input.startsWith(">")) {
                    from.set(Integer.parseInt(input.substring(1)) + 1);
                    hasFrom.set(true);
                    return true;
                }
                if (input.startsWith("<=")) {
                    to.set(Integer.parseInt(input.substring(2)));
                    hasTo.set(true);
                    return true;
                }
                if (input.startsWith("<")) {
                    to.set(Integer.parseInt(input.substring(1)) - 1);
                    hasTo.set(true);
                    return true;
                }
                if (input.startsWith("=")) {
                    int value;
                    if (input.startsWith("=="))
                        value = Integer.parseInt(input.substring(2));
                    else
                        value = Integer.parseInt(input.substring(1));
                    from.set(value);
                    hasFrom.set(true);
                    to.set(value);
                    hasTo.set(true);
                    return true;
                }
                if ((input.startsWith("[") || input.startsWith("(")) && (input.endsWith("]") || input.endsWith(")"))) {
                    boolean startInclusive = input.startsWith("[");
                    boolean endInclusive = input.endsWith("]");
                    input = input.substring(1, input.length() - 1);
                    String[] split = input.split(",", 2);
                    int start = Integer.parseInt(split[0]);
                    int end = Integer.parseInt(split[1]);
                    if (end < start)
                        return false;
                    from.set(startInclusive ? start : start + 1);
                    hasFrom.set(true);
                    to.set(endInclusive ? end : end - 1);
                    hasTo.set(true);
                    return true;
                }
                //Unsupported integer range input
                return false;
            } catch (NumberFormatException e) {
                return false;
            }
        });

        return new StockRequirement(hasFrom.get(), from.get(), hasTo.get(), to.get());
    }

    public record StockRequirement(boolean hasFrom, int from, boolean hasTo, int to) {
        public boolean satisfied(int stock) {
            if (hasFrom && hasTo)
                return stock >= from && stock <= to;

            if (hasFrom)
                return stock >= from;

            if (hasTo)
                return stock <= to;

            return true;
        }
    }
}