package io.github.lightman314.lightmanscurrency.api.config.options;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class MapLikeOption<T> extends ConfigOption<T> {

    protected MapLikeOption(Supplier<T> defaultValue) { super(defaultValue); }

    public abstract Pair<Boolean,ConfigParsingException> editMap(String value, String key, boolean isSet);

}