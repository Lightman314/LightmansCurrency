package io.github.lightman314.lightmanscurrency.api.config.options;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class ListOption<T> extends ListLikeOption<List<T>> {


    protected ListOption(@Nonnull Supplier<List<T>> defaultValue) { super(defaultValue); }

    public static <T> ConfigParser<List<T>> makeParser(ConfigParser<T> partialParser) { return new ListParser<>(partialParser); }

    @Nonnull
    @Override
    protected ConfigParser<List<T>> getParser() { return makeParser(this.getPartialParser()); }

    protected abstract ConfigParser<T> getPartialParser();

    @Nonnull
    public final Pair<Boolean,ConfigParsingException> editList(String value, int index, boolean isEdit) {
        if(index < 0 && isEdit)
        {
            //Add value
            try {
                T newValue = this.getPartialParser().tryParse(cleanWhitespace(value));
                List<T> currentValue = this.getCurrentValue();
                currentValue.add(newValue);
                this.set(currentValue);
                return Pair.of(true,null);
            } catch (ConfigParsingException e) { return Pair.of(false,e); }
        }
        if(index >= 0)
        {
            List<T> currentValue = this.getCurrentValue();
            if(index >= currentValue.size())
                return Pair.of(false, new ConfigParsingException("Invalid index. Maximum is " + (currentValue.size() - 1) + "!"));
            if(isEdit)
            {
                //Replace action
                try {
                    T newValue = this.getPartialParser().tryParse(cleanWhitespace(value));
                    currentValue.set(index, newValue);
                    this.set(currentValue);
                    return Pair.of(true,null);
                } catch (ConfigParsingException e) { return Pair.of(false, e); }
            }
            else
            {
                //Remove action
                currentValue.remove(index);
                this.set(currentValue);
                return Pair.of(true,null);
            }
        }
        return Pair.of(false, new ConfigParsingException("Invalid edit action. Cannot have an index of " + index + " without the isEdit flag!"));
    }

    @Override
    public final int getSize() { return this.get().size(); }

    private static class ListParser<T> implements ConfigParser<List<T>>
    {
        private final ConfigParser<T> parser;
        private ListParser(@Nonnull ConfigParser<T> parser) { this.parser = parser; }
        @Nonnull
        @Override
        public List<T> tryParse(@Nonnull String cleanLine) throws ConfigParsingException {
            if(cleanLine.isEmpty())
                throw new ConfigParsingException("Empty input received!");
            char c1 = cleanLine.charAt(0);
            if(c1 != '[')
                throw new ConfigParsingException("List does not start with '['!");
            char lastChar = cleanLine.charAt(cleanLine.length() - 1);
            if(lastChar != ']')
                throw new ConfigParsingException("List does not end with ']'!");

            List<String> sections = new ArrayList<>();
            StringBuilder temp = new StringBuilder();
            boolean inQuotes = false;
            boolean escaped = false;
            for(int i = 1; i < cleanLine.length() - 1; ++i)
            {
                char c = cleanLine.charAt(i);
                if(escaped)
                {
                    if(c == '\\' || c == '"')
                        temp.append(c);
                    else
                        temp.append('\\').append(c);
                    escaped = false;
                    continue;
                }
                if(c == '\\')
                {
                    escaped = true;
                    continue;
                }
                if(c == '"')
                {
                    inQuotes = !inQuotes;
                    continue;
                }
                if(c == ',')
                {
                    sections.add(temp.toString());
                    temp = new StringBuilder();
                    continue;
                }
                temp.append(c);
            }
            if(!temp.isEmpty())
                sections.add(temp.toString());
            if(sections.isEmpty())
                return new ArrayList<>();
            List<T> results = new ArrayList<>();
            for(int s = 0; s < sections.size(); ++s)
            {
                String section = sections.get(s);
                try { results.add(this.parser.tryParse(section));
                } catch (ConfigParsingException e) { LightmansCurrency.LogWarning("Failed to parse List Config entry #" + (s + 1), e); }
            }
            return results;
        }

        @Nonnull
        @Override
        public String write(@Nonnull List<T> value) {
            StringBuilder builder = new StringBuilder("[");
            boolean comma = false;
            for(T v : value)
            {
                if(comma)
                    builder.append(',');
                builder.append(this.parser.write(v));
                comma = true;
            }
            builder.append(']');
            return builder.toString();
        }
    }


}
