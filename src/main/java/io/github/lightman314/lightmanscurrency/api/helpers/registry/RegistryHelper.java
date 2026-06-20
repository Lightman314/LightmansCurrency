package io.github.lightman314.lightmanscurrency.api.helpers.registry;

import net.minecraft.core.Registry;

public final class RegistryHelper {
    private RegistryHelper() {}

    public static <T> int hash(Registry<T> registry,T entry) { return registry.getId(entry); }
    public static <T> String toString(String name,Registry<T> registry,T entry) { return name + "[" + registry.getKey(entry) + "]"; }

}