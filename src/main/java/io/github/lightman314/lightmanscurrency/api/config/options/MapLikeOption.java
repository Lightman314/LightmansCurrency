package io.github.lightman314.lightmanscurrency.api.config.options;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;

import java.util.function.Supplier;

public abstract class MapLikeOption<T> extends ConfigOption<T> {

    protected MapLikeOption(Supplier<T> defaultValue) { super(defaultValue); }

    public abstract Pair<Boolean,ConfigParsingException> editMap(String value, String key, boolean isSet);

}
