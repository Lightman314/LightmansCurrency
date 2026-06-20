package io.github.lightman314.lightmanscurrency.api.helpers.registry;

import java.util.function.Predicate;

public interface IOptionalKey {
    boolean isModded();
    default boolean isVanilla() { return !this.isModded(); }

    static <K extends IOptionalKey> Predicate<K> vanillaFilter() { return IOptionalKey::isVanilla; }
    static <K extends IOptionalKey> Predicate<K> moddedFilter() { return IOptionalKey::isModded; }
}