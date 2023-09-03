package io.github.lightman314.lightmanscurrency.common.core.groups;

import io.github.lightman314.lightmanscurrency.common.core.variants.IOptionalKey;

import java.util.function.Function;

public enum BundleRequestFilter {
    ALL(o -> true),
    VANILLA(o -> { if(o instanceof IOptionalKey ok) return ok.isVanilla(); return true; } ),
    MODDED(o -> { if(o instanceof IOptionalKey ok) return ok.isModded(); return true; } );
    private final Function<Object,Boolean> filter;
    BundleRequestFilter(Function<Object,Boolean> filter) { this.filter = filter; }
    public final boolean filterKey(Object key) { return this.filter.apply(key); }

}
