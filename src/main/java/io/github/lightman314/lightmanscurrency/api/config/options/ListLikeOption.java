package io.github.lightman314.lightmanscurrency.api.config.options;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class ListLikeOption<T> extends ConfigOption<T> {

    protected ListLikeOption(@Nonnull Supplier<T> defaultValue) { super(defaultValue); }

    @Nonnull
    public abstract Pair<Boolean,ConfigParsingException> editList(String value, int index, boolean isEdit);

    public abstract int getSize();

}