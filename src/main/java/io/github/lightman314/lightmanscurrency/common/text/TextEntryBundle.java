package io.github.lightman314.lightmanscurrency.common.text;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.ItemLike;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class TextEntryBundle<T> {

    private final Map<T,TextEntry> entryMap;

    public TextEntryBundle(Map<T,TextEntry> map) { this.entryMap = ImmutableMap.copyOf(map); }

    public static <T> TextEntryBundle<T> of(RegistryObjectBundle<? extends ItemLike,T> bundle) {
        Map<T,TextEntry> temp = new HashMap<>();
        bundle.forEach((key,item) -> temp.put(key,TextEntry.item(item)));
        return new TextEntryBundle<>(temp);
    }

    public static <T> TextEntryBundle<T> of(RegistryObjectBiBundle<? extends ItemLike,T,?> bundle) {
        Map<T,TextEntry> temp = new HashMap<>();
        bundle.forEach((key1,key2,item) -> temp.put(key1,TextEntry.item(item)));
        return new TextEntryBundle<>(temp);
    }

    public static <T extends Enum<T>> TextEntryBundle<T> of(T[] values, String prefix)
    {
        Map<T,TextEntry> temp = new HashMap<>();
        for(T key : values)
            temp.put(key,new TextEntry(prefix + "." + key.name().toLowerCase(Locale.ENGLISH)));
        return new TextEntryBundle<>(temp);
    }

    public static <T> TextEntryBundle<T> of(List<T> values, String prefix, Function<T,String> getKeyName)
    {
        Map<T,TextEntry> temp = new HashMap<>();
        for(T key : values)
            temp.put(key,new TextEntry(prefix + "." + getKeyName.apply(key)));
        return new TextEntryBundle<>(temp);
    }

    public TextEntry get(T key) { return this.entryMap.get(key); }

    public MutableComponent getComponent(T key) { return this.get(key).get(); }

    public void forEach(BiConsumer<T,TextEntry> consumer) { this.entryMap.forEach(consumer); }

}
