package io.github.lightman314.lightmanscurrency.api.config.options.parsing;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MappedConfigParser<X,T> implements ConfigParser<T> {

    private final ConfigParser<X> original;
    private final Function<T,X> write;
    private final ParsingFunction<X,T> read;
    public MappedConfigParser(ConfigParser<X> original,ParsingFunction<X,T> read,Function<T, X> write) {
        this.original = original;
        this.read = read;
        this.write = write;
    }

    @Override
    public T tryParse(String cleanLine) throws ConfigParsingException {
        X val = this.original.tryParse(cleanLine);
        return this.read.apply(val);
    }

    @Override
    public String write(T value) { return this.original.write(this.write.apply(value)); }

}
