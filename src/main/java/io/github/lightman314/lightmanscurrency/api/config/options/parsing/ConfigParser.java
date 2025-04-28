package io.github.lightman314.lightmanscurrency.api.config.options.parsing;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface ConfigParser<T> {

    T tryParse(String cleanLine) throws ConfigParsingException;
    String write(T value);

    default <X> ConfigParser<X> map(Function<T,X> write,Function<X,T> read) { return new MappedConfigParser<>(this,write,read); }

}
