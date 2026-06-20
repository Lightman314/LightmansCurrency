package io.github.lightman314.lightmanscurrency.api.codecs.partial;

import java.util.function.Function;

public class PartialHelper {

    private PartialHelper() {}

    protected static <C,T extends C,X> Function<T,X> castFunctino(Function<C,X> function) { return function::apply; }

}
