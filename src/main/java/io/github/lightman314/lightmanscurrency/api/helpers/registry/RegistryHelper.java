package io.github.lightman314.lightmanscurrency.api.helpers.registry;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;

import java.util.Optional;

public final class RegistryHelper {
    private RegistryHelper() {}

    public static <T> boolean isTag(Registry<T> registry,TagKey<T> tag,T entry) {
        Optional<HolderSet.Named<T>> set = registry.get(tag);
        return set.isPresent() && set.get().stream().anyMatch(h -> h.value() == entry);
    }

    public static <T> int hash(Registry<T> registry,T entry) { return registry.getId(entry); }
    public static <T> String toString(String name,Registry<T> registry,T entry) { return name + "[" + registry.getKey(entry) + "]"; }

}