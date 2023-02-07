package io.github.lightman314.lightmanscurrency.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.NumberUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CoinValueArgument implements ArgumentType<CoinValue> {

    private CoinValueArgument() {}

    public static CoinValueArgument argument() { return new CoinValueArgument(); }

    public static CoinValue getCoinValue(CommandContext<CommandSourceStack> commandContext, String name) {
        return commandContext.getArgument(name, CoinValue.class);
    }

    @Override
    public CoinValue parse(StringReader reader) throws CommandSyntaxException {
        CoinValue value = new CoinValue();
        StringReader inputReader = new StringReader(readStringUntil(reader, ' '));
        while(inputReader.canRead())
        {
            String s1 = readStringUntil(inputReader, '-',',');
            if(NumberUtil.IsInteger(s1))
            {
                int count = NumberUtil.GetIntegerValue(s1, 1);
                String s2 = readStringUntil(inputReader,',');
                TryParseCoin(value, inputReader, s2, count);
            }
            else
            {
                TryParseCoin(value, inputReader, s1, 1);
            }
        }
        if(!value.hasAny())
            throw NoValueException(reader);
        return value;
    }

    private static void TryParseCoin(CoinValue result, StringReader reader, String coinIDString, int count) throws CommandSyntaxException
    {
        if(ResourceLocation.isValidResourceLocation(coinIDString))
        {
            ResourceLocation coinID = new ResourceLocation(coinIDString);
            Item coin = ForgeRegistries.ITEMS.getValue(coinID);
            if(!MoneyUtil.isVisibleCoin(coin))
                throw NotACoinException(coinIDString, reader);
            result.addValue(coin, count);
        }
        else
            throw NotACoinException(coinIDString, reader);
    }

    public String readStringUntil(StringReader reader, char... t) throws CommandSyntaxException {
        List<Character> terminators = new ArrayList<>();
        for(char c : t)
            terminators.add(c);
        final StringBuilder result = new StringBuilder();
        boolean escaped = false;
        while (reader.canRead()) {
            final char c = reader.read();
            if (escaped) {
                if (terminators.contains(c) || c == '\\') {
                    result.append(c);
                    escaped = false;
                } else {
                    reader.setCursor(reader.getCursor() - 1);
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape().createWithContext(reader, String.valueOf(c));
                }
            } else if (c == '\\') {
                escaped = true;
            } else if (terminators.contains(c)) {
                return result.toString();
            } else {
                result.append(c);
            }
        }
        //If end is reached, assume end
        return result.toString();
    }

    public static CommandSyntaxException NoValueException(StringReader reader) {
        return new CommandSyntaxException(EXCEPTION_TYPE, EasyText.translatable("command.argument.coinvalue.novalue"), reader.getString(), reader.getCursor());
    }

    public static CommandSyntaxException NotACoinException(String item, StringReader reader) {
        return new CommandSyntaxException(EXCEPTION_TYPE, EasyText.translatable("command.argument.coinvalue.notacoin", item), reader.getString(), reader.getCursor());
    }

    private static final CommandExceptionType EXCEPTION_TYPE = new CommandExceptionType() {
        @Override
        public int hashCode() {
            return super.hashCode();
        }
    };

    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        int suggestedCount = 1;
        StringBuilder result = new StringBuilder();
        List<Item> coins = MoneyUtil.getAllCoins();
        for(int i = 0; i < coins.size(); ++i)
        {
            Item coin = coins.get(i);
            if(i > 0)
                result.append(",");
            result.append(suggestedCount++).append("-").append(ForgeRegistries.ITEMS.getKey(coin).toString());
        }
        builder.suggest(result.toString());
        return builder.buildFuture();
    }

    public Collection<String> getExamples() {
        int suggestedCount = 1;
        StringBuilder result = new StringBuilder();
        List<Item> coins = MoneyUtil.getAllCoins();
        for(int i = 0; i < coins.size(); ++i)
        {
            Item coin = coins.get(i);
            if(i > 0)
                result.append(",");
            result.append(suggestedCount++).append("-").append(ForgeRegistries.ITEMS.getKey(coin).toString());
        }
        return Collections.singletonList(result.toString());
    }



}
