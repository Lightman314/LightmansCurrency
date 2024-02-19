package io.github.lightman314.lightmanscurrency.api.money.value;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class MoneyValueParser {

    public final String prefix;

    protected MoneyValueParser(@Nonnull String prefix) { this.prefix = prefix; }

    protected boolean tryParse(@Nullable String prefix) { return this.prefix.equals(prefix); }

    protected abstract MoneyValue parseValueArgument(@Nonnull StringReader reader) throws CommandSyntaxException;

    @Nullable
    protected final String tryWrite(@Nonnull MoneyValue value)
    {
        String sub = this.writeValueArgument(value);
        if(sub != null)
            return this.prefix + ";" + sub;
        return null;
    }

    protected abstract String writeValueArgument(@Nonnull MoneyValue value);

    @Nonnull
    public <S> CompletableFuture<Suggestions> listSuggestions(final @Nonnull CommandContext<S> context, final @Nonnull SuggestionsBuilder builder, @Nonnull String trail, @Nonnull HolderLookup<Item> items) { return Suggestions.empty(); }

    public void addExamples(@Nonnull List<String> examples) {}

    /**
     * Safely parses a config value string as a MoneyValue
     * If an error occurs while parsing the value, it will return the default value instead.
     */
    @Nonnull
    public static MoneyValue ParseConfigString(String string, Supplier<MoneyValue> defaultValue) {
        try{
            return parse(new StringReader(string), true);
        } catch(CommandSyntaxException exception) {
            LightmansCurrency.LogError("Error parsing Money Value config input.", exception);
            return defaultValue.get();
        }
    }

    /**
     * Parses the argument as a MoneyValue
     * Used by {@link io.github.lightman314.lightmanscurrency.common.commands.arguments.MoneyValueArgument} for command arguments,
     * and by {@link #ParseConfigString(String, Supplier)} for Config Values.
     */
    @Nonnull
    public static MoneyValue parse(StringReader reader, boolean allowEmpty) throws CommandSyntaxException {
        StringReader inputReader = new StringReader(readStringUntil(reader, ' '));
        String prefix;
        if(inputReader.getString().contains(";"))
            prefix = readStringUntil(inputReader,';');
        else
            prefix = null;
        for(CurrencyType type : MoneyAPI.API.AllCurrencyTypes())
        {
            MoneyValueParser parser = type.getValueParser();
            if(parser != null && parser.tryParse(prefix))
            {
                StringReader readerCopy = new StringReader(inputReader);
                MoneyValue value = parser.parseValueArgument(readerCopy);
                if (value != null) {
                    if(allowEmpty)
                        return value;
                    if (value.isEmpty() || value.isFree())
                        throw NoValueException(reader);
                    return value;
                }
            }
        }
        throw NoValueException(inputReader);
    }

    @Nonnull
    public static String writeParsable(@Nonnull MoneyValue value) {
        for(CurrencyType type : MoneyAPI.API.AllCurrencyTypes())
        {
            MoneyValueParser parser = type.getValueParser();
            if(parser != null)
            {
                String result = parser.tryWrite(value);
                if(result != null)
                    return result;
            }
        }
        return "ERROR";
    }

    public static String readStringUntil(StringReader reader, char... t) throws CommandSyntaxException {
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

    public static final CommandExceptionType EXCEPTION_TYPE = new CommandExceptionType() {
        @Override
        public int hashCode() { return super.hashCode(); }
    };

}
