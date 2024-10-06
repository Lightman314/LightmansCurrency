package io.github.lightman314.lightmanscurrency.api.config.options;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class MapLikeOption<T> extends ConfigOption<T> {

    protected MapLikeOption(@Nonnull Supplier<T> defaultValue) { super(defaultValue); }

    @Nonnull
    public abstract Pair<Boolean,ConfigParsingException> editMap(@Nonnull String value, @Nonnull String key, boolean isSet);

}