package io.github.lightman314.lightmanscurrency.api.helpers.registry.types;

public interface IOptionalKey {
    boolean isModded();
    default boolean isVanilla() { return !this.isModded(); }
}