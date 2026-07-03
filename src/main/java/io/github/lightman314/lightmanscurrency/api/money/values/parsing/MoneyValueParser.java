package io.github.lightman314.lightmanscurrency.api.money.values.parsing;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueType;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class MoneyValueParser<T extends MoneyValue> {

    public static final SimpleCommandExceptionType NO_VALUE_EXCEPTION = new SimpleCommandExceptionType(LCText.Commands.ARGUMENT_MONEY_VALUE_NO_VALUE.get());

    public final String prefix;

    protected MoneyValueParser(String prefix) { this.prefix = prefix; }

    protected boolean tryParse(@Nullable String prefix) { return this.prefix.equals(prefix); }

    protected abstract MoneyValue parseValueArgument(StringReader reader) throws CommandSyntaxException;

    protected final Optional<String> unsafeWrite(MoneyValue value) {
        try { return Optional.of(this.write((T)value));
        } catch (ClassCastException exception) { return Optional.empty(); }
    }
    protected final String write(T value) { return this.prefix + ";" + this.writeValueArgument(value); }

    protected abstract String writeValueArgument(T value);

    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder, String trail, HolderLookup<Item> items) { return Suggestions.empty(); }

    protected void suggest(SuggestionsBuilder builder, String value) {
        builder.suggest(this.prefix + ";" + value);
    }

    public void addExamples(List<String> examples) {}

    /**
     * Parses the argument as a MoneyValue
     * Used by {@link io.github.lightman314.lightmanscurrency.api.command.arguments.MoneyValueArgument MoneyValueArgument} for command arguments
     */
    public static MoneyValue parse(StringReader reader,boolean allowEmpty,boolean allowFree) throws CommandSyntaxException {
        StringReader inputReader = new StringReader(readArgument(reader));
        String prefix;
        if(inputReader.getString().contains(";"))
            prefix = readStringUntil(inputReader,';');
        else
            prefix = null;
        CommandSyntaxException exception = null;
        for(MoneyValueType<?> type : LCRegistries.Money.VALUE_TYPE)
        {
            MoneyValueParser<?> parser = type.parser();
            if(parser != null && parser.tryParse(prefix))
            {
                StringReader readerCopy = new StringReader(inputReader);
                boolean noValueExc = false;
                try {
                    MoneyValue value = parser.parseValueArgument(readerCopy);
                    if (value != null) {
                        if(value.isEmpty() && !allowEmpty)
                        {
                            if(value.isFree() && allowFree)
                                return value;
                            noValueExc = true;
                        }
                        else
                            return value;
                    }
                } catch (CommandSyntaxException e) {
                    //Catch and store the exception for later, just in case two different parsers are using the same prefix
                    exception = e;
                }
                if(noValueExc)
                    throw NO_VALUE_EXCEPTION.createWithContext(reader);
            }
        }
        throw exception == null ? NO_VALUE_EXCEPTION.createWithContext(reader) : exception;
    }

    public static String writeParsable(MoneyValue value) {
        return value.getType().parser().unsafeWrite(value).orElse("ERROR");
    }

    //Emulates StringReader#ReadUnquotedString, but without forcing certain allowed characters
    private static String readArgument(StringReader reader)
    {
        final int start = reader.getCursor();
        while(reader.canRead() && !(reader.peek() == ' '))
            reader.skip();
        return reader.getString().substring(start, reader.getCursor());
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

}