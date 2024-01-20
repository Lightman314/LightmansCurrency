package io.github.lightman314.lightmanscurrency.api.config.options.parsing;

import javax.annotation.Nonnull;

public interface ConfigParser<T> {

    @Nonnull
    T tryParse(@Nonnull String cleanLine) throws ConfigParsingException;
    @Nonnull
    String write(@Nonnull T value);

}
