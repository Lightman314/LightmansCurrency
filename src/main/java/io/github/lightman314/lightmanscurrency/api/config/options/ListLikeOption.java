package io.github.lightman314.lightmanscurrency.api.config.options;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;

import java.util.function.Supplier;

public abstract class ListLikeOption<T> extends ConfigOption<T> {

    protected ListLikeOption(Supplier<T> defaultValue) { super(defaultValue); }

    public abstract Pair<Boolean,ConfigParsingException> editList(String value, int index, boolean isEdit);

    public abstract int getSize();

    public abstract boolean isValidEntryType(Class<?> clazz);

}