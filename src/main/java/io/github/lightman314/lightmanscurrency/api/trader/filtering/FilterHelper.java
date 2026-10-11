package io.github.lightman314.lightmanscurrency.api.trader.filtering;

import com.google.common.base.Predicates;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class FilterHelper {

    private FilterHelper() {}

    public static final String TYPE = "type";
    public static final String TRADE_TYPE = "tradetype";
    public static final String OWNER = "owner";
    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String TRADE_COUNT = "trades";
    public static final String CUSTOMER_READY = "ready";
    public static final String STOCK_COUNT = "stock";
    public static final String TRADE_STOCK = "tradestock";
    public static final String TOOLTIP = "tooltip";
    public static final String ITEM = "item";

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
        AtomicReference<Predicate<Integer>> holder = new AtomicReference<>(null);
        search.setupUnfiltered((input) -> {
            try {
                if(!input.startsWith(filter) || holder.get() != null)
                    return null;
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
                        return null;
                    return PredicateWithResult.create(v -> (startInclusive && v == start) || (endInclusive && v == end) || (v < end && v > start),holder);
                }
                //Unsupported integer range input
                return null;
            } catch (NumberFormatException e) { return null; }
        });
        return Objects.requireNonNullElseGet(holder.get(),Predicates::alwaysTrue);
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
                    return null;
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
                        return null;
                    return PredicateWithResult.create(v -> (startInclusive && v == start) || (endInclusive && v == end) || (v < end && v > start),holder);
                }
                //Unsupported integer range input
                return null;
            } catch (NumberFormatException e) { return null; }
        });
        return Objects.requireNonNullElseGet(holder.get(),Predicates::alwaysTrue);
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
                    return null;
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
                        return null;
                    return PredicateWithResult.create(v -> (startInclusive && v == start) || (endInclusive && v == end) || (v < end && v > start),holder);
                }
                //Unsupported integer range input
                return null;
            } catch (NumberFormatException e) { return null; }
        });
        return Objects.requireNonNullElseGet(holder.get(),Predicates::alwaysTrue);
    }

    public static boolean filterItem(ItemStack stack,String searchText,HolderLookup.Provider registries) {
        if(!stack.isEmpty()) {
            //Check Item Name
            if(stack.getHoverName().getString().toLowerCase().contains(searchText))
                return true;
            //Check Item ID
            if(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().contains(searchText))
                return true;
            //Check Item Enchantments
            try {
                ItemEnchantments enchantments = stack.getAllEnchantments(registries.lookupOrThrow(Registries.ENCHANTMENT));
                for(var ench : enchantments.entrySet()) {
                    if(Enchantment.getFullname(ench.getKey(),ench.getIntValue()).getString().toLowerCase().contains(searchText))
                        return true;
                    if(ench.getKey().getRegisteredName().contains(searchText))
                        return true;
                }
            } catch (IllegalStateException ignored) {}
        }
        return false;
    }

    public static boolean filterItemTooltips(ItemStack stack,String searchText,HolderLookup.Provider registries) {
        if(!stack.isEmpty()) {
            List<Component> tooltip = stack.getTooltipLines(Item.TooltipContext.of(registries),null, TooltipFlag.NORMAL);
            return tooltip.stream().anyMatch(line -> line.getString().toLowerCase().contains(searchText));
        }
        return false;
    }

    public static Predicate<String> filterItems(List<ItemStack> items,HolderLookup.Provider registries) { return filterItemsWithContext(items.stream().map(ItemWithContext::new).toList(),registries); }

    public static Predicate<String> filterItemsWithContext(List<ItemWithContext> items,HolderLookup.Provider registries) {
        return input -> {
            for(ItemWithContext item : items) {
                if(item.isEmpty())
                    continue;
                if(filterItem(item.item,input,registries))
                    return true;
                if(item.customName != null && !item.customName.isBlank() && item.customName.toLowerCase().contains(input))
                    return true;
            }
            return false;
        };
    }

    public static Predicate<String> filterItemTooltips(List<ItemStack> items,HolderLookup.Provider registries) {
        return input -> {
            for(ItemStack item : items) {
                if(filterItemTooltips(item,input,registries))
                    return true;
            }
            return false;
        };
    }

    public static Predicate<String> filterItemTooltipsWithContext(List<ItemWithContext> items,HolderLookup.Provider registries) {
        return filterItemTooltips(items.stream().map(ItemWithContext::item).toList(),registries);
    }

    public record ItemWithContext(ItemStack item, @Nullable String customName) {
        public ItemWithContext(ItemStack item) { this(item,null); }
        public ItemWithContext(TradeItem item) { this(item.getStack(),item.getNameChange()); }
        public boolean isEmpty() { return this.item.isEmpty(); }
    }

}