package io.github.lightman314.lightmanscurrency.common.money.ancient_money;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.NumberUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AncientMoneyParser extends MoneyValueParser {

    public static final MoneyValueParser INSTANCE = new AncientMoneyParser();

    public static final DynamicCommandExceptionType NOT_AN_ANCIENT_COIN_EXCEPTION = new DynamicCommandExceptionType(LCText.ARGUMENT_MONEY_VALUE_NOT_AN_ANCIENT_COIN::get);

    private AncientMoneyParser() { super("ancient"); }

    @Override
    protected MoneyValue parseValueArgument(@Nonnull StringReader reader) throws CommandSyntaxException {
        Map<AncientCoinType,Integer> map = new HashMap<>();
        String s1 = MoneyValueParser.readStringUntil(reader, '-',',');
        if(NumberUtil.IsInteger(s1))
        {
            int count = NumberUtil.GetIntegerValue(s1, 1);
            String s2 = MoneyValueParser.readStringUntil(reader,',');
            AncientCoinType type = TryParseCoin(reader, s2);
            return AncientMoneyValue.of(type,count);
        }
        else
        {
            AncientCoinType type = TryParseCoin(reader, s1);
            return AncientMoneyValue.of(type,1);
        }
    }

    @Override
    protected String writeValueArgument(@Nonnull MoneyValue value) {
        if(value instanceof AncientMoneyValue val)
        {
            return String.valueOf(val.count) + '-' + val.type;
        }
        return null;
    }

    private static AncientCoinType TryParseCoin(StringReader reader, String coinIDString) throws CommandSyntaxException
    {
        AncientCoinType type = EnumUtil.enumFromString(coinIDString, AncientCoinType.values(),null);
        if(type == null)
            throw NOT_AN_ANCIENT_COIN_EXCEPTION.createWithContext(reader,coinIDString);
        return type;
    }

    @Nonnull
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@Nonnull CommandContext<S> context, @Nonnull SuggestionsBuilder builder, @Nonnull String trail, @Nonnull HolderLookup<Item> lookup) {

        String[] split = trail.split("-");
        if(trail.indexOf('-') >= 0)
        {
            String idPart = split.length > 1 ? split[1] : "";
            String previous = split[0] + "-";

            //We're on the item input part suggest coin items
            for(AncientCoinType type : AncientCoinType.values())
            {
                String coinString = type.toString();
                if(matchesSubStr(idPart, coinString) || idPart.isEmpty())
                    this.suggest(builder,previous + coinString);
            }
        }
        else //Otherwise, don't suggest anything
            return Suggestions.empty();
        return builder.buildFuture();
    }

    private static boolean matchesSubStr(String input, String resource) {
        for(int i = 0; !resource.startsWith(input, i); ++i) {
            i = resource.indexOf(95, i);
            if (i < 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void addExamples(@Nonnull List<String> examples) { examples.add(this.prefix + ";5-COPPER"); }

}
