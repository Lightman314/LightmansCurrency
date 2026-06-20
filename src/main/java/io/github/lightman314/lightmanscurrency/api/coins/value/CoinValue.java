package io.github.lightman314.lightmanscurrency.api.coins.value;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.money.values.ItemBasedValue;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

public class CoinValue extends ItemBasedValue {

    private static final MapCodec<CoinValue> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.fieldOf("chain").forGetter(CoinValue::getChain),
            CoinValuePair.CODEC.listOf().fieldOf("entries").forGetter(v -> v.entries)
    ).apply(builder,CoinValue::parseUntrusted));
    private static final StreamCodec<RegistryFriendlyByteBuf,CoinValue> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,CoinValue::getChain,
            CoinValuePair.STREAM_CODEC.apply(ByteBufCodecs.list()),v -> v.entries,
            CoinValue::parseUntrusted);

    public static final MoneyValueType<CoinValue> TYPE = new MoneyValueType<>(MAP_CODEC,STREAM_CODEC,CoinValueParser.INSTANCE);

    private final String chain;
    public String getChain() { return this.chain; }

    private final ImmutableList<CoinValuePair> entries;
    public List<CoinValuePair> getEntries() { return this.entries; }
    public long getEntry(Item coin)
    {
        for(CoinValuePair pair : this.entries)
        {
            if(pair.coin == coin)
                return pair.amount;
        }
        return 0;
    }

    private CoinValue(String chain, List<CoinValuePair> values)
    {
        this.chain = chain;
        this.entries = ImmutableList.copyOf(values);
    }

    @Override
    public MoneyValueType<?> getType() { return TYPE; }
    @Override
    protected MoneyKey generateKey() { return MoneyKey.create(TYPE,this.chain); }
    @Override
    protected MoneyValue copyWithInternalValue(long value) { return fromNumber(this.chain,value); }

    public static MoneyValue fromNumber(String chain,long internalValue) { return fromNumber(LCApi.getCoinAPI().lookupChain(chain),internalValue); }
    public static MoneyValue fromNumber(ChainData chain, long internalValue)
    {
        if(chain == null || internalValue <= 0)
            return empty();
        long pendingValue = internalValue;
        List<CoinEntry> entries = chain.getAllEntries(false, ChainData.SORT_HIGHEST_VALUE_FIRST);
        List<CoinValuePair> pairList = new ArrayList<>();
        for(CoinEntry entry : entries)
        {
            long entryValue = entry.getInternalValue();
            if(pendingValue >= entryValue && entryValue != 0)
            {
                long thisCount = pendingValue / entryValue;
                pendingValue = pendingValue % entryValue;
                if(thisCount > 0)
                    pairList.add(new CoinValuePair(entry.getCoin(),thisCount));
            }
            if(pendingValue <= 0)
                break;
        }
        return new CoinValue(chain.chain,pairList);
    }

    /**
     * Gets a non-empty coin value from either the value of the item,
     * or if the item is not a registered coin it falls back onto the given number value from the default "main" chain.
     */
    public static MoneyValue fromItemOrValue(Item coin, long value) { return fromItemOrValue(coin,1,value); }
    /**
     * Gets a non-empty coin value from either the value of the item multiplied by the item counbt,
     * or if the item is not a registered coin it falls back onto the given number value from the default "main" chain.
     */
    public static MoneyValue fromItemOrValue(Item coin, int itemCount, long value)
    {
        ChainData data = LCApi.getCoinAPI().lookupChain(coin);
        if(data != null)
            return parseUntrusted(data.chain, Lists.newArrayList(new CoinValuePair(coin,itemCount)));
        return fromNumber(CoinAPI.DEFAULT_CHAIN,value);
    }

    private static CoinValue parseUntrusted(String chain, List<CoinValuePair> entries)
    {
        List<CoinValuePair> list = roundValue(chain,entries);
        return new CoinValue(chain,list);
    }

    private static List<CoinValuePair> roundValue(String chain,List<CoinValuePair> list)
    {
        ChainData data = LCApi.getCoinAPI().lookupChain(chain);
        if(data == null)
            return list;
        while(needsRounding(data,list))
        {
            for(int i = 0; i < list.size(); ++i)
            {
                if(needsRounding(data,list,i))
                {
                    CoinValuePair pair = list.get(i);
                    CoinEntry entry = data.findEntry(pair.coin);
                    Pair<CoinEntry,Integer> exchange = entry.getUpperExchange();
                    long largeAmount = 0;
                    while(pair.amount >= exchange.getSecond())
                    {
                        largeAmount++;
                        pair = pair.removeAmount(exchange.getSecond());
                    }
                    if(pair.amount == 0)
                    {
                        list.remove(i);
                        //Shrink the index to avoid oversight
                        i--;
                    }
                    //Otherwise replace the list entry with the new input
                    else
                        list.set(i,pair);
                    //Add the larget amount to the price values list
                    for(int j = 0; j < list.size(); ++j)
                    {
                        if(exchange.getFirst().matches(list.get(j).coin))
                        {
                            list.set(j,list.get(j).addAmount(largeAmount));
                            largeAmount = 0;
                        }
                    }
                    if(largeAmount > 0)
                        list.add(new CoinValuePair(exchange.getFirst().getCoin(),largeAmount));
                }
            }
        }
        return sortValue(data,list);
    }

    private static List<CoinValuePair> sortValue(ChainData chain,List<CoinValuePair> list)
    {
        List<CoinValuePair> newList = new ArrayList<>();
        while(!list.isEmpty())
        {
            long largestValue = chain.getInternalValue(list.getFirst().coin);
            int largestIndex = 0;
            for(int i = 1; i < list.size(); ++i)
            {
                long thisValue = chain.getInternalValue(list.get(i).coin);
                if(thisValue > largestValue)
                {
                    largestIndex = i;
                    largestValue = thisValue;
                }
            }
            newList.add(list.get(largestIndex));
            list.remove(largestIndex);
        }
        return newList;
    }

    private static boolean needsRounding(ChainData chain,List<CoinValuePair> list)
    {
        for(int i = 0; i < list.size(); ++i)
        {
            if(needsRounding(chain,list,i))
                return true;
        }
        return false;
    }

    private static boolean needsRounding(ChainData chain,List<CoinValuePair> list,int index)
    {
        CoinValuePair pair = list.get(index);
        Pair<CoinEntry,Integer> exchange = chain.getUpperExchange(pair.coin);
        if(exchange != null)
            return pair.amount >= exchange.getSecond();
        return false;
    }

    @Override
    public List<ItemStack> getAsItemList() {
        List<ItemStack> items = new ArrayList<>();
        for(CoinValuePair entry : this.entries)
        {
            long amount = entry.amount;
            while(amount > 0)
            {
                int count = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)amount;
                amount -= count;
                items.add(new ItemStack(entry.coin,count));
            }
        }
        return items;
    }

    @Override
    public Component getText(Component emptyText) {
        ChainData chain = LCApi.getCoinAPI().lookupChain(this.chain);
        if(chain == null)
            return Component.literal("ERROR");
        else
            return chain.formatValue(this,emptyText);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getInternalValue() {
        ChainData chain = LCApi.getCoinAPI().lookupChain(this.chain);
        if(chain == null)
            return 0;
        long value = 0;
        for(CoinValuePair pair : this.entries)
            value += chain.getInternalValue(pair.coin) * pair.amount;
        return Math.max(0,value);
    }

}
