package io.github.lightman314.lightmanscurrency.common.core.variants;

public interface IOptionalKey {
    boolean isModded();
    default boolean isVanilla() { return !this.isModded(); }
}
