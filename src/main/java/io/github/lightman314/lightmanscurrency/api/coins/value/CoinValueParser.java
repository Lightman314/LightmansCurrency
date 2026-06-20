package io.github.lightman314.lightmanscurrency.api.coins.value;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.helpers.NumberHelper;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.parsing.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import net.minecraft.IdentifierException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Optional;

public class CoinValueParser extends MoneyValueParser<CoinValue> {

    public static final CoinValueParser INSTANCE = new CoinValueParser();

    public static final DynamicCommandExceptionType NOT_A_COIN_EXCEPTION = new DynamicCommandExceptionType(LCText.Commands.ARGUMENT_MONEY_VALUE_NOT_A_COIN::get);
    public static final Dynamic2CommandExceptionType DIFFERENT_CHAIN_EXCEPTION = new Dynamic2CommandExceptionType(LCText.Commands.ARGUMENT_MONEY_VALUE_DIFFERENT_CHAIN::get);

    private CoinValueParser() { super("coin"); }

    //Default to this parser if no prefix is defined
    @Override
    protected boolean tryParse(@Nullable String prefix) { return prefix == null || super.tryParse(prefix); }

    @Override
    protected MoneyValue parseValueArgument(StringReader reader) throws CommandSyntaxException {
        PartialValue partial = PartialValue.empty();
        while(reader.canRead())
        {
            String s1 = readStringUntil(reader,'-',',');
            if(NumberHelper.isLong(s1))
            {
                long count = NumberHelper.getLong(s1,1L);
                String s2 = readStringUntil(reader,',');
                partial = tryParseCoin(partial,reader,s2,count);
            }
            else
                partial = tryParseCoin(partial,reader,s1,1);
        }
        return partial.build();
    }

    @Override
    protected String writeValueArgument(CoinValue value) {
        StringBuilder builder = new StringBuilder();
        boolean comma = false;
        for(CoinValuePair pair : value.getEntries())
        {
            if(!builder.isEmpty())
                builder.append(',');
            if(pair.amount <= 1)
                builder.append(BuiltInRegistries.ITEM.getKey(pair.coin));
            else
                builder.append(pair.amount).append('-').append(BuiltInRegistries.ITEM.getKey(pair.coin));
        }
        return builder.toString();
    }

    private static PartialValue tryParseCoin(PartialValue partial,StringReader reader,String coinIDString,long count) throws CommandSyntaxException
    {
        try {
            Identifier coinID = Identifier.parse(coinIDString);
            Item coin = BuiltInRegistries.ITEM.getValue(coinID);
            if(coin == null)
                throw NOT_A_COIN_EXCEPTION.createWithContext(reader,coinID);
            ChainData chain = LCApi.getCoinAPI().lookupChain(coin);
            if(chain == null)
                throw NOT_A_COIN_EXCEPTION.createWithContext(reader,coinID);
            CoinEntry entry = chain.findEntry(coin);
            if(entry == null || entry.isSideChain())
                throw NOT_A_COIN_EXCEPTION.createWithContext(reader,coinID);
            return partial.tryAdd(reader,chain,entry,count);
        } catch (IdentifierException e) { throw NOT_A_COIN_EXCEPTION.createWithContext(reader,coinIDString); }
    }

    private record PartialValue(Optional<ChainData> chain, long value) {
        static PartialValue empty() { return new PartialValue(Optional.empty(),0); }

        PartialValue tryAdd(StringReader reader,ChainData chain,CoinEntry entry,long count) throws CommandSyntaxException {
            if(this.chain.isPresent())
            {
                ChainData oldChain = this.chain.get();
                if(oldChain.chain.equals(chain.chain))
                    return new PartialValue(this.chain,this.value + (entry.getInternalValue() * count));
                throw DIFFERENT_CHAIN_EXCEPTION.createWithContext(reader,chain.chain,oldChain.chain);
            }
            return new PartialValue(Optional.of(chain),entry.getInternalValue() * count);
        }

        MoneyValue build() {
            if(this.chain.isPresent())
                return CoinValue.fromNumber(this.chain.get(),this.value);
            return MoneyValue.empty();
        }

    }

}
