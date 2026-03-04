package io.github.lightman314.lightmanscurrency.api.codecs.partial;

import java.util.function.Function;

public class PartialHelper {

    protected static <C,T extends C,X> Function<T,X> castFunction(Function<C,X> function) { return function::apply; }

}
