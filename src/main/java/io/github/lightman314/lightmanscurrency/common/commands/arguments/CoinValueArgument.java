package io.github.lightman314.lightmanscurrency.common.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.money.util.CoinValueParser;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CoinValueArgument implements ArgumentType<CoinValue> {


    private CoinValueArgument() {}

    public static CoinValueArgument argument() { return new CoinValueArgument(); }
    public static CoinValueArgument safeArgument(RegisterCommandsEvent event) { return argument(); }

    public static CoinValue getCoinValue(CommandContext<CommandSourceStack> commandContext, String name) throws CommandSyntaxException {
        return commandContext.getArgument(name, CoinValue.class);
    }

    @Override
    public CoinValue parse(StringReader reader) throws CommandSyntaxException {
        return CoinValueParser.parse(reader);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {

        String[] parts = builder.getRemaining().split(",");

        String finalPart = parts[parts.length - 1];
        String[] split = finalPart.split("-");
        if(finalPart.indexOf('-') >= 0)
        {
            String idPart = split.length > 1 ? split[1] : "";
            StringBuilder previousParts = new StringBuilder();
            for(int i = 0; i < parts.length - 1; ++i)
            {
                if(i > 0)
                    previousParts.append(",");
                previousParts.append(parts[i]);
            }
            try{
                CoinValueParser.parse(new StringReader(previousParts.toString()));
            } catch (CommandSyntaxException ignored) {
                //Failed to parse the previous parts, don't suggest anything as existing input is invalid
                return Suggestions.empty();
            }
            String previous = previousParts.append(split[0]).append("-").toString();

            //We're on the item input part suggest coin items
            List<ResourceLocation> coins = this.lookupCoinList();
            for(ResourceLocation coin : coins)
            {
                String coinString = coin.toString();
                if(matchesSubStr(idPart, coinString) || idPart.isEmpty())
                    builder.suggest(previous + coinString);
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

    private List<ResourceLocation> lookupCoinList() {
        return ForgeRegistries.ITEMS.getKeys().stream().filter(CoinValueArgument::isCoin).toList();
    }

    private static boolean isCoin(ResourceLocation itemID)
    {
        return MoneyUtil.isVisibleCoin(ForgeRegistries.ITEMS.getValue(itemID));
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