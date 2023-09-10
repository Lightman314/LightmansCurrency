package io.github.lightman314.lightmanscurrency.common.core.groups;

import io.github.lightman314.lightmanscurrency.common.core.variants.IOptionalKey;

import java.util.function.Predicate;

public enum BundleRequestFilter {
    ALL(o -> true),
    VANILLA(o -> { if(o instanceof IOptionalKey ok) return ok.isVanilla(); return true; } ),
    MODDED(o -> { if(o instanceof IOptionalKey ok) return ok.isModded(); return true; } );
    private final Predicate<Object> filter;
    BundleRequestFilter(Predicate<Object> filter) { this.filter = filter; }
    public final boolean filterKey(Object key) { return this.filter.test(key); }
}